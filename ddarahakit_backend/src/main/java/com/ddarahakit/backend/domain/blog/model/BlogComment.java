package com.ddarahakit.backend.domain.blog.model;

import com.ddarahakit.backend.common.model.BaseEntity;
import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 블로그 댓글 엔티티 (평문 텍스트)
 *
 * 로그인 사용자 누구나 작성, 수정은 작성자 본인, 삭제는 작성자 또는 관리자.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class BlogComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_idx")
    private BlogPost blogPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public void update(String content) {
        this.content = content;
    }
}
