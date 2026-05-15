package com.api.springcore.security;

import com.api.springcore.entity.User;
import com.api.springcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return UserPrincipal.of(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow();
        return UserPrincipal.of(user);
    }

    // ── UserPrincipal (inner) ────────────────────────────────────────────────

    public record UserPrincipal(
            Long id,
            String email,
            String passwordHash,
            boolean active,
            Collection<? extends GrantedAuthority> authorities
    ) implements UserDetails {

        public static UserPrincipal of(User user) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            // ROLE_ prefix for Spring Security role checks
            user.getRoles().forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
            // Raw permission strings for @PreAuthorize("hasAuthority(...)")
            user.getAllPermissions().forEach(perm ->
                    authorities.add(new SimpleGrantedAuthority(perm)));
            return new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    Boolean.TRUE.equals(user.getIsActive()),
                    authorities);
        }

        @Override @NullMarked public String getUsername() { return email; }
        @Override public String getPassword() { return passwordHash; }
        @Override public boolean isEnabled() { return active; }

        @Override public boolean isAccountNonLocked() { return active; }

        @Override @NullMarked public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    }
}