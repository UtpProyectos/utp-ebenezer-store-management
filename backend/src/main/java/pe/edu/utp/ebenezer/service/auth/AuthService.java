package pe.edu.utp.ebenezer.service.auth;

import pe.edu.utp.ebenezer.api.dto.auth.ChangePasswordRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginRequest;
import pe.edu.utp.ebenezer.api.dto.auth.LoginResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser();

    void changePassword(ChangePasswordRequest request);
}
