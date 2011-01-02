package org.onebusaway.presentation.impl.resources;

import java.net.URL;

public interface Resource {

  public String getExternalId();

  public String getExternalUrl();

  public URL getLocalUrl();
}
