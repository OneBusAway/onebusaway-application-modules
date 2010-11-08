package org.onebusaway.transit_data_federation.services.service_alerts;

import java.util.List;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationExchangeDeliveryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;

public interface ServiceAlertsService {

  public SituationBean createServiceAlert(String agencyId,
      SituationBean situation);

  public void updateServiceAlert(SituationBean situation);

  public void updateServiceAlerts(SituationExchangeDeliveryBean alerts);

  public SituationBean getServiceAlertForId(String situationId);

  public ListBean<SituationBean> getServiceAlerts(SituationQueryBean query);

  public List<SituationBean> getSituationsForLineId(String lineId);
}
