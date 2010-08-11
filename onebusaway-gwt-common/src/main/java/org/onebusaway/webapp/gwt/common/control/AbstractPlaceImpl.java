package org.onebusaway.webapp.gwt.common.control;

public abstract class AbstractPlaceImpl implements Place {

  @Override
  public String getDescriptionAsString() {
    StringBuilder b = new StringBuilder();
    for (String token : getDescription()) {
      if (b.length() > 0)
        b.append(' ');
      b.append(token);
    }
    return b.toString();
  }
}
