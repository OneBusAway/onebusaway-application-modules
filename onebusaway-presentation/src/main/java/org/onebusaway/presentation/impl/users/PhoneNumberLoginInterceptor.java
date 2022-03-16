/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.impl.users;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.users.impl.PhoneNumberLibrary;
import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.UserIndexTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class PhoneNumberLoginInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;
  
  public static final String RESET_USER = PhoneNumberLoginInterceptor.class.getName() + ".resetUser";

  private IndexedUserDetailsService _indexedUserDetailsService;

  private String _phoneNumberParameterName = "phoneNumber";

  @Autowired
  public void setIndexedUserDetailsService(
      IndexedUserDetailsService indexedUserDetailsService) {
    _indexedUserDetailsService = indexedUserDetailsService;
  }
  
  public void setPhoneNumberParameterName(String phoneNumberParameterName) {
    _phoneNumberParameterName = phoneNumberParameterName;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ActionContext context = invocation.getInvocationContext();
    Map<String, Object> params = new HashMap<>();
    for (String key : context.getParameters().keySet()) {
      params.put(key, context.getParameters().get(key).getValue());
    }

    String phoneNumber = getPhoneNumber(params);

    phoneNumber = PhoneNumberLibrary.normalizePhoneNumber(phoneNumber);

    if (phoneNumber != null && phoneNumber.length() > 0) {

      UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER, phoneNumber);
      
      if( params.containsKey(RESET_USER))
        _indexedUserDetailsService.resetUserForIndexKey(key);

      // Ensure that we have authentication, even if it's anonymous
      if (!isCurrentUserLoggedInWithKey(key)) {
        
        IndexedUserDetails userDetails = _indexedUserDetailsService.getOrCreateUserForIndexKey(
            key, "", false);

        DefaultUserAuthenticationToken token = new DefaultUserAuthenticationToken(
            userDetails);
        SecurityContextHolder.getContext().setAuthentication(token);
      }
    }

    return invocation.invoke();
  }

  private String getPhoneNumber(Map<String, Object> params) {

    Object value = params.get(_phoneNumberParameterName);

    if (value == null)
      return null;

    if (value instanceof String[]) {
      String[] values = (String[]) value;
      if (values.length > 0)
        return values[0];
    } else {
      return value.toString();
    }

    return null;
  }

  private boolean isCurrentUserLoggedInWithKey(UserIndexKey key) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null)
      return false;

    Object principal = authentication.getPrincipal();

    if (!(principal instanceof IndexedUserDetails))
      return false;

    IndexedUserDetails details = (IndexedUserDetails) principal;
    UserIndexKey indexKey = details.getUserIndexKey();
    return indexKey.equals(key);
  }
}
