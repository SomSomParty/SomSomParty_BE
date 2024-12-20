package com.acc.somsomparty.domain.Festival.repository;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Page<Festival> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Festival> findByCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime createdAt, Pageable pageable);
}
