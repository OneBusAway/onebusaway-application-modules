/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.model.MetroKCChangeDate;

public class ChangeDateHandler extends EntityHandler<String, MetroKCChangeDate> {

  private static String[] CHANGE_DATE_FIELDS = {
      "id", "bookingId", "start_date", "dbModDate", "minorChangeDate",
      "end_date", "currentNextCode", "effectiveBeginDate", "effectiveEndDate",
      "ignore", "ignore"};

  public ChangeDateHandler() {
    super(MetroKCChangeDate.class, CHANGE_DATE_FIELDS);
  }
}