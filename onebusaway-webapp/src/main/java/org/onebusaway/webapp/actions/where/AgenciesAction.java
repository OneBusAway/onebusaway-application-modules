package org.onebusaway.webapp.actions.where;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.impl.AgencyWithCoverageBeanComparator;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class AgenciesAction extends OneBusAwayActionSupport implements
    ModelDriven<List<AgencyWithCoverageBean>> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<AgencyWithCoverageBean> _model;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public List<AgencyWithCoverageBean> getModel() {
    return _model;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/agencies"),
      @Action(value = "/where/iphone/agencies"),
      @Action(value = "/where/text/agencies")})
  public String execute() throws ServiceException {
    _model = _transitDataService.getAgenciesWithCoverage();
    Collections.sort(_model, new AgencyWithCoverageBeanComparator());
    return SUCCESS;
  }

}
