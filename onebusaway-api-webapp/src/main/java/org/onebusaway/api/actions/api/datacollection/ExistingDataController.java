package org.onebusaway.api.actions.api.datacollection;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.services.DataCollectionService;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExistingDataController extends ApiActionSupport {
  
  private static final int V1 = 1;

  @Autowired
  private DataCollectionService _data;
  
  public ExistingDataController() {
    super(V1);
  }

  private static final long serialVersionUID = 1L;
  
  public DefaultHttpHeaders index() {
    List<String> values = new ArrayList<String>();
    File dataDirectory = _data.getDataDirectory();
    File[] files = dataDirectory.listFiles();
    if( files != null) {
      for( File file : files)
        values.add(file.getName());
    }
    return setOkResponse(values);
  }

}
