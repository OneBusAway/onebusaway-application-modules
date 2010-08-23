package org.onebusaway.transit_data_federation.services.reporting;

import org.onebusaway.transit_data.model.ReportProblemWithTripBean;

public interface UserReportingService {

  void reportProblemWithTrip(ReportProblemWithTripBean problem);

}
