package pe.edu.utp.ebenezer.domain.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findFirstByRole_NameAndActiveTrueOrderByIdAsc(RoleName roleName);
}
