package org.onebusaway.metrokc2gtdf.model;

import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.model.IdentityBean;
import org.onebusaway.metrokc2gtdf.MetroKCDateFieldMappingFactory;

import java.util.Date;

@CsvFields(filename = "change_dates.csv")
public class MetroKCChangeDate extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @CsvField(optional = true)
  private int id;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date startDate;

  @CsvField(optional = true, mapping = MetroKCDateFieldMappingFactory.class)
  private Date endDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
