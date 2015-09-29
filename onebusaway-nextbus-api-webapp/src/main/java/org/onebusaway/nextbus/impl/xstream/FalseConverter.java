package org.onebusaway.nextbus.impl.xstream;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class FalseConverter implements SingleValueConverter {

  public boolean canConvert(Class type) {
      return type.equals(boolean.class);
  }

  @Override
  public String toString(Object obj) {
    Boolean booleanField = (Boolean) obj;
    if(booleanField == null || !booleanField){
      return null;
    }
    return booleanField.toString();
  }

  @Override
  public Object fromString(String str) {
    return str;
  }
}