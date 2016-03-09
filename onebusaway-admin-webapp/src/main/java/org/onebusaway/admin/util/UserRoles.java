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
package org.onebusaway.admin.util;


/**
 * Holds constants for user roles in the system
 * @author abelsare
 *
 */
public enum UserRoles {

	/** Anonymous user role **/
	ROLE_ANONYMOUS("ROLE_ANONYMOUS"),
	
	/** User/Operator user role **/
	ROLE_USER("ROLE_USER"),
	
	/** Admin user role **/
	ROLE_ADMINISTRATOR("ROLE_ADMINISTRATOR"),
	
	/** Operator user role **/
	ROLE_OPERATOR("ROLE_OPERATOR"),
	
	/** Support user role **/
	ROLE_SUPPORT("ROLE_SUPPORT"),
	
	/** Reporting user role **/
	ROLE_REPORTING("ROLE_REPORTING");
	
	private String role;
	
	private UserRoles(String role) {
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}
	
	public static UserRoles fromValue(String value) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("Value cannot be null or empty!");
        } else if ("ROLE_ANONYMOUS".equals(value)) {
            return ROLE_ANONYMOUS;
        } else if ("ROLE_USER".equals(value)) {
        	return ROLE_USER;
        } else if ("ROLE_ADMINISTRATOR".equals(value)) {
        	return ROLE_ADMINISTRATOR;
		} else if ("ROLE_OPERATOR".equals(value)) {
        	return ROLE_OPERATOR;
		} else if ("ROLE_SUPPORT".equals(value)) {
        	return ROLE_SUPPORT;
		} else if ("ROLE_REPORTING".equals(value)) {
        	return ROLE_REPORTING;
		} else {
            throw new IllegalArgumentException("Cannot create enum from "
                    + value + " value!");
        }
	}
}
