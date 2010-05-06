package org.onebusaway.presentation.impl.users;

import org.onebusaway.users.impl.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import java.util.Map;

public class PhoneNumberLoginInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

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

    // Ensure that we have authentication, even if it's anonymous
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {

      ActionContext context = invocation.getInvocationContext();
      Map<String, Object> params = context.getParameters();
      String phoneNumber = getPhoneNumber(params);

      phoneNumber = normalizePhoneNumber(phoneNumber);

      if (phoneNumber != null && phoneNumber.length() > 0) {

        UserIndexKey key = new UserIndexKey("phoneNumber", phoneNumber);

        IndexedUserDetails userDetails = _indexedUserDetailsService.getOrCreateUserForIndexKey(
            key, "");

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

  private String normalizePhoneNumber(String phoneNumber) {
    if (phoneNumber == null)
      return phoneNumber;
    return phoneNumber;
  }
}
