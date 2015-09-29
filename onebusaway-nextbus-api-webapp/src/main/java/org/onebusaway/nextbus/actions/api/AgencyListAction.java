package org.onebusaway.nextbus.actions.api;

import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.model.nextbus.Agency;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class AgencyListAction implements ModelDriven<Body<Agency>> {

  @Autowired
  private TransitDataService _service;

  public HttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  @Override
  public Body<Agency> getModel() {

    Body<Agency> body = new Body<Agency>();
    List<AgencyWithCoverageBean> agencies = _service.getAgenciesWithCoverage();
    for (AgencyWithCoverageBean agencyBean : agencies) {
      Agency agency = new Agency();
      agency.setTag(agencyBean.getAgency().getId());
      agency.setTitle(agencyBean.getAgency().getName());
      agency.setRegionTitle(agencyBean.getAgency().getName());
      body.getResponse().add(agency);
    }

    return body;

  }

}
