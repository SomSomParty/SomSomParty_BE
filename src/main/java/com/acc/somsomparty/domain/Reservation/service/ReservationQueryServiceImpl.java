package com.acc.somsomparty.domain.Reservation.service;

import com.acc.somsomparty.domain.Reservation.repository.ReservationRepository;
import com.acc.somsomparty.domain.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl implements ReservationQueryService {
    private final ReservationRepository reservationRepository;

    public List<User> getReservationListByFestivalId(Long festivalId) {
        return reservationRepository.findUsersByFestivalId(festivalId);
    }
}
