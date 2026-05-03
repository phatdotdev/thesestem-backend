package com.dev.thesis_management.auth.service;

import com.dev.thesis_management.auth.dto.*;
import com.dev.thesis_management.config.security.JwtTokenProvider;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthenticatedException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.infra.mail.EmailTemplateService;
import com.dev.thesis_management.infra.mail.MailService;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.OrganizationType;
import com.dev.thesis_management.user.enums.UserRole;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.repository.StudentRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.OTPUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider jwtTokenProvider;

    RefreshTokenRedisService refreshTokenRedisService;
    RegisterRedisService registerRedisService;
    EmailTemplateService emailTemplateService;
    MailService mailService;

    OrganizationRepository organizationRepository;
    StudentRepository studentRepository;
    LecturerRepository lecturerRepository;

    @NonFinal
    @Value("${jwt.access.expiration.ms:9000}")
    Long accessExpirationMs;

    public AuthResponse login(LoginRequest request){
        return userRepository.findByUsernameAndRoleIn(request.getUsername(), List.of(UserRole.MANAGER, UserRole.ADMIN))
                .map(user -> {
                    if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
                        throw new BadRequestException("Invalid username or password");
                    }
                    String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
                    refreshTokenRedisService.save(user.getId(), refreshToken);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiresIn(accessExpirationMs)
                            .code(user.getOrganization() != null
                                    ? user.getOrganization().getCode() != null
                                    ? user.getOrganization().getCode()
                                    : user.getOrganization().getId().toString()
                                    : null)
                            .tokenType("Bearer")
                            .role(user.getRole().name())
                            .build();
                }).orElseThrow(() -> new BadRequestException("User not found."));
    }

    public AuthResponse refresh(String refreshToken){
        if(refreshToken == null || refreshToken.isBlank()){
            throw new UnauthenticatedException("Refresh token not found");
        }
        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());
        String storedRefreshToken = refreshTokenRedisService.get(userId);
        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(userId, user.getRole().name()))
                .refreshToken(refreshToken)
                .expiresIn(accessExpirationMs)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .build();
    }

    public void logout(UUID userId){
        if (userId == null) {
            throw new UnauthenticatedException("User not authenticated");
        }
        refreshTokenRedisService.delete(userId);
    }

    public void register(RegisterRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new BadRequestException("Email đã tồn tại");
        }

        String otp = generateOtp();

        RegisterInfo info = RegisterInfo.builder()
                .email(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .code(otp)
                .verified(false)
                .build();

        registerRedisService.save(info);

        String html = emailTemplateService.renderVerifyEmail(
                request.getUsername(),
                otp,
                10
        );

        mailService.sendHtml(
                request.getUsername(),
                "Xác thực tài khoản",
                html
        );
    }

    public void resendOtp(String email){

        RegisterInfo info = registerRedisService.get(email);

        if(info == null){
            throw new BadRequestException("Phiên đăng ký đã hết hạn");
        }

        long ttl = registerRedisService.getTtl(email);

        if(ttl > 60){
            throw new BadRequestException("Vui lòng chờ 60s để gửi lại OTP");
        }

        String otp = generateOtp();

        info.setCode(otp);

        registerRedisService.save(info);

        String html = emailTemplateService.renderVerifyEmail(
                email,
                otp,
                10
        );

        mailService.sendHtml(
                email,
                "Xác thực tài khoản",
                html
        );
    }

    public void verify(VerifyRequest request){

        RegisterInfo info = registerRedisService.get(request.getEmail());

        if(info == null){
            throw new UnauthenticatedException("OTP đã hết hạn");
        }

        if(!info.getCode().equals(request.getCode())){
            throw new UnauthenticatedException("OTP không đúng");
        }

        info.setVerified(true);

        registerRedisService.update(info);
    }

    @Transactional
    public void createOrganization(CreateOrgRequest request){

        RegisterInfo info = registerRedisService.get(request.getEmail());

        if(info == null){
            throw new BadRequestException("Phiên đăng ký đã hết hạn");
        }

        if(!info.isVerified()){
            throw new UnauthorizedException("Email chưa xác thực");
        }

        if(organizationRepository.existsByCode(request.getOrgCode())){
            throw new BadRequestException("Mã tổ chức đã tồn tại");
        }

        User user = User.builder()
                .username(info.getEmail())
                .password(info.getPassword())
                .role(UserRole.MANAGER)
                .build();

        Organization org = Organization.builder()
                .name(request.getOrgName())
                .code(request.getOrgCode())
                .email(request.getOrgEmail())
                .phone(request.getOrgPhone())
                .type(OrganizationType.valueOf(request.getOrgType()))
                .manager(user)
                .address(request.getOrgAddress())
                .build();

        user.setOrganization(org);

        userRepository.save(user);

        organizationRepository.save(org);

        registerRedisService.delete(request.getEmail());
    }

    public AuthResponse loginToOrg(String code, LoginRequest request) {
        Organization organization = organizationRepository.findByIdOrCode(parseUUID(code), code)
                .orElseThrow(() -> new BadRequestException("Organization not found"));
        User user = findUserInOrganization(organization, request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenRedisService.save(user.getId(), refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpirationMs)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .build();
    }

    private Optional<User> findUserInOrganization(
            Organization organization,
            String username
    ) {

        if(organization.getManager().getUsername().equals(username)){
            return Optional.of(organization.getManager());
        }

        Optional<Student> student = studentRepository
                .findByOrganizationAndStudentCode(organization, username);

        if (student.isPresent()) {
            return Optional.of(student.get().getUser());
        }

        Optional<Lecturer> lecturer = lecturerRepository
                .findByOrganizationAndLecturerCode(organization, username);

        return lecturer.map(Lecturer::getUser);

    }

}
