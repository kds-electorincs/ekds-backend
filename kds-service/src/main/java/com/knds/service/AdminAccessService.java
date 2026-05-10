package com.knds.service;

import com.knds.commons.security.AdminPage;

import java.util.Set;

public interface AdminAccessService {

    /**
     * Returns the union of all admin pages granted to this user via their assigned admin roles.
     * Cached; invalidated when grants change.
     *
     * Super admin gets all pages implicitly — they don't need explicit role assignments.
     */
    Set<AdminPage> getGrantedPages(Long userId);

    /** Convenience for guards. Equivalent to getGrantedPages(userId).contains(page). */
    boolean canAccess(Long userId, AdminPage page);

    /** Drop the cached page set for this user. Call after granting/revoking roles. */
    void invalidate(Long userId);

    /** Drop ALL cached page sets. Call after modifying a role's pages (which affects every user holding it). */
    void invalidateAll();
}