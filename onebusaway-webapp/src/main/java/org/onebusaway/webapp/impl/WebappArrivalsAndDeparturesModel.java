package org.onebusaway.webapp.impl;

import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.impl.resources.ClientBundleFactory;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryCssResource;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryResources;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class WebappArrivalsAndDeparturesModel extends ArrivalsAndDeparturesModel {

  private ArrivalsAndDeparturesPresentaion _arrivalsAndDeparturesPresentation = new ArrivalsAndDeparturesPresentaion();

  @Autowired
  public void setWhereMessages(WhereMessages messages) {
    _arrivalsAndDeparturesPresentation.setMessages(messages);
  }

  @Autowired
  public void setClientBundleFactory(ClientBundleFactory factory) {
    WhereLibraryResources resources = factory.getBundleForType(WhereLibraryResources.class);
    WhereLibraryCssResource css = resources.getCss();
    _arrivalsAndDeparturesPresentation.setCss(css);
  }

  public ArrivalsAndDeparturesPresentaion getArrivalsAndDeparturesPresentation() {
    return _arrivalsAndDeparturesPresentation;
  }
  
  public void setShowArrivals(boolean showArrivals){
    _arrivalsAndDeparturesPresentation.setShowArrivals(showArrivals);
  }
  
  public boolean isShowArrivals() {
    return _arrivalsAndDeparturesPresentation.isShowArrivals();
  }
}
