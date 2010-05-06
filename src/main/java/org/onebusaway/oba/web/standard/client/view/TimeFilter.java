package org.onebusaway.oba.web.standard.client.view;

public interface TimeFilter {
  /**
   * 
   * @param time in seconds
   * @return
   */
  public boolean isEnabled(int time);
}
