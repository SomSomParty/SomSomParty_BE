package com.acc.somsomparty.domain.Festival.repository;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Page<Festival> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Festival> findByCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime createdAt, Pageable pageable);

    @Query("SELECT f FROM Festival f WHERE (f.nameLower LIKE CONCAT('%', :keyword, '%') " +
            "OR f.descriptionLower LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:lastId = 0 OR f.id < :lastId) " +
            "ORDER BY f.id DESC")
    Page<Festival> searchByKeyword(Long lastId, String keyword, Pageable pageable);

}
