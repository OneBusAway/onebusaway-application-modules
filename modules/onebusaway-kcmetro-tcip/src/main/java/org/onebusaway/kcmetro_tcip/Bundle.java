package org.onebusaway.kcmetro_tcip;

import java.io.File;

public class Bundle {

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public File getTripIdMappingPath() {
    return new File(_path, "TripIdMapping.txt");
  }
}
