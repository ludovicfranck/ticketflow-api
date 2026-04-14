package com.ticketflow.user_service.repository;

import com.ticketflow.user_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepositoty extends JpaRepository<Role,String> {
    Set<Role> findByNameIn(Set<String> roleName);
}
