package org.onebusaway.transit_data_federation.impl.tripplanner;

public interface AStarNode {

  public void setOpen();

  public boolean isOpen();

  public void setClosed();

  public boolean isClosed();

  public void setDistanceFromStart(double distance);

  public double getDistanceFromStart();

  public void setEstimatedDistanceToEnd(double distance);

  public double getEstimatedDistanceToEnd();

  public void setCameFrom(Object cameFrom);

  public Object getCameFrom();

  public void reset();
}
