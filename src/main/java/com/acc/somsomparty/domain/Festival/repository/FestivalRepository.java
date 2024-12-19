package com.acc.somsomparty.domain.Festival.repository;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Page<Festival> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Festival> findByCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime createdAt, Pageable pageable);

    @Query("SELECT f FROM Festival f WHERE (LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:lastId = 0 OR f.id < :lastId) " +
            "ORDER BY f.id DESC")
    List<Festival> searchByKeyword(Long lastId, int limit, String keyword);

}
