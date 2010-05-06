package org.onebusaway.gtfs.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public abstract class ServiceIdCalendarDataOp implements Comparator<Date> {

  public static final ServiceIdCalendarDataOp ARRIVAL_OP = new ArrivalsServiceDateTimeOp();

  public static final ServiceIdCalendarDataOp DEPARTURE_OP = new DeparturesServiceDateTimeOp();
  
  public static final ServiceIdCalendarDataOp BOTH_OP = new MaxRangeServiceDateTimeOp();

  private boolean _reverse;

  protected ServiceIdCalendarDataOp(boolean reverse) {
    _reverse = reverse;
  }

  public abstract int getFromTime(ServiceIdCalendarData data);

  public abstract int getToTime(ServiceIdCalendarData data);

  public abstract Date getServiceDate(List<Date> data, int index);

  public int compare(Date a, Date b) {
    int rc = a.compareTo(b);
    if (_reverse)
      rc = -rc;
    return rc;
  }

  public Date shiftTime(ServiceIdCalendarData data, Date time) {
    long offset = getFromTime(data) * 1000;
    return new Date(time.getTime() - offset);
  }

  public int compareInterval(ServiceIdCalendarData data, Date serviceDate, Date from, Date to) {
    long serviceFrom = serviceDate.getTime() + getFromTime(data) * 1000;
    long serviceTo = serviceDate.getTime() + getToTime(data) * 1000;

    if (_reverse) {
      if (serviceTo >= from.getTime())
        return -1;
      if (to.getTime() >= serviceFrom)
        return 1;
      return 0;
    } else {
      if (serviceTo <= from.getTime())
        return -1;
      if (to.getTime() <= serviceFrom)
        return 1;
      return 0;
    }
  }

  private static class ArrivalsServiceDateTimeOp extends ServiceIdCalendarDataOp {

    protected ArrivalsServiceDateTimeOp() {
      super(true);
    }

    @Override
    public int getFromTime(ServiceIdCalendarData data) {
      return data.getLastArrival();
    }

    @Override
    public int getToTime(ServiceIdCalendarData data) {
      return data.getFirstArrival();
    }

    @Override
    public Date getServiceDate(List<Date> data, int index) {
      return data.get(data.size() - 1 - index);
    }
  }

  private static class DeparturesServiceDateTimeOp extends ServiceIdCalendarDataOp {

    protected DeparturesServiceDateTimeOp() {
      super(false);
    }

    @Override
    public int getFromTime(ServiceIdCalendarData data) {
      return data.getFirstDeparture();
    }

    @Override
    public int getToTime(ServiceIdCalendarData data) {
      return data.getLastDeparture();
    }

    @Override
    public Date getServiceDate(List<Date> data, int index) {
      return data.get(index);
    }
  }

  private static class MaxRangeServiceDateTimeOp extends ServiceIdCalendarDataOp {

    protected MaxRangeServiceDateTimeOp() {
      super(false);
    }

    @Override
    public int getFromTime(ServiceIdCalendarData data) {
      return Math.min(data.getFirstDeparture(), data.getFirstArrival());
    }

    @Override
    public int getToTime(ServiceIdCalendarData data) {
      return Math.max(data.getLastDeparture(), data.getLastArrival());
    }

    @Override
    public Date getServiceDate(List<Date> data, int index) {
      return data.get(index);
    }
  }

}
