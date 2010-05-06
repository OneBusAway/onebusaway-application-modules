package org.onebusaway.webapp.gwt.oba_application.view;

public interface TimeFilter {
  /**
   * 
   * @param time in seconds
   * @return
   */
  public boolean isEnabled(int time);
}
