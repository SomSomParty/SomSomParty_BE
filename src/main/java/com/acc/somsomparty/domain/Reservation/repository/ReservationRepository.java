package com.acc.somsomparty.domain.Reservation.repository;

import com.acc.somsomparty.domain.Reservation.entity.Reservation;
import com.acc.somsomparty.domain.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r.user FROM Reservation r WHERE r.festival.id = :festivalId")
    List<User> findUsersByFestivalId(@Param("festivalId") Long festivalId);
}
