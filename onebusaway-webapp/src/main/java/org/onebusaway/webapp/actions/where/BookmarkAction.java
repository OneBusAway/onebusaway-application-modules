package org.onebusaway.webapp.actions.where;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.actions.AbstractAction;
import org.onebusaway.webapp.impl.ArrivalsAndDeparturesModelImpl;
import org.onebusaway.webapp.services.ArrivalsAndDeparturesModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class BookmarkAction extends AbstractAction implements
    ModelDriven<ArrivalsAndDeparturesModel> {

  private static final long serialVersionUID = 1L;

  private int _id = -1;

  private ArrivalsAndDeparturesModelImpl _model;

  private String _bookmarkName;

  @Autowired
  public void setModel(ArrivalsAndDeparturesModelImpl model) {
    _model = model;
  }

  public void setId(int id) {
    _id = id;
  }

  public void setOrder(String order) {
    if (!_model.setOrderFromString(order))
      addFieldError("order", "unknown order value: " + order);
  }

  @TypeConversion(converter = "org.onebusaway.webapp.actions.where.DateTimeConverter")
  public void setTime(Date time) {
    _model.setTime(time);
  }

  public void setMinutesBefore(int minutesBefore) {
    _model.setMinutesBefore(minutesBefore);
  }

  public void setMinutesAfter(int minutesAfter) {
    _model.setMinutesAfter(minutesAfter);
  }

  @Override
  public ArrivalsAndDeparturesModel getModel() {
    return _model;
  }
  
  public String getBookmarkName() {
    return _bookmarkName;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/bookmark"),
      @Action(value = "/where/iphone/bookmark"),
      @Action(value = "/where/text/bookmark")})
  public String execute() {

    if (_id == -1)
      return INPUT;

    BookmarkBean bookmark = getBookmark();
    if (bookmark == null)
      return INPUT;
    
    String name = bookmark.getName();
    if( name != null && name.length() > 0)
      _bookmarkName = name;
    
    _model.setStopIds(bookmark.getStopIds());
    _model.setRouteFilter(bookmark.getRouteFilter().getRouteIds());
    _model.process();

    return SUCCESS;
  }

  private BookmarkBean getBookmark() {
    UserBean user = _currentUserService.getCurrentUser();
    for (BookmarkBean bookmark : user.getBookmarks()) {
      if (bookmark.getId() == _id)
        return bookmark;
    }
    return null;
  }

}