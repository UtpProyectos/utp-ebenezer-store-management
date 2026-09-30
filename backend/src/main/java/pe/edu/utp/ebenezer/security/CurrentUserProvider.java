package pe.edu.utp.ebenezer.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;
import pe.edu.utp.ebenezer.exception.UnauthorizedException;

/**
 * Resolves the authenticated {@link User}. Use it from services to set the user of
 * sales, purchases, internal consumptions, movements, etc. Call it inside a transaction
 * to get a managed entity.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Authentication is required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
    }
}
