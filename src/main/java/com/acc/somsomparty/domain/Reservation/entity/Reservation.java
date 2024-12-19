package com.acc.somsomparty.domain.Reservation.entity;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import com.acc.somsomparty.domain.Reservation.enums.ReservationStatus;
import com.acc.somsomparty.domain.User.entity.User;
import com.acc.somsomparty.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "festival_id")
    private Festival festival;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
}
