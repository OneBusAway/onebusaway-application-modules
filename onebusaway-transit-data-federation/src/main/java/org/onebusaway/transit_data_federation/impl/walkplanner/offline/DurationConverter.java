/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.thoughtworks.xstream.converters.SingleValueConverter;

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

  public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
    return Long.class.equals(type);
  }
}