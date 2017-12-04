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

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.TemporaryPasswordService;
import org.onebusaway.admin.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TemporaryPasswordServiceImpl implements TemporaryPasswordService {

	private static Logger _log = LoggerFactory.getLogger(TemporaryPasswordServiceImpl.class);
	
	private static final int PASSWORD_LENGTH = 12;
	private Set<String> users = new HashSet<String>();
	
	@Autowired
	private UserManagementService _userManagementService;
	
	@Override
	public String getTemporaryPasswordForUser(String username) {
		String newpass = getTemporaryPassword();
		UserDetail detail = _userManagementService.getUserDetail(username);
		_log.warn("got userdetail for name: " + username);
		detail.setPassword(newpass);
		_userManagementService.updateUser(detail);
		setTemporaryPassword(username, true);
		return newpass;
	}

	@Override
	public boolean isTemporaryPassword(String user) {
		return users.contains(user);
	}

	@Override
	public void setTemporaryPassword(String user, boolean hasTemp) {
		if (hasTemp)
			users.add(user);
		else
			users.remove(user);
	}
	
	private SecureRandom random = new SecureRandom();
	
	private String getTemporaryPassword() {
		return RandomStringUtils.random(PASSWORD_LENGTH, 0, 0, true, true, null, random);
	}
	
	
}
