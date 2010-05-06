package org.onebusaway.testing;

import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestData {

  public static final String CALTRAIN_GTFS = "org/onebusaway/testing/caltrain_20090308_1937.zip";

  public static final String CALTRAIN_DATABASE = "/testdata/CaltrainDatabase.xml.gz";

  public static final String CALTRAIN_DATABASE_EXTENDED = "/testdata/CaltrainDatabaseExtended.xml.gz";

  public static final String ISLAND_GTFS = "org/onebusaway/testing/island-transit_20090312_0314.zip";

  public static final String JEFFERSON_GTFS = "org/onebusaway/testing/jefferson-transit-authority_20090216_0507.zip";

  public static File getCaltrainGtfs() {
    return getResourceAsTemporaryFile(CALTRAIN_GTFS);
  }

  public static File getCaltrainStopSearchIndex(ApplicationContext context) {
    return getStopSearchIndex(context, "Caltrain");
  }

  public static File getCaltrainRouteCollectionSearchIndex(
      ApplicationContext context) {
    return getRouteCollectionSearchIndex(context, "Caltrain");
  }

  public static File getIslandGtfs() {
    return getResourceAsTemporaryFile(ISLAND_GTFS);
  }

  public static File getPortGtfs() {
    return getResourceAsTemporaryFile(JEFFERSON_GTFS);
  }

  public static final String ISLAND_AND_PORT_DATABASE = "/testdata/IslandAndPortDatabase.xml.gz";

  public static final String ISLAND_AND_PORT_DATABASE_EXTENDED = "/testdata/IslandAndPortDatabaseExtended.xml.gz";

  public static File getIslandAndPortStopSearchIndex(ApplicationContext context) {
    return getStopSearchIndex(context, "IslandAndPort");
  }

  public static File getIslandAndPortRouteCollectionSearchIndex(
      ApplicationContext context) {
    return getRouteCollectionSearchIndex(context, "IslandAndPort");
  }

  private static File getStopSearchIndex(ApplicationContext context, String key) {
    return getResourceAsFile(context, "/testdata/" + key + "-StopSearchIndex");
  }

  private static File getRouteCollectionSearchIndex(ApplicationContext context,
      String key) {
    return getResourceAsFile(context, "/testdata/" + key
        + "-RouteCollectionSearchIndex");
  }

  public static File getResourceAsFile(ApplicationContext context, String path) {
    try {
      return context.getResource(path).getFile();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static File getResourceAsTemporaryFile(String path) {
    try {

      ClassLoader loader = TestData.class.getClassLoader();
      InputStream in = loader.getResourceAsStream(path);

      if (in == null)
        throw new IllegalStateException("uknown classpath resource: " + path);

      File tmpFile = File.createTempFile("Tmp-" + path.replace('/', '_'),
          ".file");
      tmpFile.deleteOnExit();
      FileOutputStream out = new FileOutputStream(tmpFile);

      byte[] buffer = new byte[1024];
      while (true) {
        int rc = in.read(buffer);
        if (rc <= 0)
          break;
        out.write(buffer, 0, rc);
      }
      in.close();
      out.close();
      return tmpFile;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
