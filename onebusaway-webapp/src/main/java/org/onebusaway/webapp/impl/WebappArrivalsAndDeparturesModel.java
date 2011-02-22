package org.onebusaway.webapp.impl;

import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.impl.service_alerts.SituationsPresentation;
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

  private ArrivalsAndDeparturesPresentaion _arrivalsAndDeparturesPresentation = new ArrivalsAndDeparturesPresentaion();

  private SituationsPresentation _situations;

  @Autowired
  public void setWhereMessages(ArrivalAndDepartureMessages messages) {
    _arrivalsAndDeparturesPresentation.setMessages(messages);
  }

  public ArrivalsAndDeparturesPresentaion getArrivalsAndDeparturesPresentation() {
    return _arrivalsAndDeparturesPresentation;
  }

  public void setShowArrivals(boolean showArrivals) {
    _arrivalsAndDeparturesPresentation.setShowArrivals(showArrivals);
  }

  public boolean isShowArrivals() {
    return _arrivalsAndDeparturesPresentation.isShowArrivals();
  }

  public SituationsPresentation getSituations() {
    if (_situations == null) {
      _situations = new SituationsPresentation();
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
