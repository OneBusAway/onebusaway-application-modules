package org.onebusaway.users.impl.authentication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.commons.digester.Digester;
import org.onebusaway.users.impl.authentication.AuthenticationResult.EResultCode;
import org.onebusaway.users.impl.authentication.twitter.TwitterUser;
import org.onebusaway.users.services.validation.SecretSource;
import org.xml.sax.SAXException;

public class TwitterAuthenticationPlugin implements AuthenticationPlugin {

  private static final String CONSUMER = TwitterAuthenticationPlugin.class.getName()
      + ".consumer";

  private static final String PROVIDER = TwitterAuthenticationPlugin.class.getName()
      + ".provider";

  private String _consumerKey = "";

  private String _consumerSecret = "";

  public void setConsumerKey(String consumerKey) {
    _consumerKey = consumerKey;
  }

  public void setConsumerSecret(String consumerSecret) {
    _consumerSecret = consumerSecret;
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

    OAuthConsumer consumer = new DefaultOAuthConsumer(_consumerKey,
        _consumerSecret);

    OAuthProvider provider = new DefaultOAuthProvider(
        "http://twitter.com/oauth/request_token",
        "http://twitter.com/oauth/access_token",
        "http://twitter.com/oauth/authenticate");

    httpRequest.getSession().setAttribute(CONSUMER, consumer);
    httpRequest.getSession().setAttribute(PROVIDER, provider);

    String returnToUrl = action.getBaseUrl() + "/" + action.getPlugin()
        + "/verify";

    try {
      String authUrl = provider.retrieveRequestToken(consumer, returnToUrl);
      httpResponse.sendRedirect(authUrl);
    } catch (OAuthException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void handleVerification(HttpServletRequest httpReq,
      HttpServletResponse httpResp, PluginAction pluginAction)
      throws IOException {

    OAuthConsumer consumer = (OAuthConsumer) httpReq.getSession().getAttribute(
        CONSUMER);
    OAuthProvider provider = (OAuthProvider) httpReq.getSession().getAttribute(
        PROVIDER);

    // String oauthToken = httpReq.getParameter("oauth_token");
    String oauthVerifier = httpReq.getParameter("oauth_verifier");

    try {
      provider.retrieveAccessToken(consumer, oauthVerifier);

      String token = consumer.getToken();
      String tokenSecret = consumer.getTokenSecret();

      // if not yet done, load the token and token secret for
      // the current user and set them
      consumer.setTokenWithSecret(token, tokenSecret);

      // create a request that requires authentication
      URL url = new URL(
          "http://api.twitter.com/1/account/verify_credentials.xml");
      HttpURLConnection request = (HttpURLConnection) url.openConnection();

      // sign the request
      consumer.sign(request);

      // send the request
      request.connect();

      // response status should be 200 OK
      int statusCode = request.getResponseCode();

      if (statusCode == 200) {
        Digester digester = new Digester();
        TwitterUser user = new TwitterUser();
        digester.push(user);
        digester.addBeanPropertySetter("user/id");
        digester.addBeanPropertySetter("user/name");
        digester.addBeanPropertySetter("user/screen_name", "screenName");
        digester.parse(request.getInputStream());

        if (user.getId() != null) {
          AuthenticationResult result = new AuthenticationResult(
              EResultCode.SUCCESS, "twitter", user.getId(),
              UUID.randomUUID().toString());
          LoginManager.handleResult(httpReq, httpResp, result);
          return;
        }
      }
    } catch (OAuthNotAuthorizedException ex) {
      // We pass this through
    } catch (OAuthException ex) {
      throw new IllegalStateException(ex);
    } catch (SAXException ex) {
      throw new IOException(ex);
    }

    AuthenticationResult result = new AuthenticationResult(
        EResultCode.AUTHENTICATION_FAILED, "twitter");
    LoginManager.handleResult(httpReq, httpResp, result);
  }

}