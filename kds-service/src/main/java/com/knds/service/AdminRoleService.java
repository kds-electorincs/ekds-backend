package com.knds.service;

import com.knds.commons.dto.AdminRoleResponse;
import com.knds.commons.dto.CreateAdminRoleRequest;
import com.knds.commons.dto.UpdateAdminRoleRequest;

import java.util.List;

public interface AdminRoleService {

    AdminRoleResponse create(CreateAdminRoleRequest request, Long createdByUserId);

    List<AdminRoleResponse> listAll();

    AdminRoleResponse getById(Long id);

    AdminRoleResponse update(Long id, UpdateAdminRoleRequest request);

    void delete(Long id);
}