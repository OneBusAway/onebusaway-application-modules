package org.onebusaway.webapp.gwt.viewkit;

public class IndexPath {
  
  private final int _section;
  
  private final int _row;
  
  public IndexPath(int section, int row) {
    _section = section;
    _row = row;
  }
  
  public int getSection() {
    return _section;
  }
  
  public int getRow() {
    return _row;
  }
}
