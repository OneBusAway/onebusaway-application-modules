package org.onebusaway.users.impl.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.users.services.validation.SecretSource;

public interface AuthenticationPlugin {
  
  public void init(SecretSource secrets);

  public void handleAction(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction pluginAction) throws IOException,
      ServletException;
}
