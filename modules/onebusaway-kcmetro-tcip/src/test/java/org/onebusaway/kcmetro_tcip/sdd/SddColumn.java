/**
 * 
 */
package org.onebusaway.kcmetro_tcip.sdd;

class SddColumn {
  private String _name;
  private String _type;
  private boolean _first = false;

  public SddColumn(String name, String type) {
    _name = name;
    _type = type;
  }

  public String getName() {
    return _name;
  }

  public String getType() {
    return _type;
  }

  public void setFirst(boolean first) {
    _first = first;
  }

  public boolean isFirst() {
    return _first;
  }
}