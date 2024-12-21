package com.acc.somsomparty.domain.Notification.service;

public interface SNSService {
    void snsFCMWorkFlow(String platformApplicationArn, String token, String title, String msg);
}
