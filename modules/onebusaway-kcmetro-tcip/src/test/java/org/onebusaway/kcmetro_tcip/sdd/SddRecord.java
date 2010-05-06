package org.onebusaway.kcmetro_tcip.sdd;

import java.util.HashMap;
import java.util.Map;

public class SddRecord {

  private Map<String, String> _values = new HashMap<String, String>();

  private SddTable _table;

  public SddRecord(SddTable table) {
    _table = table;
  }

  public void put(String key, String value) {
    _values.put(key, value);
  }

  public void put(String key, int value) {
    put(key, Integer.toString(value));
  }

  public void reset() {
    _values.clear();
  }

  public String getRecordAsString() {
    StringBuilder b = new StringBuilder();
    b.append("TABLE ");
    b.append(_table.getTableName());
    b.append(" COLUMN (");

    for (SddColumn column : _table.getColumns()) {
      if (!column.isFirst())
        b.append(",");
      b.append(" ");
      b.append(column.getName());
    }
    b.append(")\n");

    for (SddColumn column : _table.getColumns()) {
      if (!column.isFirst())
        b.append(", ");
      boolean isString = column.getType().startsWith("CHAR");
      if (isString)
        b.append('\'');
      b.append(_values.get(column.getName()));
      if (isString)
        b.append('\'');
    }
    b.append(";");
    return b.toString();
  }
}
