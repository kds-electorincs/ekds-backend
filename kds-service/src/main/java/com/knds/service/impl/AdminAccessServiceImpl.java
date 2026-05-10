package com.knds.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.knds.commons.security.AdminPage;
import com.knds.entities.security.User;
import com.knds.repository.security.AdminUserRoleRepository;
import com.knds.repository.security.UserRepository;
import com.knds.service.AdminAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

@Service
public class AdminAccessServiceImpl implements AdminAccessService {

    /** Sentinel: super admins implicitly hold every page, no DB lookup needed. */
    private static final Set<AdminPage> ALL_PAGES = EnumSet.allOf(AdminPage.class);

    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final UserRepository userRepo;
    private final AdminUserRoleRepository adminUserRoleRepo;

    /**
     * Cache: userId → granted pages.
     * Caffeine, in-JVM. No Redis (per project locked decisions).
     *
     * 5 min TTL is the safety net — invalidate() is the primary mechanism.
     * The TTL is there in case someone modifies grants via direct SQL.
     */
    private final Cache<Long, Set<AdminPage>> pagesCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public AdminAccessServiceImpl(UserRepository userRepo,
                                  AdminUserRoleRepository adminUserRoleRepo) {
        this.userRepo = userRepo;
        this.adminUserRoleRepo = adminUserRoleRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<AdminPage> getGrantedPages(Long userId) {
        return pagesCache.get(userId, this::loadFromDb);
    }

    @Override
    public boolean canAccess(Long userId, AdminPage page) {
        return getGrantedPages(userId).contains(page);
    }

    @Override
    public void invalidate(Long userId) {
        pagesCache.invalidate(userId);
    }

    @Override
    public void invalidateAll() {
        pagesCache.invalidateAll();
    }

    // ── helpers ────────────────────────────────────────────────────

    private Set<AdminPage> loadFromDb(Long userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return Set.of();      // user gone → no access

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> SUPER_ADMIN_ROLE.equals(r.getName()));

        if (isSuperAdmin) {
            return ALL_PAGES;       // super admin sees everything, no per-role lookup
        }

        Set<AdminPage> granted = adminUserRoleRepo.findGrantedPagesForUser(userId);
        // Defensive: ensure immutable snapshot (Caffeine doesn't deep-copy)
        return granted.isEmpty() ? Set.of() : EnumSet.copyOf(granted);
    }
}