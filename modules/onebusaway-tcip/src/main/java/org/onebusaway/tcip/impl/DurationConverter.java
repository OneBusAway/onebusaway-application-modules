/**
 * 
 */
package org.onebusaway.tcip.impl;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class DurationConverter implements SingleValueConverter {

  private DatatypeFactory _factory;

  public DurationConverter() {
    try {
      _factory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public Object fromString(String str) {
    Duration d = _factory.newDuration(str);
    long start = System.currentTimeMillis();
    Date asDate = new Date(start);
    d.addTo(asDate);
    return new Long(asDate.getTime() - start);
  }

  public String toString(Object obj) {
    Long vObj = (Long) obj;
    return _factory.newDuration(vObj.longValue()).toString();
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return Long.class.equals(type);
  }
}