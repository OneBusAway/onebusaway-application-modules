package org.onebusaway.kcmetro_tcip.sdd;

import java.util.ArrayList;
import java.util.List;

public class SddSchema {

  private List<SddTable> _tables = new ArrayList<SddTable>();

  public void addTable(SddTable table) {
    _tables.add(table);
  }

  public String getSchemaAsString() {
    StringBuilder b = new StringBuilder();
    b.append("CREATE SCHEMA");

    for (SddTable table : _tables) {
      b.append(" CREATE TABLE ");
      b.append(table.getTableName());
      b.append(" (");

      for (SddColumn column : table.getColumns()) {
        if (!column.isFirst())
          b.append(",");
        b.append(" ");
        b.append(column.getName());
        b.append(" ");
        b.append(column.getType());
      }
      b.append(")");
    }
    b.append(";");
    return b.toString();
  }

  public SddTable getTableForName(String name) {
    for (SddTable table : _tables) {
      if (table.getTableName().equals(name))
        return table;
    }
    return null;
  }
}
