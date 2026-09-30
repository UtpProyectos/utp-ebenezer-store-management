package pe.edu.utp.ebenezer.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import pe.edu.utp.ebenezer.api.dto.auth.ChangePasswordRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginResponse;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginReturnsTokenAndUpdatesLastLogin() {
        User user = user("admin", RoleName.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("admin", RoleName.ADMIN)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest(" admin ", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().role()).isEqualTo(RoleName.ADMIN);
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void loginPropagatesBadCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginPropagatesDisabledUser() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("cashier", "secret123")))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        User user = user("admin", RoleName.ADMIN);
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(new ChangePasswordRequest("wrong", "newPassword1")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changePasswordStoresNewHash() {
        User user = user("admin", RoleName.ADMIN);
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("current1", "hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hash");

        authService.changePassword(new ChangePasswordRequest("current1", "newPassword1"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordEncoder).encode("newPassword1");
    }

    private static User user(String username, RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setName(username);
        user.setPasswordHash("hash");
        user.setRole(role);
        return user;
    }
}
