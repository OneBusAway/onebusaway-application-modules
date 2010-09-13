package org.onebusaway.webapp.actions.admin.console;

import java.util.Date;
import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

public class ActiveTripsAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private String _agencyId;

  private Date _time = new Date();

  private List<AgencyWithCoverageBean> _agencies;

  private List<TripDetailsBean> _trips;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  @TypeConversion(converter = "org.onebusaway.webapp.actions.where.DateTimeConverter")
  public void setTime(Date time) {
    _time = time;
  }

  public List<AgencyWithCoverageBean> getAgencies() {
    return _agencies;
  }

  public List<TripDetailsBean> getTrips() {
    return _trips;
  }

  @SkipValidation
  @Override
  public String execute() {
    _agencies = _transitDataService.getAgenciesWithCoverage();
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "agencyId", message = "missing required agencyId field")})
  public String agency() {
    TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
    query.setAgencyId(_agencyId);
    query.setMaxCount(Integer.MAX_VALUE);
    query.setTime(_time.getTime());
    query.setInclusion(new TripDetailsInclusionBean(true, false, true));

    ListBean<TripDetailsBean> result = _transitDataService.getTripsForAgency(query);

    _trips = result.getList();

    return SUCCESS;
  }

}
