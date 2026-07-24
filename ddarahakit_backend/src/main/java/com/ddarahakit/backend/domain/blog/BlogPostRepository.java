package com.ddarahakit.backend.domain.blog;

import com.ddarahakit.backend.domain.blog.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query(value = "SELECT b FROM BlogPost b JOIN FETCH b.user",
            countQuery = "SELECT COUNT(b) FROM BlogPost b")
    Page<BlogPost> findAllWithUser(Pageable pageable);

    @Query(value = "SELECT b FROM BlogPost b JOIN FETCH b.user WHERE b.category = :category",
            countQuery = "SELECT COUNT(b) FROM BlogPost b WHERE b.category = :category")
    Page<BlogPost> findByCategoryWithUser(@Param("category") String category, Pageable pageable);

    @Query(value = "SELECT b FROM BlogPost b JOIN FETCH b.user " +
            "WHERE b.title LIKE %:keyword% OR b.summary LIKE %:keyword% OR b.content LIKE %:keyword%",
            countQuery = "SELECT COUNT(b) FROM BlogPost b " +
                    "WHERE b.title LIKE %:keyword% OR b.summary LIKE %:keyword% OR b.content LIKE %:keyword%")
    Page<BlogPost> searchByKeywordWithUser(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT b FROM BlogPost b JOIN FETCH b.user " +
            "WHERE b.category = :category AND (b.title LIKE %:keyword% OR b.summary LIKE %:keyword% OR b.content LIKE %:keyword%)",
            countQuery = "SELECT COUNT(b) FROM BlogPost b " +
                    "WHERE b.category = :category AND (b.title LIKE %:keyword% OR b.summary LIKE %:keyword% OR b.content LIKE %:keyword%)")
    Page<BlogPost> searchByCategoryAndKeywordWithUser(@Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

    /** 카테고리별 글 수 (빈 카테고리 제외, 글 많은 순) — [category, count] */
    @Query("SELECT b.category, COUNT(b) FROM BlogPost b " +
            "WHERE b.category IS NOT NULL AND b.category <> '' " +
            "GROUP BY b.category ORDER BY COUNT(b) DESC")
    List<Object[]> countByCategory();

    @Query("SELECT b FROM BlogPost b JOIN FETCH b.user WHERE b.idx = :idx")
    Optional<BlogPost> findByIdWithUser(@Param("idx") Long idx);

    @Modifying
    @Query("UPDATE BlogPost b SET b.viewCount = b.viewCount + 1 WHERE b.idx = :idx")
    void incrementViewCount(@Param("idx") Long idx);
}
