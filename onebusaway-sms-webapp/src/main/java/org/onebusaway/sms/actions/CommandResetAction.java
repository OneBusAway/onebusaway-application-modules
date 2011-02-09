package org.onebusaway.sms.actions;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.users.services.BookmarkException;
import org.springframework.beans.factory.annotation.Autowired;

public class CommandResetAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private String _arg;

  private DefaultSearchLocationService _defaultSearchLocationService;

  public void setArg(String arg) {
    _arg = arg;
  }
  
  @Autowired
  public void setDefaultSearchLocationService(DefaultSearchLocationService defaultSearchLoctionService) {
    _defaultSearchLocationService = defaultSearchLoctionService;
  }

  @Override
  public String execute() throws ServiceException, BookmarkException {

    if (_arg != null && _arg.length() > 0) {

    } else {
      _defaultSearchLocationService.clearDefaultLocationForCurrentUser();
      _currentUserService.resetCurrentUser();
      clearNextActions();
    }

    return SUCCESS;
  }
}
