package org.onebusaway.gtfs.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileCsvInputSource implements CsvInputSource {

  private File _sourceDirectory;

  public FileCsvInputSource(File sourceDirectory) {
    _sourceDirectory = sourceDirectory;
  }

  public boolean hasResource(String name) throws IOException {
    File file = new File(_sourceDirectory, name);
    return file.exists();
  }

  public InputStream getResource(String name) throws IOException {
    File file = new File(_sourceDirectory, name);
    return new FileInputStream(file);
  }

  public void close() throws IOException {

  }
}
