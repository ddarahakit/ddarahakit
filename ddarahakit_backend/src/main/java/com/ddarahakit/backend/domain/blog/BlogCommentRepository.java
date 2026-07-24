package com.ddarahakit.backend.domain.blog;

import com.ddarahakit.backend.domain.blog.model.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    @Query("SELECT c FROM BlogComment c JOIN FETCH c.user WHERE c.blogPost.idx = :blogIdx ORDER BY c.idx ASC")
    List<BlogComment> findByBlogPostWithUser(@Param("blogIdx") Long blogIdx);

    @Transactional
    void deleteAllByBlogPostIdx(Long blogPostIdx);
}
