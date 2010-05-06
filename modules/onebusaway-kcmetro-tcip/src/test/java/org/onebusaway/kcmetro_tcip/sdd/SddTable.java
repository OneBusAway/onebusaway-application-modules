package org.onebusaway.kcmetro_tcip.sdd;

import java.util.ArrayList;
import java.util.List;

public class SddTable {

  private List<SddColumn> _columns = new ArrayList<SddColumn>();
  
  private String _tableName;

  public SddTable(String tableName) {
    _tableName = tableName;
  }

  public String getTableName() {
    return _tableName;
  }

  public List<SddColumn> getColumns() {
    return _columns;
  }
  
  public void addColumn(String name, String type) {
    SddColumn column = new SddColumn(name, type);
    if( _columns.isEmpty())
      column.setFirst(true);
    _columns.add(column);
  }
}
