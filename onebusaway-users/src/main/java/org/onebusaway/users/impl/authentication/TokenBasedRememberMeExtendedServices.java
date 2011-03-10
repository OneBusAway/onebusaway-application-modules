package org.onebusaway.users.impl.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.userdetails.UserDetails;

/**
 * Extension of {@link TokenBasedRememberMeServices} that deals with the fact
 * that the cookie token delimiter might be present in usernames
 * 
 * @author bdferris
 */
public class TokenBasedRememberMeExtendedServices extends
    TokenBasedRememberMeServices {

  @Override
  public UserDetails processAutoLoginCookie(String[] cookieTokens,
      HttpServletRequest request, HttpServletResponse response) {

    if (cookieTokens != null && cookieTokens.length > 3) {
      int n = cookieTokens.length;

      String a = cookieTokens[0];
      for (int i = 1; i < n - 2; i++)
        a += ":" + cookieTokens[i];
      String[] updated = {a, cookieTokens[n - 2], cookieTokens[n - 1]};
      cookieTokens = updated;
    }

    return super.processAutoLoginCookie(cookieTokens, request, response);
  }
}
