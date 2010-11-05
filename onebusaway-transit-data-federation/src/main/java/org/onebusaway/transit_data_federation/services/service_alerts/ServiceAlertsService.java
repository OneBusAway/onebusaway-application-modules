package org.onebusaway.transit_data_federation.services.service_alerts;

import org.onebusaway.transit_data.model.service_alerts.SituationExchangeDeliveryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;

public interface ServiceAlertsService {
  public SituationExchangeDeliveryBean getServiceAlerts(SituationQueryBean query);
}
