package com.ticketflow.user_service.repository;

import com.ticketflow.user_service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PermissionRepositoty extends JpaRepository<Permission, String> {
    Set<Permission> findByNameIn(Set<String> permissionNames);
}
