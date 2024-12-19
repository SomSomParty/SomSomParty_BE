package com.acc.somsomparty.domain.Notification.service;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import com.acc.somsomparty.domain.Festival.service.FestivalQueryService;
import com.acc.somsomparty.domain.Reservation.service.ReservationQueryService;
import com.acc.somsomparty.domain.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    @Value("${aws.sns.application-arn}")
    private String arn;

    private final FestivalQueryService festivalQueryService;
    private final ReservationQueryService reservationQueryService;
    private final FcmTokenQueryService fcmTokenQueryService;
    private final SNSService snsService;

    // festival id로 토큰 리스트 가져오기
    private List<String> getFCMTokensByFestivalId(Long festivalId) {
        List<User> users = reservationQueryService.getReservationListByFestivalId(festivalId);
        List<String> tokens = new ArrayList<>();
        for (User user : users) {
            tokens.addAll(fcmTokenQueryService.getTokensByUserId(user.getId()));
        }
        return tokens;
    }

    // festival 알림을 위한 aws sns 설정
    public void setAWSSNSForFestival() {
        LocalDate localDate = LocalDate.now();
        List<Festival> festivals = festivalQueryService.getFestivalListByStartTime(localDate);

        for (Festival festival : festivals) {
            String title = festival.getName();
            String msg = "하루 전입니다.";

            List<String> tokens = getFCMTokensByFestivalId(festival.getId());

            for (String token : tokens) {
                snsService.snsFCMWorkFlow(arn, token, title, msg);
            }
        }
    }
}
