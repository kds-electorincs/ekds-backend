package com.knds.repository.security;

import com.knds.commons.security.AdminPage;
import com.knds.entities.security.AdminUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AdminUserRoleRepository extends JpaRepository<AdminUserRole, AdminUserRole.Id> {

    List<AdminUserRole> findByUserId(Long userId);

    boolean existsByIdUserIdAndIdRoleId(Long userId, Long roleId);

    /**
     * Returns the union of all admin pages granted to a user via their assigned admin roles.
     * Single query, no entity hydration — just the page set.
     */
    @Query("""
        SELECT DISTINCT p
        FROM AdminUserRole aur
        JOIN aur.role r
        JOIN r.pages p
        WHERE aur.user.id = :userId
    """)
    Set<AdminPage> findGrantedPagesForUser(@Param("userId") Long userId);

    void deleteByIdUserIdAndIdRoleId(Long userId, Long roleId);

    void deleteByIdUserId(Long userId);
}