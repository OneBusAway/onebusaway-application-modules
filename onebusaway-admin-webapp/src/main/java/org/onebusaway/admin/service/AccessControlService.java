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
package org.onebusaway.admin.service;

import org.onebusaway.admin.model.role.Privilege;
import org.onebusaway.users.model.User;

// implements RBAC. Maps Roles to Privileges and allows for checking whether a user has a privilege.
public interface AccessControlService {

	public boolean userHasPrivilege(User user, String privilege);
	
	public boolean userHasPrivilege(User user, Privilege privilege);

	public boolean currentUserHasPrivilege(String privilege);
	
	public boolean currentUserHasPrivilege(Privilege privilege);
}
