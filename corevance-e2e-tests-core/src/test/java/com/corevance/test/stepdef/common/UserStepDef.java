/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.corevance.test.stepdef.common;

import static com.corevance.client.feign.util.FeignCalls.ok;

import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.PostRolesRequest;
import com.corevance.client.models.PostRolesResponse;
import com.corevance.client.models.PostUsersRequest;
import com.corevance.client.models.PostUsersResponse;
import com.corevance.client.models.PutRolesRoleIdPermissionsRequest;
import com.corevance.test.helper.Utils;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class UserStepDef extends AbstractStepDef {

    private static final String EMAIL = "test@test.com";

    @Autowired
    private CorevanceFeignClient corevanceClient;

    private static final String PWD_USER_WITH_ROLE = "1234567890Aa!";

    @When("Admin creates new user with {string} username, {string} role name and given permissions:")
    public void createUserWithUsernameAndRoles(String username, String roleName, List<String> permissions) {
        ok(() -> corevanceClient.roles().retrieveAllRoles());
        PostRolesRequest newRoleRequest = new PostRolesRequest().name(Utils.randomStringGenerator(roleName, 8)).description(roleName);
        PostRolesResponse createNewRole = ok(() -> corevanceClient.roles().createRole(newRoleRequest));
        Long roleId = createNewRole.getResourceId();
        Map<String, Boolean> permissionMap = new HashMap<>();
        permissions.forEach(role -> permissionMap.put(role, true));
        PutRolesRoleIdPermissionsRequest putRolesRoleIdPermissionsRequest = new PutRolesRoleIdPermissionsRequest()
                .permissions(permissionMap);
        ok(() -> corevanceClient.roles().updateRolePermissions(roleId, putRolesRoleIdPermissionsRequest));

        String generatedUsername = Utils.randomStringGenerator(username, 8);
        PostUsersRequest postUsersRequest = new PostUsersRequest() //
                .username(generatedUsername) //
                .email(EMAIL) //
                .firstname(Utils.randomFirstNameGenerator()) //
                .lastname(Utils.randomLastNameGenerator()) //
                .sendPasswordToEmail(Boolean.FALSE) //
                .officeId(1L) //
                .password(PWD_USER_WITH_ROLE) //
                .repeatPassword(PWD_USER_WITH_ROLE) //
                .roles(List.of(roleId));

        PostUsersResponse createUserResponse = ok(() -> corevanceClient.users().createUser(postUsersRequest));
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_RESPONSE, createUserResponse);
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_USERNAME, generatedUsername);
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_PASSWORD, PWD_USER_WITH_ROLE);
    }
}
