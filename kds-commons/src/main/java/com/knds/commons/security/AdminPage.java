package com.knds.commons.security;

/**
 * Admin panel sections that can be granted to admin roles.
 *
 * Each value represents one logical section of the admin UI with full CRUD access.
 * Adding a new admin page = add a value here + an entry in admin-pages.properties for its label.
 *
 * NEVER rename or remove an enum value once it's referenced by an admin_role_pages
 * row in production. Only add new values. Renaming would orphan existing role grants.
 */
public enum AdminPage {

    PRODUCTS,
    CATEGORIES,

    ORDERS,

    USERS,
    SUPPORT,
    FINANCE,

    BANNERS,
    REPORTS;
}