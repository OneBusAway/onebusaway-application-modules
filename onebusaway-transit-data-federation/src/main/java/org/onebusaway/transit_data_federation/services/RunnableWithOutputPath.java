package org.onebusaway.transit_data_federation.services;

import java.io.File;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;

/**
 * Extension of {@link Runnable} that adds an output path for the task to be
 * run. Used by a number of tasks in the
 * {@link FederatedTransitDataBundleCreator} pipeline.
 * 
 * @author bdferris
 */
public interface RunnableWithOutputPath extends Runnable {
  /**
   * @param path the output path for the task
   */
  public void setOutputPath(File path);
}
