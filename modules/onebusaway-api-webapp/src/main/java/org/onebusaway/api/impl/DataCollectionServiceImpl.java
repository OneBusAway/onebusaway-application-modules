package org.onebusaway.api.impl;

import org.onebusaway.api.services.DataCollectionService;

import java.io.File;

class DataCollectionServiceImpl implements DataCollectionService {

  private File _dataDirectory;

  public void setDataDirectory(File dataDirectory) {
    _dataDirectory = dataDirectory;
    _dataDirectory.mkdirs();
  }

  public File getDataDirectory() {
    return _dataDirectory;
  }
}
