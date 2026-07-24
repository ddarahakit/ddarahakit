package com.ddarahakit.backend.domain.blog.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

/**
 * 블로그 글 엔티티
 *
 * 관리자가 작성하는 소개/공지성 글. 커뮤니티(Post)와 달리 댓글·스크랩·태그가 없고
 * 본문(content)은 에디터 Delta(JSON)로 저장한다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class BlogPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private String title;

    // 카테고리(선택) — 예: 네트워크, 개발, 인프라
    @Column(length = 50)
    private String category;

    // 목록 미리보기용 요약(선택)
    @Column(length = 500)
    private String summary;

    // 목록 카드 대표 이미지(선택)
    private String thumbnailUrl;

    // 본문(에디터 Delta JSON)
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 조회수
    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    public void update(String title, String category, String summary, String thumbnailUrl, String content) {
        this.title = title;
        this.category = category;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.content = content;
    }
}
