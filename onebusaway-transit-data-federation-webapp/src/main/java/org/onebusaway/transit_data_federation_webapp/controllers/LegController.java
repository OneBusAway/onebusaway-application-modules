package org.onebusaway.transit_data_federation_webapp.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LegController {

  private static DateFormat _format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

  @Autowired
  private TransitGraphDao _graphDao;

  @Autowired
  private TransferPatternPathService _pathService;

  @RequestMapping("/leg.action")
  public ModelAndView index(@RequestParam() String time,
      @RequestParam() String fromStopId, @RequestParam() String toStopId)
      throws ParseException {

    long t = convertTime(time);

    StopEntry fromStop = _graphDao.getStopEntryForId(
        AgencyAndIdLibrary.convertFromString(fromStopId), true);
    StopEntry toStop = _graphDao.getStopEntryForId(
        AgencyAndIdLibrary.convertFromString(toStopId), true);

    long tIn = System.currentTimeMillis();
    _pathService.leg(fromStop, toStop, t);
    long tOut = System.currentTimeMillis();
    System.out.println(tOut - tIn);

    return new ModelAndView("redirect:/index.action");
  }

  private long convertTime(String time) throws ParseException {
    if (time.matches("^\\d+$"))
      return Long.parseLong(time);
    Date date = _format.parse(time);
    return date.getTime();
  }
}
