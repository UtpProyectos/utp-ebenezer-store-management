package pe.edu.utp.ebenezer.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.RoleRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DemoFamilyUsersSeedServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private DemoFamilyUsersSeedService seedService;

    @Test
    void createsFourActiveUsersWithRoleBasedOnFamilyList() {
        Role adminRole = new Role();
        adminRole.setName(RoleName.ADMIN);
        Role cashierRole = new Role();
        cashierRole.setName(RoleName.CASHIER);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(RoleName.CASHIER)).thenReturn(Optional.of(cashierRole));
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));

        assertThat(seedService.seedMissingFamilyUsers()).isEqualTo(4);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(4)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(User::getUsername)
                .containsExactly("nelly.garcia", "ruben.velarde", "eduardo.velarde", "renzo.velarde");
        assertThat(captor.getAllValues()).allMatch(User::getActive);
        assertThat(captor.getAllValues())
                .extracting(User::getPasswordHash)
                .containsExactly(
                        "encoded:nelly.garcia",
                        "encoded:ruben.velarde",
                        "encoded:eduardo.velarde",
                        "encoded:renzo.velarde"
                );
        assertThat(captor.getAllValues())
                .extracting(user -> user.getRole().getName())
                .containsExactly(RoleName.ADMIN, RoleName.ADMIN, RoleName.CASHIER, RoleName.CASHIER);
    }

    @Test
    void leavesExistingAccountsUntouched() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThat(seedService.seedMissingFamilyUsers()).isZero();

        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByName(any());
    }
}
