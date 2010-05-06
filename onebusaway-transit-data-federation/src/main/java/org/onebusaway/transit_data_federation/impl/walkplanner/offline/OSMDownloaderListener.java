package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import java.io.File;

public interface OSMDownloaderListener {
  public void handleMapTile(String key, double lat, double lon,
      File pathToMapTile);
}
