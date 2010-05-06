package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;

public interface StopNameStrategy {
  public boolean hasNameForStop(MetroKCStop stop);

  public String getNameForStop(MetroKCStop stop);
}
