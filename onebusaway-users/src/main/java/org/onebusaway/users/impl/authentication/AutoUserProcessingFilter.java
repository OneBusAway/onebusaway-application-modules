package org.onebusaway.users.impl.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.users.impl.PrincipalFactory;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.AuthenticationDetailsSource;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;
import org.springframework.security.ui.WebAuthenticationDetailsSource;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.Assert;

/**
 * Automatically creates a new web user if the current web session does not have
 * an authenticated user
 * 
 * @author bdferris
 * 
 */
public class AutoUserProcessingFilter extends SpringSecurityFilter {

  private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();

  private String key = "AutoUserProcessingFilter";

  private TokenBasedRememberMeServices _rememberMeServices;

  private IndexedUserDetailsService _userDetailsService;

  private List<String> _pathsToExclude = new ArrayList<String>();

  public void afterPropertiesSet() throws Exception {
    Assert.hasLength(key);
  }

  @Autowired
  public void setRememberMeServices(
      TokenBasedRememberMeServices rememberMeServices) {
    _rememberMeServices = rememberMeServices;
  }

  @Autowired
  public void setUserDetailsService(IndexedUserDetailsService userDetailsService) {
    _userDetailsService = userDetailsService;
  }

  public void setPathsToExclude(List<String> pathsToExclude) {
    _pathsToExclude = pathsToExclude;
  }

  protected Authentication createAuthentication(HttpServletRequest request) {

    UserIndexKey principal = PrincipalFactory.createPrincipal();
    UUID credentials = UUID.randomUUID();

    IndexedUserDetails details = _userDetailsService.getOrCreateUserForIndexKey(
        principal, credentials.toString(), true);

    AutoUserAuthenticationToken auth = new AutoUserAuthenticationToken(key,
        details.getUsername(), details.getPassword(), details.getAuthorities());
    auth.setDetails(authenticationDetailsSource.buildDetails((HttpServletRequest) request));
    auth.setDetails(details);
    return auth;
  }

  @Override
  protected void doFilterHttp(HttpServletRequest request,
      HttpServletResponse response, FilterChain chain) throws IOException,
      ServletException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null && ! isPathExcluded(request)) {

      authentication = createAuthentication(request);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      _rememberMeServices.onLoginSuccess(request, response, authentication);

      if (logger.isDebugEnabled()) {
        logger.debug("Populated SecurityContextHolder with anonymous token: '"
            + authentication + "'");
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("SecurityContextHolder not populated with anonymous token, as it already contained: '"
            + authentication + "'");
      }
    }

    chain.doFilter(request, response);
  }

  public int getOrder() {
    return FilterChainOrder.ANONYMOUS_FILTER;
  }

  public String getKey() {
    return key;
  }

  public void setAuthenticationDetailsSource(
      AuthenticationDetailsSource authenticationDetailsSource) {
    Assert.notNull(authenticationDetailsSource,
        "AuthenticationDetailsSource required");
    this.authenticationDetailsSource = authenticationDetailsSource;
  }

  public void setKey(String key) {
    this.key = key;
  }

  private boolean isPathExcluded(HttpServletRequest request) {

    String path = request.getServletPath();
    if (path == null || path.length() == 0)
      return false;
    
    for (String toExclude : _pathsToExclude) {
      if (path.startsWith(toExclude))
        return true;
    }

    return false;
  }
}
