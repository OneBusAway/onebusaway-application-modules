package org.onebusaway.users.impl.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;

public class IndexedUserAuthenticationProcessorFilter extends
    AuthenticationProcessingFilter {

  private String _indexTypeParameter = "j_indexType";

  public void setIndexTypeParameter(String indexTypeParameter) {
    _indexTypeParameter = indexTypeParameter;
  }

  public String getIndexTypeParameter() {
    return _indexTypeParameter;
  }

  @Override
  protected String obtainUsername(HttpServletRequest request) {
    String username = super.obtainUsername(request);
    if (username != null) {
      username = username.trim();
      if (username.length() > 0) {
        username = obtainUserIndexType(request) + "_" + username;
      }
    }
    return username;
  }

  protected String obtainUserIndexType(HttpServletRequest request) {
    return request.getParameter(_indexTypeParameter);
  }
}
