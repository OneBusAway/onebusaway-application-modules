package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.metrokc2gtfs.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtfs.model.MetroKCTPIPath;

import java.util.List;

public interface TPIPathModificationStrategy {
  public void modify(MetroKCPatternTimepoint patternTimepoint, List<MetroKCTPIPath> paths);
}
