package org.onebusaway.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class IOLibrary {

  public static InputStream getPathAsInputStream(String path)
      throws IOException {
    if (path.startsWith("http")) {
      URL url = new URL(path);
      return url.openStream();
    } else {
      return new FileInputStream(new File(path));
    }
  }
}
