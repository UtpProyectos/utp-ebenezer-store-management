package pe.edu.utp.ebenezer.service.user;

import java.util.List;

import pe.edu.utp.ebenezer.api.dto.user.RoleResponse;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

public interface RoleService {

    List<RoleResponse> findAll();

    /** Internal use by other services. */
    Role getByName(RoleName name);

    /** Creates every {@link RoleName} that does not exist yet. Idempotent. */
    void ensureDefaultRoles();
}
