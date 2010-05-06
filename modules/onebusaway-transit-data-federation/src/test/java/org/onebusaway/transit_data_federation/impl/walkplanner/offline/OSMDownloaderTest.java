package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import static org.junit.Assert.assertEquals;

import org.onebusaway.transit_data_federation.impl.walkplanner.offline.OSMDownloader;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.OSMDownloaderListener;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OSMDownloaderTest {
  @Test
  public void test() throws IOException {

    File cacheDir = new File("/tmp/OSMCache-Test");
    
    cacheDir.mkdirs();

    OSMDownloader downloader = new OSMDownloader();
    downloader.setCacheDirectory(cacheDir);
    downloader.setLatStep(0.04);
    downloader.setLonStep(0.04);
    downloader.setOverlap(0.001);

    CoordinateRectangle r = new CoordinateRectangle(47.64746520572399,
        -122.34752655029297, 47.70202189040304, -122.27405548095703);
    HandlerImpl handler = new HandlerImpl();
    downloader.visitRegion(r, handler);
    
    Map<String, File> tiles = handler.getTiles();
    assertEquals(6,tiles.size());
  }

  private class HandlerImpl implements OSMDownloaderListener {

    private Map<String, File> _tiles = new HashMap<String, File>();

    public Map<String, File> getTiles() {
      return _tiles;
    }

    public void handleMapTile(String key, double lat, double lon,
        File pathToMapTile) {
      _tiles.put(key, pathToMapTile);
    }

  }
}
