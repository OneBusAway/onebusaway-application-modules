package org.onebusaway.webapp.resources;

import org.onebusaway.presentation.services.resources.WebappImport;
import org.onebusaway.presentation.services.resources.WebappSource;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryCssResource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;

public interface StopResources extends ClientBundle {

  @Source("Stop.css")
  @WebappSource("Stop.css")
  @Import( {WhereLibraryCssResource.class})
  @WebappImport( {WhereLibraryCssResource.class})
  public CssResource getCss();
}
