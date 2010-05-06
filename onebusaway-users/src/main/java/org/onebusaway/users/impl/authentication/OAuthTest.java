package org.onebusaway.users.impl.authentication;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthException;

public class OAuthTest {

  private static String _consumerKey = "6NW3xfaLuqH9SQNmK1Erlg";

  private static String _consumerSecret = "aZajXvYWswF8DvZV7znFAgFN2zRLVEpupQwDBJsxTM";

  public static void main(String[] args) throws OAuthException {

    OAuthConsumer consumer = new DefaultOAuthConsumer(_consumerKey,
        _consumerSecret);

    OAuthProvider provider = new DefaultOAuthProvider(
        "http://twitter.com/oauth/request_token",
        "http://twitter.com/oauth/access_token",
        "http://twitter.com/oauth/authorize");

    String authUrl = provider.retrieveRequestToken(consumer,
        "http://localhost:8080/onebusaway");

    System.out.println(authUrl);

  }
}
