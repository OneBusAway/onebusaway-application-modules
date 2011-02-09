package org.onebusaway.geospatial.model;

public interface Point {
  
  public int getDimensions();
  
  public double getOrdinate(int index);
  
  public Point translate(double[] distances);
  
  public double getDistance(Point position);
}
