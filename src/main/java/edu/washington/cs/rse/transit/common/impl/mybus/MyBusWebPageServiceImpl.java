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

import com.ibm.icu.text.SimpleDateFormat;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBusWebPageServiceImpl implements MyBusService {

  private static Logger _log = Logger.getLogger(MyBusWebPageServiceImpl.class.getName());

  protected static final TimeZone _tz = TimeZone.getTimeZone("US/Pacific");

  private static final Integer MYBUS_WINDOW_PRE_IN_MINUTES = 10;

  private static final Integer MYBUS_WINDOW_POST_IN_MINUTES = 30;

  private static final String SERVICE_ENDPOINT = "http://mybus.org/metrokc/avl.jsp?id=";

  private static Pattern _htmlRecordPattern = Pattern.compile("^<tr class=\"backcolor\\d\"><td>(\\d+)\\w*</td><td>(.*)</td><td>(.*)</td><td class=\"\\w+\">(.*)</td></tr>$");

  private static Pattern _xMinDelayPattern = Pattern.compile("^(\\d+) min delay$");

  private static Pattern _xMinEarlyPattern = Pattern.compile("^(\\d+) min early");

  private static Pattern _departedPattern = Pattern.compile("^(predicted ){0,1}departed (at|by) (.*)$");

  private static final SimpleDateFormat _dateParser = new SimpleDateFormat(
      "hh:mmaa");

  public MyBusWebPageServiceImpl() {
    System.out.println("========================> "
        + MyBusWebPageServiceImpl.class);
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
    
    List<BusArrivalEstimateBean> beans = new LinkedList<BusArrivalEstimateBean>();

    try {
      String path = SERVICE_ENDPOINT + mybusId;
      URL url = new URL(path);
      go(mybusId, beans, url);
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
    return System.currentTimeMillis();
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private void go(int timepointId, List<BusArrivalEstimateBean> beans, URL u)
      throws IOException, SAXException {

    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
    connection.connect();

    try {

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
        throw new IOExceptionWithResponseCode(connection.getResponseCode());

      parse(timepointId, beans, connection.getInputStream());

    } catch (IOException ex) {

      InputStream err = connection.getErrorStream();
      byte[] buffer = new byte[1024];
      while (err.read(buffer) > 0) {

      }
      err.close();

      throw ex;
    }
  }

  private void parse(int timepointId, List<BusArrivalEstimateBean> beans,
      InputStream inputStream) throws IOException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        inputStream));
    String line = null;

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      Matcher m = _htmlRecordPattern.matcher(line);
      if (m.matches()) {

        try {

          int route = Integer.parseInt(m.group(1));
          String dest = m.group(2);
          Date scheduledTimeAsDate = _dateParser.parse(m.group(3));
          String predicted = m.group(4);

          int scheduledTime = getSecondsSinceStartOfDay(scheduledTimeAsDate.getTime());
          int goalTime = parseGoalTime(scheduledTimeAsDate.getTime(),
              scheduledTime, predicted);

          BusArrivalEstimateBean bean = new BusArrivalEstimateBean();
          bean.setTimepointId(timepointId);
          bean.setRoute(route);
          bean.setDestination(dest);
          bean.setSchedTime(scheduledTime);
          bean.setGoalTime(goalTime);
          beans.add(bean);
          
          if( goalTime != -1)
            bean.setGoalDeviation(goalTime-scheduledTime);

        } catch (NumberFormatException ex) {
          _log.warning("invalid mybus line=" + line);
        } catch (ParseException e) {
          _log.warning("invalid mybus line=" + line);
        }
      }
    }

    reader.close();
  }

  private int parseGoalTime(long scheduledTimeFull, int scheduledTime,
      String predicted) {
    predicted = predicted.trim().toLowerCase();
    if ("no info".equals(predicted))
      return -1;
    if ("on time".equals(predicted))
      return scheduledTime;

    Matcher m1 = _xMinDelayPattern.matcher(predicted);
    if (m1.matches()) {
      int minutes = Integer.parseInt(m1.group(1));
      return scheduledTime + minutes;
    }

    Matcher m2 = _xMinEarlyPattern.matcher(predicted);
    if (m2.matches()) {
      int minutes = Integer.parseInt(m2.group(1));
      return scheduledTime - minutes;
    }

    Matcher m3 = _departedPattern.matcher(predicted);
    if (m3.matches()) {
      String time = m3.group(3);
      try {
        Date predictedTimeAsDate = _dateParser.parse(time);
        int minDiff = (int) ((predictedTimeAsDate.getTime() - scheduledTimeFull) / 1000);
        return scheduledTime + minDiff;
      } catch (ParseException e) {
        _log.warning("invalid prediction string: " + predicted + " time="
            + time);
        return -1;
      }
    }

    _log.warning("invalid prediction string: " + predicted);

    return -1;
  }

  protected int getSecondsSinceStartOfDay(long time) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(_tz);
    calendar.setTimeInMillis(time);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    long startOfWeek = calendar.getTimeInMillis();
    return (int) ((time - startOfWeek) / 1000);
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
