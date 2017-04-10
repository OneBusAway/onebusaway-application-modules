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
