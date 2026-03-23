package com.dev.thesis_management.config.security;

import com.dev.thesis_management.auth.service.RefreshTokenRedisService;
import com.dev.thesis_management.auth.service.RegisterRedisService;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.OrganizationType;
import com.dev.thesis_management.user.enums.UserRole;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    UserRepository userRepository;
    OrganizationRepository organizationRepository;

    JwtTokenProvider jwtTokenProvider;
    PasswordEncoder passwordEncoder;
    RefreshTokenRedisService refreshTokenRedisService;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        // String providerId = oAuth2User.getAttribute("sub");
        User user = userRepository
                .findByUsernameAndRoleIn(email, List.of(UserRole.MANAGER, UserRole.ADMIN))
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(email)
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(UserRole.MANAGER)
                                .enabled(true)
                                .build()
                ));
        Organization organization = organizationRepository.findByManager(user)
                .orElseGet(() -> {
                    Organization org = Organization.builder()
                            .manager(user)
                            .type(OrganizationType.COLLEGE)
                            .code(UUID.randomUUID().toString())
                            .build();
                    return organizationRepository.save(org);
                });

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        Cookie deleteCookie = new Cookie("refresh_token", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);


        response.addCookie(deleteCookie);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);


        response.addCookie(refreshCookie);

        refreshTokenRedisService.save(user.getId(), refreshToken);


        String redirectUrl =
                "http://localhost:3000/oauth2/redirect"
                        + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                        + "&code=" + organization.getCode();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
