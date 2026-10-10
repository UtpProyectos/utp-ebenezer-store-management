package pe.edu.utp.ebenezer.service.user;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.RoleRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class DemoFamilyUsersSeedService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public int seedMissingFamilyUsers() {
        int createdCount = 0;
        for (FamilyUser seed : FAMILY_USERS) {
            String username = usernameFor(seed.name());
            if (userRepository.existsByUsername(username)) {
                continue;
            }
            Role role = roleRepository.findByName(seed.role())
                    .orElseThrow(() -> new IllegalStateException("Default role is missing: " + seed.role()));
            User user = new User();
            user.setName(seed.name());
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(username));
            user.setRole(role);
            user.setActive(true);
            userRepository.save(user);
            createdCount++;
        }
        return createdCount;
    }

    private static String usernameFor(String name) {
        String[] parts = name.trim().split("\\s+");
        return normalize(parts[0]) + "." + normalize(parts[parts.length - 1]);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private record FamilyUser(String name, RoleName role) {
    }

    private static final List<FamilyUser> FAMILY_USERS = List.of(
            new FamilyUser("Nelly Garcia", RoleName.ADMIN),
            new FamilyUser("Ruben Velarde", RoleName.ADMIN),
            new FamilyUser("Eduardo Velarde", RoleName.CASHIER),
            new FamilyUser("Renzo Velarde", RoleName.CASHIER)
    );
}
