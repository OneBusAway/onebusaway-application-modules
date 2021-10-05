/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.isA;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.users.impl.authentication.VersionedPasswordEncoder;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserService;

/**
 * Tests {@link UserManagementServiceImpl}
 * @author abelsare
 *
 */
public class UserManagementServiceImplTest {

	@Mock
	private UserService userService;
	
	@Mock
	private User user;
	
	@Mock
	private StandardAuthoritiesService authoritiesService;
	
	@Mock
	private UserDao userDao;
	
	@Mock
	private VersionedPasswordEncoder passwordEncoder;
	
	private UserManagementServiceImpl service;
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		service = new UserManagementServiceImpl();
		service.setUserService(userService);
		service.setUserDao(userDao);
		service.setAuthoritiesService(authoritiesService);
		service.setPasswordEncoder(passwordEncoder);
		
	}
	
	@Test
	public void testCreateUser() {
		UserIndex userIndex = mock(UserIndex.class);
		when(userService.getOrCreateUserForUsernameAndPassword("operator", "password")).thenReturn(userIndex);
		
		boolean success = service.createUser("operator", "password", false);
		
		assertTrue("Expecting user to be created successfully", success);
		
		verify(userService).getOrCreateUserForUsernameAndPassword("operator", "password");
		verify(userService, times(0)).enableAdminRoleForUser(isA(User.class), isA(Boolean.class));
	}
	
	@Test
	public void testCreateAdmin() {
		UserIndex userIndex = mock(UserIndex.class);
		UserRole role = mock(UserRole.class);
		
		Set<UserRole> roles = new HashSet<UserRole>();
		roles.add(role);
		
		when(userService.getOrCreateUserForUsernameAndPassword("admin", "password")).thenReturn(userIndex);
		when(userIndex.getUser()).thenReturn(user);
		when(authoritiesService.getUserRoleForName(StandardAuthoritiesService.USER)).thenReturn(role);
		when(user.getRoles()).thenReturn(roles);
		
		boolean success = service.createUser("admin", "password", true);
		
		assertTrue("Expecting user to be created successfully", success);
		
		verify(userService).getOrCreateUserForUsernameAndPassword("admin", "password");
		verify(userService).enableAdminRoleForUser(user, false);
		verify(userDao).saveOrUpdateUser(user);
	}
	
	@Test
	public void testUpdateNonExistingUser() {
		UserDetail userDetail = mock(UserDetail.class);
		
		when(userDetail.getId()).thenReturn(1);
		when(userDetail.getUsername()).thenReturn("admin2");
		
		when(userDao.getUserForId(1)).thenReturn(null);
		
		boolean success = service.updateUser(userDetail);
		
		assertFalse("Unable to update non existing user", success);
		
		verify(passwordEncoder, times(0)).encode("password");
		verify(userDao, times(0)).saveOrUpdateUser(user);
	}
	
	@Test
	public void testUpdateUser() {
		String credentials = "encryptedPassword";
		Integer userId = 1;
		
		UserDetail userDetail = mock(UserDetail.class);
		UserIndex userIndex = mock(UserIndex.class);
		UserRole userRole = mock(UserRole.class);
		UserRole adminRole = mock(UserRole.class);
		
		Set<UserIndex> userIndices = new HashSet<UserIndex>();
		userIndices.add(userIndex);
		
		Set<UserRole> userRoles = new HashSet<UserRole>();
		userRoles.add(userRole);
		
		when(userRole.getName()).thenReturn("ROLE_USER");
		
		buildUserDetail(userId, userDetail, "password");
		
		when(userService.getUserForId(userId)).thenReturn(user);
		
		when(passwordEncoder.encode("password")).thenReturn(credentials);
		
		when(user.getUserIndices()).thenReturn(userIndices);
		when(user.getRoles()).thenReturn(userRoles);
		
		when(authoritiesService.getUserRoleForName("ROLE_ADMINISTRATOR")).thenReturn(adminRole);
		
		boolean success = service.updateUser(userDetail);
		
		assertTrue("User updated successfully", success);

		verify(passwordEncoder).encodePassword("password", "admin");
		verify(authoritiesService).getUserRoleForName("ROLE_ADMINISTRATOR");
		verify(userDao).saveOrUpdateUser(user);
		
	}

	private void buildUserDetail(Integer userId, UserDetail userDetail, String password) {
		when(userDetail.getId()).thenReturn(userId);
		when(userDetail.getUsername()).thenReturn("admin");
		when(userDetail.getPassword()).thenReturn(password);
		when(userDetail.getRole()).thenReturn("ROLE_ADMINISTRATOR");
	}
	
	@Test
	public void testUpdatePassword() {
		String credentials = "encryptedPassword";
		Integer userId = 1;
		
		UserDetail userDetail = mock(UserDetail.class);
		UserIndex userIndex = mock(UserIndex.class);
		UserRole userRole = mock(UserRole.class);
		
		Set<UserIndex> userIndices = new HashSet<UserIndex>();
		userIndices.add(userIndex);
		
		Set<UserRole> userRoles = new HashSet<UserRole>();
		userRoles.add(userRole);
		
		when(userRole.getName()).thenReturn("ROLE_ADMINISTRATOR");
		
		buildUserDetail(userId, userDetail,"password");
		
		when(userService.getUserForId(userId)).thenReturn(user);
		
		when(passwordEncoder.encode("password")).thenReturn(credentials);
		
		when(user.getUserIndices()).thenReturn(userIndices);
		when(user.getRoles()).thenReturn(userRoles);
		
		boolean success = service.updateUser(userDetail);
		
		assertTrue("User's password updated successfully", success);
		
		verify(passwordEncoder).encodePassword("password", "admin");
		verify(authoritiesService, times(0)).getAdministratorRole();
		verify(userDao).saveOrUpdateUser(user);
		
	}
	
	@Test
	public void testUpdateUserRole() {
		Integer userId = 1;
		
		UserDetail userDetail = mock(UserDetail.class);
		UserIndex userIndex = mock(UserIndex.class);
		UserRole userRole = mock(UserRole.class);
		
		Set<UserIndex> userIndices = new HashSet<UserIndex>();
		userIndices.add(userIndex);
		
		Set<UserRole> userRoles = new HashSet<UserRole>();
		userRoles.add(userRole);
		
		when(userRole.getName()).thenReturn("ROLE_USER");
		
		buildUserDetail(userId, userDetail, "");
		
		when(userService.getUserForId(userId)).thenReturn(user);
		
		when(user.getUserIndices()).thenReturn(userIndices);
		when(user.getRoles()).thenReturn(userRoles);
		
		boolean success = service.updateUser(userDetail);
		
		assertTrue("User's password updated successfully", success);
		
		verify(passwordEncoder, times(0)).encode("");
		verify(authoritiesService).getUserRoleForName("ROLE_ADMINISTRATOR");
		verify(userDao).saveOrUpdateUser(user);
		
	}
	
	@Test
	public void testDeactivateNonExistingUser() {
		UserDetail userDetail = mock(UserDetail.class);
		
		when(userDetail.getId()).thenReturn(1);
		when(userDetail.getUsername()).thenReturn("admin2");
		
		when(userDao.getUserForId(1)).thenReturn(null);
		
		boolean success = service.deactivateUser(userDetail);
		
		assertFalse("Unable to delete non existing user", success);
		
		verify(userDao, times(0)).deleteUserIndex(isA(UserIndex.class));
		verify(userDao, times(0)).saveOrUpdateUser(user);
	}
	
	@Test
	public void testDeactivateUser() {
		Integer userId = 1;
		
		UserDetail userDetail = mock(UserDetail.class);
		buildUserDetail(userId, userDetail, "password");
		
		UserIndex userIndex = mock(UserIndex.class);
		
		Set<UserIndex> userIndices = new HashSet<UserIndex>();
		userIndices.add(userIndex);
		
		when(userService.getUserForId(userId)).thenReturn(user);
		when(user.getUserIndices()).thenReturn(userIndices);
		
		boolean success = service.deactivateUser(userDetail);
		
		assertTrue("User deactivated successfully", success);
		
		verify(userDao).deleteUserIndex(isA(UserIndex.class));
		verify(userDao).saveOrUpdateUser(user);
	}
	

}
