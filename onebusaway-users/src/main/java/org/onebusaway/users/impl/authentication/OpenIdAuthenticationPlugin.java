package org.onebusaway.users.impl.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.users.impl.authentication.AuthenticationResult.EResultCode;
import org.onebusaway.users.services.validation.SecretSource;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;

public class OpenIdAuthenticationPlugin implements AuthenticationPlugin {

  private ConsumerManager _manager;

  public OpenIdAuthenticationPlugin() {
    try {
      _manager = new ConsumerManager();
    } catch (ConsumerException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void init(SecretSource secrets) {
    // TODO Auto-generated method stub

  }

  public void handleAction(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction pluginAction)
      throws IOException, ServletException {

    String action = pluginAction.getAction();
    if (action == null || "default".equals(action))
      handleAuthentication(httpReq, httpResp, pluginAction);
    else if ("verify".equals(action))
      handleVerification(httpReq, httpResp);

  }

  public void handleAuthentication(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction action) throws IOException,
      ServletException {

    String userSuppliedString = httpReq.getParameter("url");

    try {

      String returnToUrl = action.getBaseUrl() + "/" + action.getPlugin()
          + "/verify";

      // perform discovery on the user-supplied identifier
      List<?> discoveries = _manager.discover(userSuppliedString);

      // attempt to associate with the OpenID provider
      // and retrieve one service endpoint for authentication
      DiscoveryInformation discovered = _manager.associate(discoveries);

      // store the discovery information in the user's session
      httpReq.getSession().setAttribute("openid-disc", discovered);

      // obtain a AuthRequest message to be sent to the OpenID provider
      AuthRequest authReq = _manager.authenticate(discovered, returnToUrl);

      // Attribute Exchange example: fetching the 'email' attribute
      FetchRequest fetch = FetchRequest.createFetchRequest();
      fetch.addAttribute("email", "http://schema.openid.net/contact/email",
          false); // required

      // attach the extension to the authentication request
      authReq.addExtension(fetch);

      if (!discovered.isVersion2()) {
        // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
        // The only method supported in OpenID 1.x
        // redirect-URL usually limited ~2048 bytes
        httpResp.sendRedirect(authReq.getDestinationUrl(true));
      } else {
        // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
        String destinationUrl = authReq.getDestinationUrl(false);
        Map<?, ?> paramMap = authReq.getParameterMap();
        UtilityLibrary.writeFormPostRedirect(httpResp, destinationUrl, paramMap);
      }
    } catch (OpenIDException e) {
      // present error to the user
    }
  }

  // --- processing the authentication response ---
  public void handleVerification(HttpServletRequest httpReq,
      HttpServletResponse httpResp) {

    try {
      // extract the parameters from the authentication response
      // (which comes in as a HTTP request from the OpenID provider)
      ParameterList response = new ParameterList(httpReq.getParameterMap());

      // retrieve the previously stored discovery information
      DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute(
          "openid-disc");

      // extract the receiving URL from the HTTP request
      StringBuffer receivingURL = httpReq.getRequestURL();
      String queryString = httpReq.getQueryString();
      if (queryString != null && queryString.length() > 0)
        receivingURL.append("?").append(httpReq.getQueryString());

      // verify the response; ConsumerManager needs to be the same
      // (static) instance used to place the authentication request
      VerificationResult verification = _manager.verify(
          receivingURL.toString(), response, discovered);

      // examine the verification result and extract the verified identifier
      Identifier verified = verification.getVerifiedId();
      if (verified != null) {
        AuthenticationResult result = new AuthenticationResult(
            EResultCode.SUCCESS, "openid", verified.getIdentifier(),
            UUID.randomUUID().toString());
        LoginManager.handleResult(httpReq, httpResp, result);
        return;
      }
    } catch (OpenIDException e) {
      // present error to the user
    }

    AuthenticationResult result = new AuthenticationResult(
        EResultCode.AUTHENTICATION_FAILED, "openid");
    LoginManager.handleResult(httpReq, httpResp, result);

  }

}
