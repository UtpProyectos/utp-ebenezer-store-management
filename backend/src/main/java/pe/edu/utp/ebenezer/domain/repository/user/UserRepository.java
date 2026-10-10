package pe.edu.utp.ebenezer.domain.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

public interface UserRepository extends JpaRepository<User, Long> {

    // The role is always read after loading a user (authorities, responses): fetch it in the same query.
    @EntityGraph(attributePaths = "role")
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "role")
    List<User> findAllByOrderByIdAsc();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findFirstByRole_NameAndActiveTrueOrderByIdAsc(RoleName roleName);
}
