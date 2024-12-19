package com.acc.somsomparty.domain.Festival.entity;

import com.acc.somsomparty.domain.Reservation.entity.Reservation;
import com.acc.somsomparty.global.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Festival extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
    private List<Reservation> reservationList = new ArrayList<>();
//    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
//    private List<Ticket> ticketList = new ArrayList<>();
}
