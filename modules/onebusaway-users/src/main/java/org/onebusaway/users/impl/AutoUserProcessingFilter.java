package org.onebusaway.users.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.AuthenticationDetailsSource;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;
import org.springframework.security.ui.WebAuthenticationDetailsSource;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AutoUserProcessingFilter extends SpringSecurityFilter {

  private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();

  private String key = "AutoUserProcessingFilter";

  private TokenBasedRememberMeServices _rememberMeServices;

  private UserDetailsService _userDetailsService;

  public void afterPropertiesSet() throws Exception {
    Assert.hasLength(key);
  }

  @Autowired
  public void setRememberMeServices(
      TokenBasedRememberMeServices rememberMeServices) {
    _rememberMeServices = rememberMeServices;
  }
  
  @Autowired
  public void setUserDetailsService(UserDetailsService userDetailsService) {
    _userDetailsService = userDetailsService;
  }

  protected Authentication createAuthentication(HttpServletRequest request) {
    
    String principal = PrincipalFactory.createPrincipal();
    UserDetails details = _userDetailsService.loadUserByUsername(principal);

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

    if (authentication == null) {
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
}
