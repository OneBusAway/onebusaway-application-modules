/**
 * 
 */
package org.onebusaway.metrokc2gtdf.handlers;

import org.onebusaway.metrokc2gtdf.model.MetroKCChangeDate;

public class ChangeDateHandler extends EntityHandler<Integer, MetroKCChangeDate> {

  private static String[] CHANGE_DATE_FIELDS = {
      "id", "bookingId", "start_date", "dbModDate", "minorChangeDate",
      "end_date", "currentNextCode", "effectiveBeginDate", "effectiveEndDate",
      "ignore", "ignore"};

  public ChangeDateHandler() {
    super(MetroKCChangeDate.class, CHANGE_DATE_FIELDS);
  }
}