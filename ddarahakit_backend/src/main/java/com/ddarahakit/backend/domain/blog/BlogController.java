package com.ddarahakit.backend.domain.blog;

import com.ddarahakit.backend.common.model.BaseResponse;
import com.ddarahakit.backend.config.security.AuthUserDetails;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentCreateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCommentUpdateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogCreateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogDetailResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogPageResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogSummaryResponse;
import com.ddarahakit.backend.domain.blog.model.BlogDto.BlogUpdateRequest;
import com.ddarahakit.backend.domain.blog.model.BlogDto.CategoryCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Blog", description = "블로그 API (관리자 작성, 전체 공개 열람)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "블로그 글 목록 조회", description = "블로그 글 목록을 페이징으로 조회합니다. 카테고리/키워드로 필터할 수 있습니다. (공개)")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<BlogPageResponse>> getList(
            @Parameter(description = "카테고리 필터")
            @RequestParam(required = false) String category,
            @Parameter(description = "검색 키워드 (제목/요약/본문)")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 9, sort = "idx", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.getList(category, keyword, pageable)));
    }

    @Operation(summary = "카테고리 목록 조회", description = "카테고리별 글 수를 조회합니다. (공개, aside용)")
    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<List<CategoryCountResponse>>> getCategories() {
        return ResponseEntity.ok(BaseResponse.success(blogService.getCategories()));
    }

    @Operation(summary = "최신 글 조회", description = "최신 글 목록을 조회합니다. (공개, aside용)")
    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<List<BlogSummaryResponse>>> getRecent(
            @Parameter(description = "조회할 글 수 (기본 5)")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.getRecent(limit)));
    }

    @Operation(summary = "블로그 글 상세 조회", description = "블로그 글 상세를 조회합니다. (공개, 조회수 +1)")
    @GetMapping("/{idx}")
    public ResponseEntity<BaseResponse<BlogDetailResponse>> getDetail(
            @Parameter(description = "블로그 글 ID") @PathVariable Long idx
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.getDetail(idx)));
    }

    @Operation(summary = "블로그 글 작성", description = "새 블로그 글을 작성합니다. 관리자만 가능합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<BlogDetailResponse>> create(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody BlogCreateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.create(authUserDetails, request)));
    }

    @Operation(summary = "블로그 글 수정", description = "블로그 글을 수정합니다. 관리자만 가능합니다.")
    @PutMapping("/{idx}")
    public ResponseEntity<BaseResponse<BlogDetailResponse>> update(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "블로그 글 ID") @PathVariable Long idx,
            @Valid @RequestBody BlogUpdateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.update(authUserDetails, idx, request)));
    }

    @Operation(summary = "블로그 글 삭제", description = "블로그 글을 삭제합니다. 관리자만 가능합니다.")
    @DeleteMapping("/{idx}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "블로그 글 ID") @PathVariable Long idx
    ) {
        blogService.delete(authUserDetails, idx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    // ================================
    // 댓글 (로그인 사용자 누구나 작성)
    // ================================

    @Operation(summary = "블로그 댓글 작성", description = "블로그 글에 댓글을 작성합니다. 로그인 사용자면 누구나 가능합니다.")
    @PostMapping("/{idx}/comment")
    public ResponseEntity<BaseResponse<BlogCommentResponse>> createComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "블로그 글 ID") @PathVariable Long idx,
            @Valid @RequestBody BlogCommentCreateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.createComment(authUserDetails, idx, request)));
    }

    @Operation(summary = "블로그 댓글 수정", description = "댓글을 수정합니다. 작성자 본인만 가능합니다.")
    @PutMapping("/comment/{commentIdx}")
    public ResponseEntity<BaseResponse<BlogCommentResponse>> updateComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "댓글 ID") @PathVariable Long commentIdx,
            @Valid @RequestBody BlogCommentUpdateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.success(blogService.updateComment(authUserDetails, commentIdx, request)));
    }

    @Operation(summary = "블로그 댓글 삭제", description = "댓글을 삭제합니다. 작성자 본인 또는 관리자만 가능합니다.")
    @DeleteMapping("/comment/{commentIdx}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "댓글 ID") @PathVariable Long commentIdx
    ) {
        blogService.deleteComment(authUserDetails, commentIdx);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
