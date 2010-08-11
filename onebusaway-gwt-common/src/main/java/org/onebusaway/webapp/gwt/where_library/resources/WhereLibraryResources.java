package org.onebusaway.webapp.gwt.where_library.resources;

import org.onebusaway.presentation.services.resources.WebappSource;

import com.google.gwt.resources.client.ClientBundle;

public interface WhereLibraryResources extends ClientBundle {
  
  @Source("WhereLibrary.css")
  @WebappSource("classpath:WhereLibrary.css")
  public WhereLibraryCssResource getCss();
  
  @Source("WhereLibraryStandardStop.css")
  @WebappSource("classpath:WhereLibraryStandardStop.css")
  public WhereLibraryStandardStopCssResource getStandardStopCss();
}
