package org.onebusaway.gtfs_diff.services;

import org.onebusaway.gtfs_diff.model.GtfsDifferences;

public interface GtfsDifferencesSerializationService {
  public void serializeDifferences(GtfsDifferences differences);
}
