package org.onebusaway.webapp.gwt.login_widget;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LoginWidgetResources extends ClientBundle {

  @Source("Provider-AOL.png")
  ImageResource providerAOL();
  
  @Source("Provider-Facebook.png")
  ImageResource providerFacebook();

  @Source("Provider-Google.png")
  ImageResource providerGoogle();

  @Source("Provider-OpenID.png")
  ImageResource providerOpenID();

  @Source("Provider-Twitter.png")
  ImageResource providerTwitter();

  @Source("Provider-Yahoo.png")
  ImageResource providerYahoo();
}
