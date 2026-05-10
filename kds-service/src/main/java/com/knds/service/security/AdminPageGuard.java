package com.knds.service.security;

import com.knds.commons.security.AdminPage;
import com.knds.service.AdminAccessService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security expression bean. Used in @PreAuthorize annotations:
 *
 *     @PreAuthorize("@adminPageGuard.canAccess('PRODUCTS')")
 *
 * Looks up the currently authenticated user from SecurityContext and asks
 * AdminAccessService whether they have access to the given page.
 */
@Component("adminPageGuard")
public class AdminPageGuard {

    private final AdminAccessService accessService;

    public AdminPageGuard(AdminAccessService accessService) {
        this.accessService = accessService;
    }

    public boolean canAccess(String pageName) {
        AdminPage page;
        try {
            page = AdminPage.valueOf(pageName);
        } catch (IllegalArgumentException ex) {
            // Typo in @PreAuthorize annotation — fail closed, log loud.
            // (Could throw instead; refusing access is safer for production.)
            return false;
        }

        Long userId = currentUserId();
        if (userId == null) return false;

        return accessService.canAccess(userId, page);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        // The principal is JwtAuthenticationFilter.JwtUserContext (record with userId, email).
        // We avoid importing the kds-web type by using duck typing on the principal.
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal jp) {
            return jp.userId();
        }
        return null;
    }

    /**
     * Marker interface that the JwtUserContext record (in kds-web) implements,
     * so AdminPageGuard can read userId without depending on kds-web.
     *
     * kds-web's JwtAuthenticationFilter.JwtUserContext should implement this interface.
     */
    public interface JwtPrincipal {
        Long userId();
    }
}