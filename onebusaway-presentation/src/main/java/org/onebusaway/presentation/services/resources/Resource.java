package org.onebusaway.presentation.services.resources;

import java.net.URL;

public interface Resource {

  public String getExternalId();

  public String getExternalUrl();

  public URL getLocalUrl();
  
  public long getContentLength();
  
  public long getLastModifiedTime();
}
