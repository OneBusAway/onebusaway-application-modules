package org.onebusaway.transit_data_federation.services;

import java.io.File;

public interface RunnableWithOutputPath extends Runnable {
  public void setOutputPath(File path);
}
