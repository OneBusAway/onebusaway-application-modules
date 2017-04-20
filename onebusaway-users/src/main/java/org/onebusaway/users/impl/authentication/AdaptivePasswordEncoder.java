/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.users.impl.authentication;

import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.util.Assert;

public class AdaptivePasswordEncoder {
	
	public PasswordEncoder getPasswordEncoder(Object passwordEncoder) throws IllegalArgumentException{
		 if (passwordEncoder instanceof PasswordEncoder) {
	          return (PasswordEncoder) passwordEncoder;
	     }

	     if (passwordEncoder instanceof org.springframework.security.crypto.password.PasswordEncoder) {
	          final org.springframework.security.crypto.password.PasswordEncoder delegate =
	                  (org.springframework.security.crypto.password.PasswordEncoder)passwordEncoder;
	          return new PasswordEncoder() {
	              public String encodePassword(String rawPass, Object salt) {
	                  checkSalt(salt);
	                  return delegate.encode(rawPass);
	              }

	              public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
	                  checkSalt(salt);
	                  return delegate.matches(rawPass, encPass);
	              }

	              private void checkSalt(Object salt) {
	                  Assert.isNull(salt, "Salt value must be null when used with crypto module PasswordEncoder");
	              }
	          };
	     }

	     throw new IllegalArgumentException("passwordEncoder must be a PasswordEncoder instance");
	}
	
}
