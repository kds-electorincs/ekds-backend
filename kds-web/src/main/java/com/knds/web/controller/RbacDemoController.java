package com.knds.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * TEMPORARY: demonstrates @PreAuthorize("@adminPageGuard.canAccess(...)") works.
 *
 * Delete this controller as soon as Phase 2 introduces the real ProductController.
 * The real controller will use the same annotation pattern.
 */
@RestController
@RequestMapping("/api/admin/rbac-demo")
public class RbacDemoController {

    @GetMapping("/products")
    @PreAuthorize("@adminPageGuard.canAccess('PRODUCTS')")
    public Map<String, String> productsPage() {
        return Map.of("page", "PRODUCTS", "status", "you can access this page");
    }

    @GetMapping("/orders")
    @PreAuthorize("@adminPageGuard.canAccess('ORDERS')")
    public Map<String, String> ordersPage() {
        return Map.of("page", "ORDERS", "status", "you can access this page");
    }
}