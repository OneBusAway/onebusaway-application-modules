/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.presentation.impl;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

  private static Logger _log = LoggerFactory.getLogger(DateUtil.class);
  
  public static XMLGregorianCalendar toXmlGregorianCalendar(long timestamp){
    // to Gregorian Calendar
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTimeInMillis(timestamp);
      // to XML Gregorian Calendar
      try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    } catch (DatatypeConfigurationException e) {
      _log.error("Error converting timestamp to XMLGregorianCalendar", e);
      return null;
    }   
  }
  
  public static XMLGregorianCalendar toXmlGregorianCalendar(GregorianCalendar gc){
      // to XML Gregorian Calendar
      try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    } catch (DatatypeConfigurationException e) {
      _log.error("Error converting timestamp to XMLGregorianCalendar", e);
      return null;
    }   
  }
}
