package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.onebusaway.transit_data.model.service_alerts.AbstractSituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationExchangeDeliveryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.springframework.stereotype.Component;

@Component
public class ServiceAlertsServiceImpl implements ServiceAlertsService {

  private ConcurrentMap<Long, AbstractSituationBean> _situations = new ConcurrentHashMap<Long, AbstractSituationBean>();

  public void updateServiceAlerts(SituationExchangeDeliveryBean alerts) {

    List<AbstractSituationBean> situations = alerts.getSituations();

    if (situations == null || situations.isEmpty())
      return;

    for (AbstractSituationBean situation : situations) {
      _situations.put(situation.getSituationId(), situation);
    }
  }

  @Override
  public SituationExchangeDeliveryBean getServiceAlerts(SituationQueryBean query) {
    List<AbstractSituationBean> situations = new ArrayList<AbstractSituationBean>(
        _situations.values());

    SituationExchangeDeliveryBean delivery = new SituationExchangeDeliveryBean();
    delivery.setSituations(situations);
    return delivery;
  }
}
