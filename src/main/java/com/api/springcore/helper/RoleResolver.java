package com.api.springcore.helper;

import com.api.springcore.entity.Role;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleResolver {

    private final RoleRepository roleRepository;

    public Set<Role> resolve(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));
            return new HashSet<>(Set.of(defaultRole));
        }
        List<Role> found = roleRepository.findAllByIdIn(roleIds);
        if (found.size() != roleIds.size()) {
            throw new BadRequestException("One or more role IDs are invalid");
        }
        return new HashSet<>(found);
    }
}