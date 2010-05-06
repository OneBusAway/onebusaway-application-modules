package org.onebusaway.webapp.resources;

import org.onebusaway.webapp.services.resources.CssResource;
import org.onebusaway.webapp.services.resources.DataResource;
import org.onebusaway.webapp.services.resources.ImmutableResourceBundle;

public interface WhereStandardResources extends ImmutableResourceBundle {
  
  @Resource("/css/where/standard.css")
  public CssResource getCss();
  
  @Resource("/images/Snow.png")
  public DataResource getSnowImage();
}
