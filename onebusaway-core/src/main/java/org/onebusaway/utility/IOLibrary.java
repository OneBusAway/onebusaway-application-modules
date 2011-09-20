/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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

  public static InputStream getPathAsInputStream(String path)
      throws IOException {
    if (path.startsWith("http")) {
      URL url = new URL(path);
      return url.openStream();
    } else {
      return getFileAsInputStream(new File(path));
    }
  }

  public static BufferedReader getFileAsBufferedReader(File path)
      throws IOException {
    return new BufferedReader(getFileAsReader(path));
  }

  public static Reader getFileAsReader(File path) throws IOException {
    return new InputStreamReader(getFileAsInputStream(path));
  }

  public static InputStream getFileAsInputStream(File path) throws IOException {
    InputStream is = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      is = new GZIPInputStream(is);
    return is;
  }

  public static OutputStream getFileAsOutputStream(File file)
      throws IOException {
    OutputStream out = new FileOutputStream(file);
    if (file.getName().endsWith(".gz"))
      out = new GZIPOutputStream(out);
    return out;
  }

  public static Writer getFileAsWriter(File file) throws IOException {
    OutputStream out = getFileAsOutputStream(file);
    return new OutputStreamWriter(out);
  }
}
