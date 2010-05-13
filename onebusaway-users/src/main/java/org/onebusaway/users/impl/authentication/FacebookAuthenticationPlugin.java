package org.onebusaway.users.impl.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.users.impl.authentication.AuthenticationResult.EResultCode;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.validation.SecretSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacebookAuthenticationPlugin implements AuthenticationPlugin {

  private Logger _log = LoggerFactory.getLogger(FacebookAuthenticationPlugin.class);

  private String _clientId = "";

  private String _clientSecret = "";
  
  public void setClientId(String clientId) {
    _clientId = clientId;
  }
  
  public void setClientSecret(String clientSecret) {
    _clientSecret = clientSecret;
  }

  @Override
  public void init(SecretSource secrets) {

  }

  public void handleAction(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction pluginAction)
      throws IOException, ServletException {

    String action = pluginAction.getAction();

    if (action == null || action.equals("default"))
      handleAuthentication(httpReq, httpResp, pluginAction);
    else if (action.equals("verify")) {
      handleVerification(httpReq, httpResp, pluginAction);
    }
  }

  private void handleAuthentication(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse, PluginAction action) throws IOException {

    String returnToUrl = getVerifyUrl(action);

    String authUrl = "https://graph.facebook.com/oauth/authorize?client_id="
        + _clientId + "&redirect_uri=" + returnToUrl;
    httpResponse.sendRedirect(authUrl);
  }

  private void handleVerification(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction action) throws IOException {

    String returnToUrl = getVerifyUrl(action);

    String code = httpReq.getParameter("code");
    URL url = new URL(
        "https://graph.facebook.com/oauth/access_token?client_id=" + _clientId
            + "&redirect_uri=" + returnToUrl + "&client_secret=" + _clientSecret
            + "&code=" + code);

    HttpURLConnection request = (HttpURLConnection) url.openConnection();

    // response status should be 200 OK
    int statusCode = request.getResponseCode();

    if (statusCode != 200) {
      _log.error("error connecting to " + url.toExternalForm() + " code="
          + statusCode);
      setErrorResult(httpReq, httpResp);
      return;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        request.getInputStream()));
    String line = reader.readLine();
    reader.close();

    url = new URL("https://graph.facebook.com/me?" + line);
    request = (HttpURLConnection) url.openConnection();

    if (statusCode != 200) {
      _log.error("error connecting to " + url.toExternalForm() + " code="
          + statusCode);
      setErrorResult(httpReq, httpResp);
      return;
    }

    StringBuilder b = new StringBuilder();
    reader = new BufferedReader(new InputStreamReader(request.getInputStream()));

    while ((line = reader.readLine()) != null)
      b.append(line);
    reader.close();

    try {

      JSONObject json = new JSONObject(b.toString());

      if (!json.has("id")) {
        _log.error("facebook json did not contain \"id\" property");
        setErrorResult(httpReq, httpResp);
        return;
      }

      String userId = json.getString("id");

      AuthenticationResult result = new AuthenticationResult(
          EResultCode.SUCCESS, UserIndexTypes.FACEBOOK, userId);
      LoginManager.handleResult(httpReq, httpResp, result);
      return;

    } catch (JSONException ex) {
      _log.error("json parsing exception", ex);
      setErrorResult(httpReq, httpResp);
      return;
    }
  }

  private String getVerifyUrl(PluginAction action) {
    return action.getBaseUrl() + "/" + action.getPlugin() + "/verify";
  }

  private void setErrorResult(HttpServletRequest httpReq,
      HttpServletResponse httpResp) {
    AuthenticationResult result = new AuthenticationResult(
        EResultCode.AUTHENTICATION_FAILED, UserIndexTypes.FACEBOOK);
    LoginManager.handleResult(httpReq, httpResp, result);
  }

}