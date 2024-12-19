package com.acc.somsomparty.domain.Notification.service;

import com.acc.somsomparty.domain.Notification.repository.FcmTokenRepository;
import com.acc.somsomparty.global.exception.CustomException;
import com.acc.somsomparty.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmTokenQueryServiceImpl implements FcmTokenQueryService {
    private final FcmTokenRepository fcmTokenRepo;
    private static final Logger logger = LoggerFactory.getLogger(FcmTokenQueryService.class);

    public List<String> getTokensByUserId(Long userId) {
        List<String> fcmTokens = fcmTokenRepo.findTokensByUserId(userId);

        if (fcmTokens == null || fcmTokens.isEmpty()) {
            logger.error("FCM Tokens not Found for user : {}", userId);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        return fcmTokens;
    }
}
