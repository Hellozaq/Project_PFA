package com.example.springnodebackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private String id;
    private String recipient;
    private String type;
    private String content;
    private Object data;
    private boolean isRead;
    private Date createdAt;
    private Date readAt;
}