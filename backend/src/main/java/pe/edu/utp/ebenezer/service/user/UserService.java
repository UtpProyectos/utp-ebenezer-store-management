package pe.edu.utp.ebenezer.service.user;

import java.util.List;

import pe.edu.utp.ebenezer.api.dto.user.ResetPasswordRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserCreateRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserStatusRequest;
import pe.edu.utp.ebenezer.api.dto.user.UserUpdateRequest;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse updateStatus(Long id, UserStatusRequest request);

    void resetPassword(Long id, ResetPasswordRequest request);

    /**
     * Creates the first ADMIN user only when the users table is empty.
     *
     * @return true if the user was created
     */
    boolean createInitialAdminIfMissing(String name, String username, String password);
}
