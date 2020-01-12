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
package org.onebusaway.admin.service;

import java.util.List;

import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.users.model.User;

/**
 * Service interface for CRUD operations on system user.
 * @author abelsare
 *
 */
public interface UserManagementService {
	
	/**
	 * Returns a list of user names that contain the given search string
	 * @param searchString the search string 
	 * @return list of matching user names that contains the given search string
	 */
	List<String> getUserNames(String searchTerm);

	/**
	 * Returns the number of user details (name, role) for every user in the database
	 * @return long number (count) of user details
	 */
	Integer getUserDetailsCount();

	/**
	 * Returns a list of user details (name, role) for maxResults of users staring with 'start'
	 * @return list of userDetails
	 */
	List<UserDetail> getUserDetails(final int start, final int maxResults);

	/**
	 * Returns a list of user details (name, role) for every enabled user in the database
     * An enabled user is a user where the property isDisabled=false
	 * @return list of userDetails
	 */
	List<UserDetail> getActiveUsersDetails();

    /**
     * Returns a list of user details (name, role) for every disabled user in the database
     * An disabled user is a user where the property isDisabled=true
     * @return list of userDetails
     */
    List<UserDetail> getInactiveUsersDetails();

	/**
	 * Fetches the user details such as user name, user role of the given user
	 * @param userName user whose details are desired
	 * @return user details of the desired user
	 */
	UserDetail getUserDetail(String userName);
	
	/**
	 * Disables operator role for the given user. A user can either be an admin or operator but not 
	 * both.
	 * @param user system user
	 */
	void disableOperatorRole(User user);
	
	/**
	 * Creates a user in the system with given user name and password. Assigns admin role to the created
	 * user if admin flag is set
	 * @param userName user name of the new user
	 * @param password password of the new user
	 * @param admin true if user beign created is admin
	 * @return true if user creation is successful, false otherwise
	 */
	boolean createUser(String userName, String password, boolean admin);
	
	/**
	 * Creates a user in the system with given user name and password. Assigns admin role to the created
	 * user if admin flag is set
	 * @param userName user name of the new user
	 * @param password password of the new user
	 * @param role role of the new user
	 * @return true if user creation is successful, false otherwise
	 */
	boolean createUser(String userName, String password, String role);
	
	/**
	 * Updates user in the system with new password and role. Only updates the user record if it is found in the
	 * system.
	 * @param userDetail userDetails such as username, password etc
	 * @return true if update operation succeeds, false otherwise
	 */
	boolean updateUser(UserDetail userDetail);
	
	/**
	 * Deactivates/soft deletes a given user.  This action is un-reversible.
     * Called "delete" in the UI
	 * @param userDetail userDetails such as id, username etc
	 * @return true if soft delete operation succeeds, false otherwise
	 */
	boolean deactivateUser(UserDetail userDetail);

    /**
     * Inactivates a given user.  User can be activated again.
     * @param userDetail userDetails such as id, username etc
     * @return true if setting isDisabled property succeeds, false otherwise
     */
    boolean inactivateUser(UserDetail userDetail);

    /**
     * Activates a given user that has been deactivated
     * @param userDetail userDetails such as id, username etc
     * @return true if setting isDisabled property succeeds, false otherwise
     */
    boolean activateUser(UserDetail userDetail);

	List<String> getAllRoleNames();

    List<String> getManagedRoleNames();
}
