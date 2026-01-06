package com.bkap.teach.common.notify;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Notification {
    private NotifyType type;
    private String message;
}
