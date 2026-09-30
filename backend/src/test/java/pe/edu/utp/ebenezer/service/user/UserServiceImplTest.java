package pe.edu.utp.ebenezer.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pe.edu.utp.ebenezer.api.dto.user.UserCreateRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserStatusRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserUpdateRequest;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createRejectsDuplicatedUsername() {
        when(userRepository.existsByUsername("cashier")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(
                new UserCreateRequest("Cashier", "cashier", null, "password1", RoleName.CASHIER)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Username is already in use");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicatedEmail() {
        when(userRepository.existsByUsername("cashier")).thenReturn(false);
        when(userRepository.existsByEmail("cashier@store.pe")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(
                new UserCreateRequest("Cashier", "cashier", " Cashier@Store.pe ", "password1", RoleName.CASHIER)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void createEncodesPasswordAndAssignsRole() {
        when(roleService.getByName(RoleName.CASHIER)).thenReturn(role(RoleName.CASHIER));
        when(passwordEncoder.encode("password1")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(
                new UserCreateRequest("Cashier", "cashier", "", "password1", RoleName.CASHIER));

        assertThat(response.role()).isEqualTo(RoleName.CASHIER);
        assertThat(response.email()).isNull();
        assertThat(response.active()).isTrue();
    }

    @Test
    void cannotDeactivateOwnAccount() {
        User admin = user(1L, RoleName.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);

        assertThatThrownBy(() -> userService.updateStatus(1L, new UserStatusRequest(false)))
                .isInstanceOf(BusinessException.class);
        assertThat(admin.getActive()).isTrue();
    }

    @Test
    void cannotChangeOwnRole() {
        User admin = user(1L, RoleName.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);

        assertThatThrownBy(() -> userService.update(1L, new UserUpdateRequest("Admin", null, RoleName.CASHIER)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void initialAdminIsOnlyCreatedWhenThereAreNoUsers() {
        when(userRepository.count()).thenReturn(1L);

        assertThat(userService.createInitialAdminIfMissing("Admin", "admin", "password1")).isFalse();
        verify(userRepository, never()).save(any());
    }

    private static Role role(RoleName name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }

    private static User user(Long id, RoleName roleName) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setUsername("user" + id);
        user.setRole(role(roleName));
        user.setActive(true);
        return user;
    }
}
