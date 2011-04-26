package org.onebusaway.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class IOLibrary {

  public static Reader getPathAsReader(String path) throws IOException {
    return new InputStreamReader(getPathAsInputStream(path));
  }

  public static InputStream getPathAsInputStream(String path)
      throws IOException {
    if (path.startsWith("http")) {
      URL url = new URL(path);
      return url.openStream();
    } else {
      InputStream is = new FileInputStream(new File(path));
      if (path.endsWith(".gz"))
        is = new GZIPInputStream(is);
      return is;
    }
  }
}
