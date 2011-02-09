package org.onebusaway.sms.actions;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.BookmarkException;

import com.opensymphony.xwork2.ModelDriven;

public class CommandDebugAction extends AbstractTextmarksAction implements
    ModelDriven<UserBean> {

  private static final long serialVersionUID = 1L;

  private UserBean _user;

  public UserBean getModel() {
    return _user;
  }

  @Override
  public String execute() throws ServiceException, BookmarkException {

    _user = _currentUserService.getCurrentUser();

    return SUCCESS;
  }
}
