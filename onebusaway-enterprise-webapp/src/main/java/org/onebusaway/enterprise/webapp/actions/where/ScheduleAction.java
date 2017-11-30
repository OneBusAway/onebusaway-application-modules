/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.enterprise.webapp.actions.where;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.schedule.FrequencyInstanceBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.DateLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.Conversion;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Conversion
public class ScheduleAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private static final StopCalendarDayAdapter _stopCalendarDayAdapter = new StopCalendarDayAdapter();

  private final StopTimeAdapter _stopTimeAdapter = new StopTimeAdapter();

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _date;

  private StopScheduleBean _result;

  private TimeZone _timeZone;

  private Boolean _showArrivals = null;

  public void setId(String id) {
    _id = id;
  }

  @TypeConversion(converter = "org.onebusaway.enterprise.webapp.actions.schedule.ScheduleByStopDateConverter")
  public void setDate(Date date) {
    _date = date;
  }

  public void setShowArrivals(boolean showArrivals) {
    _showArrivals = showArrivals;
  }

  public boolean isShowArrivals() {
    return _showArrivals;
  }

  public StopScheduleBean getResult() {
    return _result;
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }
  
  @Override
  @Actions({
      @Action(value = "/where/schedule")
  })
  public String execute() throws Exception {

    if (_date == null)
      _date = new Date();

    if (_id == null)
      return INPUT;

    _result = _service.getScheduleForStop(_id, _date);
    
    if (_result == null)
      throw new NoSuchStopServiceException(_id);
    
    StopCalendarDaysBean days = _result.getCalendarDays();
    String tzName = days.getTimeZone();
    _timeZone = TimeZone.getTimeZone(tzName);
    if (_timeZone == null)
      _timeZone = TimeZone.getDefault();

    filterResults();
    
    return SUCCESS;
  }

  public boolean isAmPmClock() {
    return DateLibrary.hasAmPmClock(getLocale());
  }

  public List<String> getShortWeekDays() {

    List<String> result = new ArrayList<String>();
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(getLocale());

    String shortWeekdays[] = dateFormatSymbols.getShortWeekdays();
    Calendar calendar = Calendar.getInstance(getLocale());
    int firstDayOfWeek = calendar.getFirstDayOfWeek();

    for (int dayOfWeek = firstDayOfWeek; dayOfWeek < shortWeekdays.length; dayOfWeek++) {
      result.add(shortWeekdays[dayOfWeek]);
    }

    for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek < firstDayOfWeek; dayOfWeek++) {
      result.add(shortWeekdays[dayOfWeek]);
    }

    return result;
  }

  public <T> List<List<T>> getRows(List<T> elements, int rowLength) {

    List<List<T>> rows = new ArrayList<List<T>>();
    List<T> row = new ArrayList<T>();

    for (T element : elements) {
      row.add(element);
      if (row.size() == rowLength) {
        rows.add(row);
        row = new ArrayList<T>();
      }
    }

    if (!row.isEmpty())
      rows.add(row);

    return rows;
  }

  /****
   * 
   * @param stopTimes
   * @param format
   * @return
   ****/

  public List<T2<String, List<StopTimeInstanceBean>>> getStopTimesByFormatKey(
      List<StopTimeInstanceBean> stopTimes, String format) {
    return getTimesByFormatKey(stopTimes, format, _stopTimeAdapter);
  }

  public List<T2<String, List<StopCalendarDayBean>>> getStopCalendarDaysByFormatKey(
      List<StopCalendarDayBean> stopCalendarDays, String format) {
    return getTimesByFormatKey(stopCalendarDays, format,
        _stopCalendarDayAdapter);
  }

  public List<StopCalendarDayBean> getStopCalendarDaysByFormatKeyAndValue(
      List<StopCalendarDayBean> stopCalendarDays, String format, String value) {
    return getTimesByFormatKeyAndValue(stopCalendarDays, format, value,
        _stopCalendarDayAdapter);
  }

  public String getFrequencyCellHeight(FrequencyInstanceBean bean) {
    int hours = (int) Math.round((bean.getEndTime() - bean.getStartTime())
        / (60.0 * 60 * 1000));
    if (hours == 0)
      hours = 1;
    return hours + "em";
  }

  /****
   * 
   ****/

  private void filterResults() {

    List<StopRouteScheduleBean> routes = _result.getRoutes();
    List<StopRouteScheduleBean> filteredRoutes = new ArrayList<StopRouteScheduleBean>(
        routes.size());

    for (StopRouteScheduleBean route : routes) {

      List<StopRouteDirectionScheduleBean> directions = route.getDirections();
      List<StopRouteDirectionScheduleBean> filteredDirections = new ArrayList<StopRouteDirectionScheduleBean>(
          directions.size());

      for (StopRouteDirectionScheduleBean direction : directions) {
    	  
        List<StopTimeInstanceBean> filteredStopTimes = getFilteredStopTimes(direction);
        List<FrequencyInstanceBean> filteredFrequencies = getFilteredFrequencies(direction);
        
        if (!(filteredStopTimes.isEmpty() && filteredFrequencies.isEmpty())) {
          direction.setStopTimes(filteredStopTimes);
          direction.setFrequencies(filteredFrequencies);
          filteredDirections.add(direction);
        }
      }

      if (!filteredDirections.isEmpty()) {
        route.setDirections(filteredDirections);
        filteredRoutes.add(route);
      }
    }

    _result.setRoutes(filteredRoutes);
  }

  private List<StopTimeInstanceBean> getFilteredStopTimes(
      StopRouteDirectionScheduleBean direction) {
    List<StopTimeInstanceBean> stopTimes = direction.getStopTimes();
    List<StopTimeInstanceBean> filteredStopTimes = new ArrayList<StopTimeInstanceBean>(
        stopTimes.size());

    boolean showArrivals = (_showArrivals == null) ? !(directionHasDepartures(direction)) : _showArrivals;
    
    for (StopTimeInstanceBean stopTime : stopTimes) {
      if ((showArrivals && stopTime.isArrivalEnabled())
          || (!showArrivals && stopTime.isDepartureEnabled()))
        filteredStopTimes.add(stopTime);
    }
    return filteredStopTimes;
  }

  private List<FrequencyInstanceBean> getFilteredFrequencies(
      StopRouteDirectionScheduleBean direction) {
    List<FrequencyInstanceBean> frequencies = direction.getFrequencies();
    List<FrequencyInstanceBean> filteredFrequencies = new ArrayList<FrequencyInstanceBean>(
        frequencies.size());

    boolean showArrivals = (_showArrivals == null) ? !(directionHasDepartures(direction)) : _showArrivals;
    
    for (FrequencyInstanceBean frequency : frequencies) {
      if ((showArrivals && frequency.isArrivalEnabled())
          || (! showArrivals && frequency.isDepartureEnabled()))
        filteredFrequencies.add(frequency);
    }
    return filteredFrequencies;
  }
  
  private boolean directionHasDepartures(StopRouteDirectionScheduleBean direction) {
	  
	  for (StopTimeInstanceBean stopTime : direction.getStopTimes())
		  if (stopTime.isDepartureEnabled())
			  return true;
	 
	  for (FrequencyInstanceBean frequency : direction.getFrequencies())
		  if (frequency.isDepartureEnabled())
			  return true;

	  return false;
  }

  public Date getShiftedDate(Date date) {
    return getShiftedDateStatic(date);
  }

  public String getFormattedDate(String format, Date date) {
    SimpleDateFormat df = new SimpleDateFormat(format);
    df.setTimeZone(_timeZone);
    return df.format(date);
  }

  /****
   * Private Methods
   ****/

  private static interface IAdapter<FROM, TO> {
    public TO adapt(FROM source);
  }

  private static Date getShiftedDateStatic(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.HOUR_OF_DAY, 12);
    return c.getTime();
  }

  private <T> List<T> getTimesByFormatKeyAndValue(List<T> times, String format,
      String value, IAdapter<T, Date> adapter) {

    SimpleDateFormat df = new SimpleDateFormat(format, getLocale());
    df.setTimeZone(_timeZone);
    List<T> results = new ArrayList<T>();

    for (T time : times) {
      Date date = adapter.adapt(time);
      String v = df.format(date);
      if (v.equals(value))
        results.add(time);
    }
    return results;
  }

  private <T> List<T2<String, List<T>>> getTimesByFormatKey(List<T> stopTimes,
      String format, IAdapter<T, Date> adapter) {

    SimpleDateFormat df = new SimpleDateFormat(format);
    df.setTimeZone(_timeZone);

    List<T2<String, List<T>>> tuples = new ArrayList<T2<String, List<T>>>();
    T2<String, List<T>> tuple = null;

    for (T bean : stopTimes) {
      Date date = adapter.adapt(bean);
      String key = df.format(date);
      if (tuple == null || !tuple.getFirst().equals(key)) {
        if (tuple != null && !tuple.getSecond().isEmpty())
          tuples.add(tuple);
        List<T> beans = new ArrayList<T>();
        tuple = Tuples.tuple(key, beans);
      }
      tuple.getSecond().add(bean);
    }

    if (tuple != null && !tuple.getSecond().isEmpty())
      tuples.add(tuple);

    return tuples;
  }

  private class StopTimeAdapter implements IAdapter<StopTimeInstanceBean, Date> {
    public Date adapt(StopTimeInstanceBean source) {
      return new Date(_showArrivals ? source.getArrivalTime()
          : source.getDepartureTime());
    }
  }

  private static class StopCalendarDayAdapter implements
      IAdapter<StopCalendarDayBean, Date> {
    public Date adapt(StopCalendarDayBean source) {
      Date date = source.getDate();
      return getShiftedDateStatic(date);
    }
  }
  
}
