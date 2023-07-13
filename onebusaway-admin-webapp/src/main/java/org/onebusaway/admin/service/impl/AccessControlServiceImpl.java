/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.onebusaway.admin.model.role.Privilege;
import org.onebusaway.admin.model.role.Role;
import org.onebusaway.admin.service.AccessControlService;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessControlServiceImpl implements AccessControlService {

	private static Logger _log = LoggerFactory.getLogger(AccessControlServiceImpl.class);
		
	private Map<String, Role> roleByName;
	
	@Autowired
	private ConfigurationServiceClient _configurationServiceClient;
	
	@Autowired
	private CurrentUserService currentUserService;
	
	private static final String privilegeComponent = "privilege";

	@Override
	public boolean userHasPrivilege(User user, String privilege) {		
		return userHasPrivilege(user, new Privilege(privilege));
	}		

	@Override
	public boolean userHasPrivilege(User user, Privilege privilege) {
		if (user == null) { // anonymous user
			Role role = roleByName.get(StandardAuthoritiesService.ANONYMOUS);
			boolean allowed = roleHasPrivilege(role, privilege);
			if (!allowed) {
				_log.warn("user " + user + " denied " + privilege);
			}
			return allowed;
		}
		
		Set<UserRole> roles = user.getRoles();
		for (UserRole userRole : roles) {
			
			Role role = roleByName.get(userRole.getName());
			if (role == null)
				_log.warn("No privileges found for role " + userRole.getName());
			
			else if (role.hasAllPrivileges() || 
					(privilege != null && roleHasPrivilege(role, privilege))) {
				_log.debug(userRole.getName() + " has privileges for " + privilege.getName());
				return true;
			}
		}
		
		_log.warn("Auth failed for " + user + ", " + privilege);
		return false;
	}
	
	@Override
	public boolean currentUserHasPrivilege(String privilege) {
		return currentUserHasPrivilege(new Privilege(privilege));
	}

	@Override
	public boolean currentUserHasPrivilege(Privilege privilege) {
		UserIndex idx = currentUserService.getCurrentUserAsUserIndex();
		User user = (idx == null) ? null : idx.getUser();
		return userHasPrivilege(user, privilege);
	}
	
	private boolean roleHasPrivilege(Role role, Privilege privilege) {
		if (role == null) return false;
		Set<Privilege> allowed = role.getAllowedPrivileges();
		boolean authorized = allowed != null && allowed.contains(privilege);
		if (!authorized) {
		  _log.warn("Auth failed for " + role + ", " + privilege);
		}
		return authorized;
	}
	
	@PostConstruct
	private void init() throws Exception {
		
		roleByName = new ConcurrentHashMap<String, Role>();
		List<Map<String, String>> settings = null;
		try {
			settings = _configurationServiceClient.getItems("config", "list");
		} catch (Exception any) {
			final String msg = "ERROR: configuration service not configured";
			System.out.println(msg);
			_log.error(msg);
		}
		
		if (settings == null) {
			final String msg = "ERROR:  roles not configured.  Defaulting ADMIN";
			System.out.println(msg);
			_log.error(msg);
			settings = createDefaultAdminConfig();
		}
		
		for (Map<String, String> setting : settings) {
			
  			if ((setting.containsKey("component") && 
  					privilegeComponent.equals(setting.get("component"))) &&
  				setting.containsKey("key")) {
  				
  				String roleName = setting.get("key");
  				String privListString = setting.get("value");
  		
  				if (privListString.equals("*")) {
  					Role role = new Role(roleName, null);
  	  				roleByName.put(roleName, role);
  	  				continue;
  				}
  				
  				Set<Privilege> privileges = new HashSet<Privilege>();
  				for (String privName : privListString.split(",")) {
  					Privilege priv = new Privilege(privName);
  					privileges.add(priv);
  				}
  				
  				Role role = new Role(roleName, privileges);
  				roleByName.put(roleName, role);
  			}
		}
	}

	private List<Map<String, String>> createDefaultAdminConfig() {
		List<Map<String, String>> items = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		map.put("component", "privilege");
		map.put("key", "ROLE_ADMINISTRATOR");
		map.put("value", "*");
		items.add(map);
		return items;
	}

}
