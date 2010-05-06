package org.onebusaway.users.impl.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.users.impl.authentication.AuthenticationResult.EResultCode;

public class LoginManager {

  private static final String KEY_TARGET = LoginManager.class.getName()
      + ".target";

  private static final String KEY_RESULT = LoginManager.class.getName()
      + ".result";

  private Map<String, AuthenticationPlugin> _plugins = new HashMap<String, AuthenticationPlugin>();

  public void setPlugins(Map<String, AuthenticationPlugin> plugins) {
    _plugins.putAll(plugins);
  }

  public void handleAuthentication(HttpServletRequest httpReq,
      HttpServletResponse httpResp) throws IOException, ServletException {

    PluginAction pluginAction = getPluginAction(httpReq);

    if (pluginAction.getPlugin() == null)
      pluginAction.setPlugin(httpReq.getParameter("plugin"));
    if (pluginAction.getAction() == null)
      pluginAction.setAction("default");

    AuthenticationPlugin plugin = _plugins.get(pluginAction.getPlugin());

    String target = httpReq.getParameter("target");
    if (target != null)
      httpReq.getSession().setAttribute(KEY_TARGET, target);

    if (plugin != null) {

      plugin.handleAction(httpReq, httpResp, pluginAction);
    } else {
      AuthenticationResult result = new AuthenticationResult(
          EResultCode.NO_SUCH_PROVIDER);
      handleResult(httpReq, httpResp, result);
    }
  }

  public static void handleResult(HttpServletRequest httpReq,
      HttpServletResponse httpResp, AuthenticationResult result) {

    try {
      httpReq.getSession().setAttribute(KEY_RESULT, result);
      String target = (String) httpReq.getSession().getAttribute(KEY_TARGET);
      if (target != null)
        httpResp.sendRedirect(target);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static AuthenticationResult getResult(HttpServletRequest httpReq) {
    return (AuthenticationResult) httpReq.getSession().getAttribute(KEY_RESULT);
  }

  private PluginAction getPluginAction(HttpServletRequest request) {

    String baseUrl = request.getRequestURL().toString();

    String pathInfo = request.getPathInfo();

    if (pathInfo != null && baseUrl.endsWith(pathInfo))
      baseUrl = baseUrl.substring(0, baseUrl.length() - pathInfo.length());

    PluginAction action = new PluginAction();

    action.setBaseUrl(baseUrl);

    if (pathInfo == null)
      return action;

    // Strip any leading or trailing '/' characters
    while (pathInfo.startsWith("/"))
      pathInfo = pathInfo.substring(1);
    while (pathInfo.endsWith("/"))
      pathInfo = pathInfo.substring(0, pathInfo.length() - 1);

    if (pathInfo.length() == 0)
      return action;

    String[] tokens = pathInfo.split("/+");

    if (tokens.length > 0)
      action.setPlugin(tokens[0]);
    if (tokens.length > 1)
      action.setAction(tokens[1]);

    return action;
  }
}
