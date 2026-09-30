package pe.edu.utp.ebenezer.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.auth.ChangePasswordRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.security.JwtService;
import pe.edu.utp.ebenezer.security.SecurityConstants;
import pe.edu.utp.ebenezer.service.user.UserMapper;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        // Throws BadCredentialsException / DisabledException, mapped to 401 by GlobalExceptionHandler.
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, request.password()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        user.setLastLoginAt(LocalDateTime.now());

        String token = jwtService.generateToken(user.getUsername(), user.getRole().getName());
        return new LoginResponse(token, SecurityConstants.TOKEN_TYPE, jwtService.getExpirationSeconds(),
                UserMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserMapper.toResponse(currentUserProvider.getCurrentUser());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }
}
