package org.onebusaway.webapp.gwt.where_library.resources;

import org.onebusaway.webapp.services.resources.WebappImportedWithPrefix;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;

@ImportedWithPrefix("WhereLibrary")
@WebappImportedWithPrefix("WhereLibrary")
public interface WhereLibraryCssResource extends CssResource {

  public String arrivalStatusOnTime();
  
  public String arrivalStatusNoInfo();

  public String arrivalStatusEarly();

  public String arrivalStatusDelayed();
  
  public String arrivalStatusDepartedOnTime();
  
  public String arrivalStatusDepartedNoInfo();

  public String arrivalStatusDepartedEarly();

  public String arrivalStatusDepartedDelayed();
  
  public String arrivalStatusCancelled();

}
