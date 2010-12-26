package org.onebusaway.presentation.impl.resources;

import java.net.URL;

public class DefaultResourceStrategyImpl implements ResourceStrategy {

  @Override
  public URL getSourceResourceAsLocalResource(URL sourceResource) {
    return sourceResource;
  }
}
