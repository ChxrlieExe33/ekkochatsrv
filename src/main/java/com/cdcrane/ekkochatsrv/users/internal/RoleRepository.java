package com.cdcrane.ekkochatsrv.users.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("SELECT r FROM Role r WHERE r.authority = 'USER'")
    Role getUserRole();

    @Query("SELECT r FROM Role r WHERE r.authority = 'ADMIN'")
    Role getAdminRole();
}
