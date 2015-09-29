package org.onebusaway.nextbus.impl.xstream;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class RemoveEmptyConverter implements SingleValueConverter {

  public boolean canConvert(Class type) {
      return type.equals(String.class);
  }

  @Override
  public String toString(Object obj) {
    String string = (String) obj;
    if(StringUtils.isBlank(string)){
      return null;
    }
    return string;
  }

  @Override
  public Object fromString(String str) {
    return str;
  }
}