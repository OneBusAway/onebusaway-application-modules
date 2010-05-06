package org.onebusaway.gtfs;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.serialization.GtfsEntityStore;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GtfsTestData {

  public static final String CALTRAIN_GTFS = "org/onebusaway/gtfs/caltrain_20090308_1937.zip";

  public static final String ISLAND_GTFS = "org/onebusaway/gtfs/island-transit_20090312_0314.zip";

  public static final String BART_GTFS = "org/onebusaway/gtfs/bart.zip";

  public static File getCaltrainGtfs() {
    return getResourceAsTemporaryFile(CALTRAIN_GTFS);
  }

  public static File getIslandGtfs() {
    return getResourceAsTemporaryFile(ISLAND_GTFS);
  }

  public static File getBartGtfs() {
    return getResourceAsTemporaryFile(BART_GTFS);
  }

  public static <T extends GtfsEntityStore> void readGtfs(T entityStore,
      File resourcePath, String defaultAgencyId) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(defaultAgencyId);

    reader.setInputLocation(resourcePath);

    reader.setEntityStore(entityStore);

    reader.run();

  }

  private static File getResourceAsTemporaryFile(String path) {
    try {

      ClassLoader loader = GtfsTestData.class.getClassLoader();
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

  public static <T> List<T> grep(Iterable<T> elements, String propertyExpression,
      Object value) {
  
    String[] properties = propertyExpression.split("\\.");
    List<T> matches = new ArrayList<T>();
  
    for (T element : elements) {
      Object v = element;
      for (String property : properties) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(v);
        v = wrapper.getPropertyValue(property);
      }
      if ((value == null && v == null) || (value != null && value.equals(v)))
        matches.add(element);
    }
    return matches;
  }

}
