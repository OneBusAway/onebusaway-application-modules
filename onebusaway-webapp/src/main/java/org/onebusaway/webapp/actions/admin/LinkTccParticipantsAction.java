package org.onebusaway.webapp.actions.admin;

import java.io.File;
import java.io.IOException;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.services.TccParticipantRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", name="redirect", params = {"actionName", "index"})})
public class LinkTccParticipantsAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _data;

  private File _file;

  private TccParticipantRegistrationService _registrationService;

  @Autowired
  public void setRegistrationService(
      TccParticipantRegistrationService registrationService) {
    _registrationService = registrationService;
  }

  public void setData(String data) {
    _data = data;
  }

  public void setFile(File file) {
    _file = file;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  public String submit() throws IOException {

    if (_data != null && _data.length() > 0)
      _registrationService.register(_data);

    if (_file != null)
      _registrationService.register(_file);

    return "redirect";
  }
}
