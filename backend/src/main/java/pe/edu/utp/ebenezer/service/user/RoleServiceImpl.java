package pe.edu.utp.ebenezer.service.user;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.user.RoleResponse;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.user.RoleRepository;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Map<RoleName, String> DEFAULT_DESCRIPTIONS = Map.of(
            RoleName.ADMIN, "Full access to the system",
            RoleName.CASHIER, "Sales and daily store operations");

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll(Sort.by("id")).stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getByName(RoleName name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }

    @Override
    @Transactional
    public void ensureDefaultRoles() {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                role.setDescription(DEFAULT_DESCRIPTIONS.get(name));
                roleRepository.save(role);
            }
        }
    }
}
