package org.onebusaway.webapp.actions.where;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.actions.AbstractAction;
import org.onebusaway.webapp.impl.WebappArrivalsAndDeparturesModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class BookmarkAction extends AbstractAction implements
    ModelDriven<WebappArrivalsAndDeparturesModel> {

  private static final long serialVersionUID = 1L;

  private int _id = -1;

  private WebappArrivalsAndDeparturesModel _model;

  private String _bookmarkName;

  @Autowired
  public void setModel(WebappArrivalsAndDeparturesModel model) {
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
    _model.setTargetTime(time);
  }

  public void setMinutesBefore(int minutesBefore) {
    _model.setMinutesBefore(minutesBefore);
  }

  public void setMinutesAfter(int minutesAfter) {
    _model.setMinutesAfter(minutesAfter);
  }

  @Override
  public WebappArrivalsAndDeparturesModel getModel() {
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
    if (name != null && name.length() > 0)
      _bookmarkName = name;

    List<String> stopIds = bookmark.getStopIds();
    Set<String> routeIds = bookmark.getRouteFilter().getRouteIds();

    _model.setStopIds(stopIds);
    _model.setRouteFilter(routeIds);
    _model.process();

    logUserInteraction("stopIds", stopIds, "routeIds", routeIds);

    return SUCCESS;
  }

  private BookmarkBean getBookmark() {
    UserBean user = getCurrentUser();
    for (BookmarkBean bookmark : user.getBookmarks()) {
      if (bookmark.getId() == _id)
        return bookmark;
    }
    return null;
  }

}