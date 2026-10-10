package pe.edu.utp.ebenezer.service.user;

import java.util.List;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.user.ResetPasswordRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserCreateRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserStatusRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserUpdateRequest;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUser(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = request.username().trim();
        String email = normalizeEmail(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Username is already in use");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BusinessException("Email is already in use");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(roleService.getByName(request.role()));
        user.setActive(true);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        String email = normalizeEmail(request.email());

        if (email != null && userRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Email is already in use");
        }
        if (isCurrentUser(user) && user.getRole().getName() != request.role()) {
            throw new BusinessException("You cannot change your own role");
        }

        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(roleService.getByName(request.role()));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UserStatusRequest request) {
        User user = getUser(id);
        if (!request.active() && isCurrentUser(user)) {
            throw new BusinessException("You cannot deactivate your own account");
        }
        user.setActive(request.active());
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = getUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public boolean createInitialAdminIfMissing(String name, String username, String password) {
        if (userRepository.count() > 0) {
            return false;
        }
        User admin = new User();
        admin.setName(name);
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(roleService.getByName(RoleName.ADMIN));
        admin.setActive(true);
        userRepository.save(admin);
        return true;
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isCurrentUser(User user) {
        return Objects.equals(currentUserProvider.getCurrentUser().getId(), user.getId());
    }

    private static String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }
}
