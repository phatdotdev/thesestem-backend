package com.dev.thesis_management.infra.mail;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailTemplateService {
    SpringTemplateEngine templateEngine;

    public String renderVerifyEmail(
            String email,
            String otp,
            int expireMinutes
    ) {
        Context context = new Context();
        context.setVariable("mail", email);
        context.setVariable("otp", otp);
        context.setVariable("expireMinutes", expireMinutes);

        return templateEngine.process(
                "mail/verify-mail",
                context
        );
    }
}
