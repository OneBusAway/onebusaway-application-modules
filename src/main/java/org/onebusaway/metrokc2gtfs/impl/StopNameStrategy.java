package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.metrokc2gtfs.model.MetroKCStop;

public interface StopNameStrategy {
  public boolean hasNameForStop(MetroKCStop stop);

  public String getNameForStop(MetroKCStop stop);
}
