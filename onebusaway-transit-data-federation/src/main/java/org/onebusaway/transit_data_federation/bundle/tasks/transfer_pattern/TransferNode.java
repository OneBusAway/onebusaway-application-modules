package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.HashSet;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferNode extends TransferParent {

  private final Pair<StopEntry> stops;

  private boolean _exitAllowed;

  private double _minRemainingWeight = -1;

  private Object _departureLocalValue = null;

  private Object _arrivalLocalValue = null;

  public TransferNode(TransferPatternData data, Pair<StopEntry> stops) {
    super(data);
    this.stops = stops;
  }

  public Pair<StopEntry> getStops() {
    return stops;
  }

  public StopEntry getFromStop() {
    return stops.getFirst();
  }

  public StopEntry getToStop() {
    return stops.getSecond();
  }

  public void setExitAllowed(boolean exitAllowed) {
    _exitAllowed = exitAllowed;
  }

  public boolean isExitAllowed() {
    return _exitAllowed;
  }

  public double getMinRemainingWeight() {
    return _minRemainingWeight;
  }

  public void setMinRemainingWeight(double minRemainingWeight) {
    _minRemainingWeight = minRemainingWeight;
  }

  public Object getDepartureLocalValue() {
    return _departureLocalValue;
  }

  public void setDepartureLocalValue(Object departureLocalValue) {
    _departureLocalValue = departureLocalValue;
  }

  public Object getArrivalLocalValue() {
    return _arrivalLocalValue;
  }

  public void setArrivalLocalValue(Object arrivalLocalValue) {
    _arrivalLocalValue = arrivalLocalValue;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    toString(this, new HashSet<Pair<StopEntry>>(), "", b);
    return b.toString();
  }
}
