package com.acc.somsomparty.domain.Festival.repository;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Page<Festival> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Festival> findByCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime createdAt, Pageable pageable);
    List<Festival> findByStartDate(LocalDate localDate);
}
