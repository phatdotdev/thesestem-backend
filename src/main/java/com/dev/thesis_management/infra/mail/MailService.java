package com.dev.thesis_management.infra.mail;

public interface MailService {
    void sendText(String to, String subject, String content);
    void sendHtml(String to, String subject, String html);
}
