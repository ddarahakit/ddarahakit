package com.ddarahakit.backend.domain.blog;

import com.ddarahakit.backend.common.exception.BaseException;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.blog.model.BlogComment;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentCreateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentUpdateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCreateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogDetailResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogPageResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogSummaryResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogUpdateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.CategoryCountResponse;
import com.ddarahakit.backend.domain.blog.model.BlogPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ddarahakit.backend.common.model.BaseResponseStatus.BLOG_NOT_FOUND;
import static com.ddarahakit.backend.common.model.BaseResponseStatus.COMMENT_NOT_FOUND;
import static com.ddarahakit.backend.common.model.BaseResponseStatus.COMMENT_UNAUTHORIZED;
import static com.ddarahakit.backend.common.model.BaseResponseStatus.INVALID_USER_ROLE;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BlogService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;

    /**
     * 블로그 글 목록 조회 (공개) — 카테고리/키워드 필터
     */
    public BlogPageResponse getList(String category, String keyword, Pageable pageable) {
        boolean hasCategory = StringUtils.hasText(category);
        boolean hasKeyword = StringUtils.hasText(keyword);

        Page<BlogPost> page;
        if (hasCategory && hasKeyword) {
            page = blogPostRepository.searchByCategoryAndKeywordWithUser(category, keyword, pageable);
        } else if (hasCategory) {
            page = blogPostRepository.findByCategoryWithUser(category, pageable);
        } else if (hasKeyword) {
            page = blogPostRepository.searchByKeywordWithUser(keyword, pageable);
        } else {
            page = blogPostRepository.findAllWithUser(pageable);
        }
        return BlogPageResponse.from(page);
    }

    /**
     * 카테고리별 글 수 (aside 카테고리 목록용, 공개)
     */
    public List<CategoryCountResponse> getCategories() {
        return blogPostRepository.countByCategory().stream()
                .map(row -> CategoryCountResponse.of((String) row[0], (Long) row[1]))
                .toList();
    }

    /**
     * 최신 글 목록 (aside 최신글용, 공개)
     */
    public List<BlogSummaryResponse> getRecent(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "idx"));
        return blogPostRepository.findAllWithUser(pageable).getContent().stream()
                .map(BlogSummaryResponse::from)
                .toList();
    }

    /**
     * 블로그 글 상세 조회 (공개) — 조회 시 조회수 +1
     */
    @Transactional
    public BlogDetailResponse getDetail(Long idx) {
        blogPostRepository.incrementViewCount(idx);

        BlogPost post = blogPostRepository.findByIdWithUser(idx)
                .orElseThrow(() -> BaseException.of(BLOG_NOT_FOUND));

        List<BlogCommentResponse> comments = blogCommentRepository.findByBlogPostWithUser(idx).stream()
                .map(BlogCommentResponse::from)
                .toList();
        return BlogDetailResponse.from(post, comments);
    }

    /**
     * 블로그 글 작성 — 관리자(ROLE_ADMIN)만 가능
     */
    @Transactional
    public BlogDetailResponse create(AuthUserDetails authUserDetails, BlogCreateRequest request) {
        requireAdmin(authUserDetails);

        BlogPost saved = blogPostRepository.save(request.toEntity(authUserDetails.toEntity()));
        return BlogDetailResponse.from(saved);
    }

    /**
     * 블로그 글 수정 — 관리자(ROLE_ADMIN)만 가능
     */
    @Transactional
    public BlogDetailResponse update(AuthUserDetails authUserDetails, Long idx, BlogUpdateRequest request) {
        requireAdmin(authUserDetails);

        BlogPost post = blogPostRepository.findByIdWithUser(idx)
                .orElseThrow(() -> BaseException.of(BLOG_NOT_FOUND));

        post.update(request.getTitle(), request.getCategory(), request.getSummary(), request.getThumbnailUrl(), request.getContent());
        return BlogDetailResponse.from(post);
    }

    /**
     * 블로그 글 삭제 — 관리자(ROLE_ADMIN)만 가능
     */
    @Transactional
    public void delete(AuthUserDetails authUserDetails, Long idx) {
        requireAdmin(authUserDetails);

        BlogPost post = blogPostRepository.findById(idx)
                .orElseThrow(() -> BaseException.of(BLOG_NOT_FOUND));

        // 연관 댓글 먼저 정리 후 글 삭제
        blogCommentRepository.deleteAllByBlogPostIdx(idx);
        blogPostRepository.delete(post);
    }

    // ================================
    // 댓글 (로그인 사용자 누구나 작성)
    // ================================

    /**
     * 댓글 작성 — 로그인 사용자면 누구나 가능
     */
    @Transactional
    public BlogCommentResponse createComment(AuthUserDetails authUserDetails, Long blogIdx, BlogCommentCreateRequest request) {
        BlogPost post = blogPostRepository.findById(blogIdx)
                .orElseThrow(() -> BaseException.of(BLOG_NOT_FOUND));

        BlogComment comment = BlogComment.builder()
                .blogPost(post)
                .user(authUserDetails.toEntity())
                .content(request.getContent())
                .build();
        return BlogCommentResponse.from(blogCommentRepository.save(comment));
    }

    /**
     * 댓글 수정 — 작성자 본인만 가능
     */
    @Transactional
    public BlogCommentResponse updateComment(AuthUserDetails authUserDetails, Long commentIdx, BlogCommentUpdateRequest request) {
        BlogComment comment = blogCommentRepository.findById(commentIdx)
                .orElseThrow(() -> BaseException.of(COMMENT_NOT_FOUND));

        if (!comment.getUser().getIdx().equals(authUserDetails.getIdx())) {
            throw BaseException.of(COMMENT_UNAUTHORIZED);
        }
        comment.update(request.getContent());
        return BlogCommentResponse.from(comment);
    }

    /**
     * 댓글 삭제 — 작성자 본인 또는 관리자만 가능
     */
    @Transactional
    public void deleteComment(AuthUserDetails authUserDetails, Long commentIdx) {
        BlogComment comment = blogCommentRepository.findById(commentIdx)
                .orElseThrow(() -> BaseException.of(COMMENT_NOT_FOUND));

        boolean isOwner = comment.getUser().getIdx().equals(authUserDetails.getIdx());
        boolean isAdmin = ROLE_ADMIN.equals(authUserDetails.getRole());
        if (!isOwner && !isAdmin) {
            throw BaseException.of(COMMENT_UNAUTHORIZED);
        }
        blogCommentRepository.delete(comment);
    }

    private void requireAdmin(AuthUserDetails authUserDetails) {
        if (authUserDetails == null || !ROLE_ADMIN.equals(authUserDetails.getRole())) {
            throw BaseException.of(INVALID_USER_ROLE);
        }
    }
}
