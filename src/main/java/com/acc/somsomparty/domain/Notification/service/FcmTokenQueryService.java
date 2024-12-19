package com.acc.somsomparty.domain.Notification.service;

import java.util.List;

public interface FcmTokenQueryService {
    List<String> getTokensByUserId(Long userId);
}
