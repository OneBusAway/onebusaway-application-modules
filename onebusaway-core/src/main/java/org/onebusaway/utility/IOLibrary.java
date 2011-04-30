package org.onebusaway.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IOLibrary {

  public static BufferedReader getPathAsBufferedReader(String path)
      throws IOException {
    return new BufferedReader(getPathAsReader(path));
  }

  public static Reader getPathAsReader(String path) throws IOException {
    return new InputStreamReader(getPathAsInputStream(path));
  }

  public static InputStream getFileAsInputStream(File path) throws IOException {
    InputStream is = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      is = new GZIPInputStream(is);
    return is;
  }

  public static InputStream getPathAsInputStream(String path)
      throws IOException {
    if (path.startsWith("http")) {
      URL url = new URL(path);
      return url.openStream();
    } else {
      return getFileAsInputStream(new File(path));
    }
  }

  public static OutputStream getFileAsOutputStream(File file)
      throws IOException {
    OutputStream out = new FileOutputStream(file);
    if (file.getName().endsWith(".gz"))
      out = new GZIPOutputStream(out);
    return out;
  }
}
