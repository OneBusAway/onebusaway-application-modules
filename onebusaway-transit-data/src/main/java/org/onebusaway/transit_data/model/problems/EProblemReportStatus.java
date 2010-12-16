package org.onebusaway.transit_data.model.problems;

/**
 * Status of a problem report, similar the the status of a bug in an issue
 * tracking system.
 * 
 * @author bdferris
 * @see StopProblemReportBean
 * @see TripProblemReportBean
 */
public enum EProblemReportStatus {

  /**
   * A new, unaccepted problem report
   */
  NEW,

  /**
   * An accepted problem report that needs action
   */
  ACCEPTED,
  
  /**
   * Work on this problem has been started
   */
  STARTED,
  
  /**

  /**
   * A problem report that has been fixed
   */
  FIXED,

  /**
   * The fix for the problem report has been verified
   */
  VERIFIED,
  
  /**
   * The problem report is invalid
   */
  INVALID,
  
  /**
   * Duplicate error report
   */
  DUPLICATE,

  /**
   * We will not address this problem report
   */
  WONT_FIX
}
