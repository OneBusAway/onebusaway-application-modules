package org.onebusaway.where.web.actions;

import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.collections.tuple.T2;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopScheduleBean;
import org.onebusaway.where.web.common.client.model.StopTimeBean;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduleByStopAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private static final StopTimeAdapter _stopTimeAdapter = new StopTimeAdapter();

  private static final StopCalendarDayAdapter _stopCalendarDayAdapter = new StopCalendarDayAdapter();

  @Autowired
  private WhereService _whereService;

  private String _id;

  private Date _date;

  private String _route;

  private StopScheduleBean _result;

  public void setId(String id) {
    _id = id;
  }

  @TypeConversion(converter = "org.onebusaway.where.web.actions.ScheduleByStopDateConverter")
  public void setDate(Date date) {
    _date = date;
  }

  public void setRoute(String route) {
    _route = route;
  }

  public StopScheduleBean getResult() {
    return _result;
  }

  @Override
  public String execute() throws Exception {

    if (_date == null)
      _date = new Date();

    if (_id == null)
      return INPUT;

    _result = _whereService.getScheduleForStop(_id, _date);

    return SUCCESS;
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

  public List<T2<String, List<StopTimeBean>>> getStopTimesByFormatKey(
      List<StopTimeBean> stopTimes, String format) {
    return getTimesByFormatKey(stopTimes, format, _stopTimeAdapter);
  }

  public List<T2<String, List<StopCalendarDayBean>>> getStopCalendarDaysByFormatKey(
      List<StopCalendarDayBean> stopTimes, String format) {
    return getTimesByFormatKey(stopTimes, format, _stopCalendarDayAdapter);
  }

  public List<StopCalendarDayBean> getStopCalendarDaysByFormatKeyAndValue(
      List<StopCalendarDayBean> stopTimes, String format, String value) {
    return getTimesByFormatKeyAndValue(stopTimes, format, value,
        _stopCalendarDayAdapter);
  }

  private <T> List<T> getTimesByFormatKeyAndValue(List<T> times, String format,
      String value, IAdapter<T, Date> adapter) {

    SimpleDateFormat df = new SimpleDateFormat(format);
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

    List<T2<String, List<T>>> tuples = new ArrayList<T2<String, List<T>>>();
    T2<String, List<T>> tuple = null;

    for (T bean : stopTimes) {
      Date date = adapter.adapt(bean);
      String key = df.format(date);
      if (tuple == null || !tuple.getFirst().equals(key)) {
        if (tuple != null && !tuple.getSecond().isEmpty())
          tuples.add(tuple);
        List<T> beans = new ArrayList<T>();
        tuple = T2.create(key, beans);
      }
      tuple.getSecond().add(bean);
    }

    if (tuple != null && !tuple.getSecond().isEmpty())
      tuples.add(tuple);

    return tuples;
  }

  private static class StopTimeAdapter implements IAdapter<StopTimeBean, Date> {
    public Date adapt(StopTimeBean source) {
      return source.getDepartureDate();
    }
  }

  private static class StopCalendarDayAdapter implements
      IAdapter<StopCalendarDayBean, Date> {
    public Date adapt(StopCalendarDayBean source) {
      return source.getDate();
    }
  }
}
