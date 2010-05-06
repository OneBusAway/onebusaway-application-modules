package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.kcmetro2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTPIPath;

import java.util.List;

public interface TPIPathModificationStrategy {
  public void modify(MetroKCPatternTimepoint patternTimepoint, List<MetroKCTPIPath> paths);
}
