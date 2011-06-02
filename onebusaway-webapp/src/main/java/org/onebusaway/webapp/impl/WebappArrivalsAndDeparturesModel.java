package org.onebusaway.webapp.impl;

import java.util.Date;
import java.util.Map;

import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.impl.service_alerts.SituationsPresentation;
import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.webapp.actions.bundles.ArrivalAndDepartureMessages;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class WebappArrivalsAndDeparturesModel extends
    ArrivalsAndDeparturesModel {

  private ConfigurationService _configurationService;

  private ArrivalsAndDeparturesPresentaion _arrivalsAndDeparturesPresentation = new ArrivalsAndDeparturesPresentaion();

  private SituationsPresentation _situations;

  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    _configurationService = configurationService;
  }

  @Autowired
  public void setWhereMessages(ArrivalAndDepartureMessages messages) {
    _arrivalsAndDeparturesPresentation.setMessages(messages);
  }

  public ArrivalsAndDeparturesPresentaion getArrivalsAndDeparturesPresentation() {
    return _arrivalsAndDeparturesPresentation;
  }

  @Override
  public void setTargetTime(Date time) {
    super.setTargetTime(time);
    _arrivalsAndDeparturesPresentation.setTime(time.getTime());
  }

  public void setShowArrivals(boolean showArrivals) {
    super.setShowArrivals(showArrivals);
    _arrivalsAndDeparturesPresentation.setShowArrivals(showArrivals);
  }

  public boolean isShowArrivals() {
    return _arrivalsAndDeparturesPresentation.isShowArrivals();
  }

  public SituationsPresentation getSituations() {
    if (_situations == null) {
      _situations = new SituationsPresentation();

      Map<String, Object> config = _configurationService.getConfiguration(false);
      String key = (String) config.get("apiKey");
      if (key != null)
        _situations.setApiKey(key);

      _situations.setSituations(_result.getSituations());
      _situations.setUser(_user);
    }
    return _situations;
  }

  public SituationsPresentation getSituationsForArrivalAndDeparture(
      ArrivalAndDepartureBean arrivalAndDeparture) {
    SituationsPresentation situations = new SituationsPresentation();
    situations.setSituations(arrivalAndDeparture.getSituations());
    situations.setUser(_user);
    return situations;
  }

}
