package com.acc.somsomparty.domain.Reservation.service;

import com.acc.somsomparty.domain.Reservation.entity.Reservation;
import com.acc.somsomparty.domain.User.entity.User;

import java.util.List;

public interface ReservationQueryService {
    List<User> getReservationListByFestivalId(Long festivalId);
}
