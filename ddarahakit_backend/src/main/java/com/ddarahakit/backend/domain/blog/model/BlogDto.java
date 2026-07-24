package com.ddarahakit.backend.domain.blog.model;

import com.ddarahakit.backend.domain.user.model.entity.User;
import com.ddarahakit.backend.utils.TimeAgoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

public class BlogDto {

    private BlogDto() {
    }

    // ================================
    // Request DTOs
    // ================================

    @Getter
    @Schema(description = "블로그 글 작성 요청")
    public static class BlogCreateRequest {

        @Schema(description = "제목", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(min = 1, max = 150, message = "제목은 1자 이상 150자 이하로 입력해주세요.")
        private String title;

        @Schema(description = "카테고리 (선택)")
        @Size(max = 50, message = "카테고리는 50자 이하로 입력해주세요.")
        private String category;

        @Schema(description = "요약 (목록 미리보기용, 선택)")
        @Size(max = 500, message = "요약은 500자 이하로 입력해주세요.")
        private String summary;

        @Schema(description = "대표 이미지 URL (선택)")
        @Size(max = 500, message = "이미지 URL은 500자 이하로 입력해주세요.")
        private String thumbnailUrl;

        @Schema(description = "본문 내용 (에디터 Delta JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;

        public BlogPost toEntity(User user) {
            return BlogPost.builder()
                    .title(this.title)
                    .category(this.category)
                    .summary(this.summary)
                    .thumbnailUrl(this.thumbnailUrl)
                    .content(this.content)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @Schema(description = "블로그 글 수정 요청")
    public static class BlogUpdateRequest {

        @Schema(description = "제목", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(min = 1, max = 150, message = "제목은 1자 이상 150자 이하로 입력해주세요.")
        private String title;

        @Schema(description = "카테고리 (선택)")
        @Size(max = 50, message = "카테고리는 50자 이하로 입력해주세요.")
        private String category;

        @Schema(description = "요약 (목록 미리보기용, 선택)")
        @Size(max = 500, message = "요약은 500자 이하로 입력해주세요.")
        private String summary;

        @Schema(description = "대표 이미지 URL (선택)")
        @Size(max = 500, message = "이미지 URL은 500자 이하로 입력해주세요.")
        private String thumbnailUrl;

        @Schema(description = "본문 내용 (에디터 Delta JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    // ================================
    // Response DTOs
    // ================================

    @Getter
    @Builder
    @Schema(description = "블로그 목록 페이징 응답")
    public static class BlogPageResponse {
        private final int page;
        private final int size;
        private final boolean hasNext;
        private final boolean hasPrev;
        private final int totalPages;
        private final long totalPosts;
        private final List<BlogSummaryResponse> posts;

        public static BlogPageResponse from(Page<BlogPost> postPage) {
            List<BlogSummaryResponse> posts = postPage.getContent().stream()
                    .map(BlogSummaryResponse::from)
                    .toList();

            return BlogPageResponse.builder()
                    .page(postPage.getNumber())
                    .size(postPage.getSize())
                    .hasNext(postPage.hasNext())
                    .hasPrev(postPage.hasPrevious())
                    .totalPages(postPage.getTotalPages())
                    .totalPosts(postPage.getTotalElements())
                    .posts(posts)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "블로그 목록용 요약 응답")
    public static class BlogSummaryResponse {
        private final Long idx;
        private final String title;
        private final String category;
        private final String summary;
        private final String thumbnailUrl;
        private final String userName;
        private final Long userIdx;
        private final int viewCount;
        private final String createdAt;

        public static BlogSummaryResponse from(BlogPost post) {
            User user = post.getUser();
            return BlogSummaryResponse.builder()
                    .idx(post.getIdx())
                    .title(post.getTitle())
                    .category(post.getCategory())
                    .summary(post.getSummary())
                    .thumbnailUrl(post.getThumbnailUrl())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .viewCount(post.getViewCount())
                    .createdAt(TimeAgoUtil.timeAgo(post.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "블로그 글 상세 응답")
    public static class BlogDetailResponse {
        private final Long idx;
        private final String title;
        private final String category;
        private final String summary;
        private final String thumbnailUrl;
        private final String content;
        private final String userName;
        private final Long userIdx;
        private final String userProfileImageUrl;
        private final int viewCount;
        private final String createdAt;
        private final String updatedAt;

        @Schema(description = "댓글 목록")
        private final List<BlogCommentResponse> comments;

        @Schema(description = "댓글 수")
        private final int commentCount;

        public static BlogDetailResponse from(BlogPost post) {
            return from(post, List.of());
        }

        public static BlogDetailResponse from(BlogPost post, List<BlogCommentResponse> comments) {
            User user = post.getUser();
            return BlogDetailResponse.builder()
                    .idx(post.getIdx())
                    .title(post.getTitle())
                    .category(post.getCategory())
                    .summary(post.getSummary())
                    .thumbnailUrl(post.getThumbnailUrl())
                    .content(post.getContent())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .userProfileImageUrl(user != null ? user.getProfileImageUrl() : null)
                    .viewCount(post.getViewCount())
                    .createdAt(TimeAgoUtil.timeAgo(post.getCreatedAt()))
                    .updatedAt(TimeAgoUtil.timeAgo(post.getUpdatedAt()))
                    .comments(comments)
                    .commentCount(comments != null ? comments.size() : 0)
                    .build();
        }
    }

    @Getter
    @Schema(description = "블로그 댓글 작성 요청")
    public static class BlogCommentCreateRequest {
        @Schema(description = "댓글 내용", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 1000, message = "댓글은 1000자 이하로 입력해주세요.")
        private String content;
    }

    @Getter
    @Schema(description = "블로그 댓글 수정 요청")
    public static class BlogCommentUpdateRequest {
        @Schema(description = "댓글 내용", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 1000, message = "댓글은 1000자 이하로 입력해주세요.")
        private String content;
    }

    @Getter
    @Builder
    @Schema(description = "블로그 댓글 응답")
    public static class BlogCommentResponse {
        private final Long idx;
        private final String content;
        private final String userName;
        private final Long userIdx;
        private final String userProfileImageUrl;
        private final String createdAt;

        public static BlogCommentResponse from(BlogComment comment) {
            User user = comment.getUser();
            return BlogCommentResponse.builder()
                    .idx(comment.getIdx())
                    .content(comment.getContent())
                    .userName(user != null ? user.getName() : "알 수 없음")
                    .userIdx(user != null ? user.getIdx() : null)
                    .userProfileImageUrl(user != null ? user.getProfileImageUrl() : null)
                    .createdAt(TimeAgoUtil.timeAgo(comment.getCreatedAt()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "카테고리별 글 수 응답")
    public static class CategoryCountResponse {
        private final String category;
        private final long count;

        public static CategoryCountResponse of(String category, long count) {
            return CategoryCountResponse.builder()
                    .category(category)
                    .count(count)
                    .build();
        }
    }
}
