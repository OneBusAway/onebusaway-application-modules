package org.onebusaway.presentation.impl.resources;

import java.net.URL;

public interface ResourceStrategy {

  public URL getSourceResourceAsLocalResource(URL sourceResource);

}
