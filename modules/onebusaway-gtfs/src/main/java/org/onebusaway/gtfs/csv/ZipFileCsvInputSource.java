package org.onebusaway.gtfs.csv;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileCsvInputSource implements CsvInputSource {

  private ZipFile _zipFile;

  public ZipFileCsvInputSource(ZipFile zipFile) {
    _zipFile = zipFile;
  }

  public boolean hasResource(String name) throws IOException {
    ZipEntry entry = _zipFile.getEntry(name);
    return entry != null;
  }

  public InputStream getResource(String name) throws IOException {
    ZipEntry entry = _zipFile.getEntry(name);
    return _zipFile.getInputStream(entry);
  }

  public void close() throws IOException {
    _zipFile.close();
  }
}
