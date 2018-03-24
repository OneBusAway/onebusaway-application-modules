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
package org.onebusaway.admin.model.role;

import java.util.Set;

// Role model class for RBAC. org.onebusaway.users.model.UserRole corresponds to a Role here.
public class Role {
	String name;

	// List of allowed privileges, or null for access to all privileges
	Set<Privilege> allowedPrivileges;
	
	public Set<Privilege> getAllowedPrivileges() {
		return allowedPrivileges;
	}

	public void setAllowedPrivileges(Set<Privilege> allowedPrivileges) {
		this.allowedPrivileges = allowedPrivileges;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Role(String name, Set<Privilege> allowedPrivileges) {
		this.name = name;
		this.allowedPrivileges = allowedPrivileges;
	}
	
	public boolean hasAllPrivileges() {
		return (allowedPrivileges == null);
	}
	
	
}
