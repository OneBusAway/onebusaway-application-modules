/**
 * 
 */
package org.onebusaway.metrokc2gtdf.handlers;

import org.onebusaway.metrokc2gtdf.model.MetroKCStreetName;

public class StreetNameHandler extends
    EntityHandler<Integer, MetroKCStreetName> {

  private static final String[] STREET_NAME_FIELDS = {
      "id", "status", "dbModDate", "prefix", "name", "type", "suffix"};

  public StreetNameHandler() {
    super(MetroKCStreetName.class, STREET_NAME_FIELDS);
  }

  public String getStreetName(int id) {

    MetroKCStreetName street = getEntity(id);

    String combined = street.getName();
    String type = street.getType();
    String prefix = street.getPrefix();
    String suffix = street.getSuffix();

    if (type != null)
      type = type.trim();
    if (prefix != null)
      prefix = prefix.trim();
    if (suffix != null)
      suffix = suffix.trim();

    if (type != null && type.length() > 0)
      combined = combined + " " + type;
    if (prefix != null && prefix.length() > 0)
      combined = prefix + " " + combined;
    if (suffix != null && suffix.length() > 0)
      combined = combined + " " + suffix;

    return combined;
  }
}