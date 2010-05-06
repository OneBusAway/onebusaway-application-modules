/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.impl.mybus;

import edu.washington.cs.rse.transit.common.model.aggregate.BusArrivalEstimateBean;
import edu.washington.cs.rse.transit.common.services.MyBusService;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.SetNestedPropertiesRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class MyBusServiceImpl implements MyBusService {

  /***************************************************************************
   * Timining
   **************************************************************************/

  private static final Integer MYBUS_WINDOW_PRE_IN_MINUTES = 10;

  private static final Integer MYBUS_WINDOW_POST_IN_MINUTES = 30;

  /***************************************************************************
     *
     **************************************************************************/
  
  private static final String SERVICE_ENDPOINT = "http://ws.its.washington.edu:9090/transit/mybus/services/MybusService";

  private static final String SERVICE_METHOD_GET_TIME = "getTime";

  private static final String SERVICE_METHOD_GET_EVENT_DATA = "getEventData";

  private static final String PROVIDER_METRO_KC_TRANSIT = "http://transit.metrokc.gov";

  /***************************************************************************
   * Public Methods
   **************************************************************************/
  
  public MyBusServiceImpl() {
    System.out.println("========================> " + MyBusServiceImpl.class);
  }

  public void startup() {

  }

  public void shutdown() {

  }

  /***************************************************************************
   * {@link MyBusService} Interface
   **************************************************************************/

  public int getMyBusWindowPreInMinutes() {
    return MYBUS_WINDOW_PRE_IN_MINUTES;
  }

  public int getMyBusWindowPostInMinutes() {
    return MYBUS_WINDOW_POST_IN_MINUTES;
  }

  public List<BusArrivalEstimateBean> getSchedule(int mybusId)
      throws IOException, NoSuchStopException {
    return getSchedule(mybusId, MYBUS_WINDOW_PRE_IN_MINUTES,
        MYBUS_WINDOW_POST_IN_MINUTES);
  }

  /**
   * Query the MyBus system for predicted arrival times at a particular spot
   * 
   * @param mybusId - MyBus stop id
   * @param timeBefore - window size in minutes to include into the past
   * @param timeAfter - window size in minutes to include into the future
   * @return a list of bus arrival estimates
   * @throws IOException
   * @throws NoSuchStopException
   */
  public List<BusArrivalEstimateBean> getSchedule(int mybusId, int timeBefore,
      int timeAfter) throws IOException, NoSuchStopException {

    Digester d = new Digester();
    String base = "soapenv:Envelope/soapenv:Body/multiRef";

    List<BusArrivalEstimateBean> beans = new LinkedList<BusArrivalEstimateBean>();
    d.push(beans);

    d.addObjectCreate(base, BusArrivalEstimateBean.class);
    d.addRule(base, new SetNestedPropertiesRule());
    d.addSetNext(base, "add");

    StringBuffer url = new StringBuffer();
    url.append(SERVICE_ENDPOINT);
    url.append('?');
    url.append("method=").append(SERVICE_METHOD_GET_EVENT_DATA);
    url.append("&in0=").append(timeAfter);
    url.append("&in1=").append(-timeBefore);
    url.append("&in2=").append(mybusId);
    url.append("&in3=").append(PROVIDER_METRO_KC_TRANSIT);

    try {
      URL u = new URL(url.toString());
      go(d, u);
    } catch (SAXException ex) {
      throw new IOException("Error parsing schedule");
    } catch (IOExceptionWithResponseCode ex) {
      throw new NoSuchStopException();
    } catch (Exception ex) {
      throw new NoSuchStopException();
    }

    for (BusArrivalEstimateBean bean : beans)
      bean.setTimepointId(mybusId);

    return beans;
  }

  public long getMetroTime() throws IOException {

    Digester d = new Digester();
    String base = "soapenv:Envelope/soapenv:Body/getTimeResponse/getTimeReturn";

    TimeBean bean = new TimeBean();
    d.push(bean);

    d.addCallMethod(base, "setTime", 0);

    StringBuffer url = new StringBuffer();
    url.append(SERVICE_ENDPOINT);
    url.append('?');
    url.append("method=").append(SERVICE_METHOD_GET_TIME);

    try {
      URL u = new URL(url.toString());
      go(d, u);
    } catch (SAXException ex) {
      throw new IOException("Error parsing schedule");
    }

    return bean.getTime();
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private void go(Digester d, URL u) throws IOException, SAXException {
    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
    connection.connect();
    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
      throw new IOExceptionWithResponseCode(connection.getResponseCode());

    try {
      d.parse(connection.getInputStream());
    } catch (IOException ex) {

      InputStream err = connection.getErrorStream();
      byte[] buffer = new byte[1024];
      while (err.read(buffer) > 0) {

      }
      err.close();

      throw ex;
    }
  }

  private class TimeBean {

    private long _time;

    public void setTime(String time) {
      _time = Long.parseLong(time);
    }

    public long getTime() {
      return _time;
    }
  }

  private static class IOExceptionWithResponseCode extends IOException {

    private static final long serialVersionUID = 1L;

    private int _responseCode;

    public IOExceptionWithResponseCode(int responseCode) {
      _responseCode = responseCode;
    }

    public int getResponseCode() {
      return _responseCode;
    }
  }
}
