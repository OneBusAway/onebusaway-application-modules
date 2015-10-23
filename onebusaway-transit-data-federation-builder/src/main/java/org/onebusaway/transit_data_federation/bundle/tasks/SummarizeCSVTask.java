package org.onebusaway.transit_data_federation.bundle.tasks;

import org.springframework.beans.factory.annotation.Autowired;

public class SummarizeCSVTask implements Runnable {

  @Autowired
  private MultiCSVLogger logger;
  
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }
      
  @Override
  public void run() {
    logger.summarize();
  }

}
