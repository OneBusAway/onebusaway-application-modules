package org.onebusaway.where.web.standard.resources;

import org.onebusaway.common.web.gwt.resources.CssResource;
import org.onebusaway.common.web.gwt.resources.DataResource;
import org.onebusaway.common.web.gwt.resources.ImmutableResourceBundle;

public interface WhereStandardResources extends ImmutableResourceBundle {
  
  @Resource("/css/where/standard.css")
  public CssResource getCss();
  
  @Resource("/images/Snow.png")
  public DataResource getSnowImage();
}
