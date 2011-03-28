package org.onebusaway.transit_data_federation_webapp.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class VehicleLocationRecordController {

  private static DateFormat _format = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");

  @Autowired
  private VehicleLocationListener _vehicleLocationListener;

  @RequestMapping("/vehicle-location-record.action")
  public ModelAndView index() {
    return new ModelAndView("vehicle-location-record.jspx");
  }

  @RequestMapping("/vehicle-location-record!submit.action")
  public ModelAndView submit(@RequestParam() String time,
      @RequestParam() String serviceDate, @RequestParam() String blockId,
      @RequestParam() String vehicleId, int scheduleDeviation)
      throws ParseException {

    long t = convertTime(time);
    long sd = convertTime(serviceDate);

    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setTimeOfRecord(t);
    record.setTimeOfLocationUpdate(t);
    record.setServiceDate(sd);
    record.setBlockId(AgencyAndIdLibrary.convertFromString(blockId));
    record.setVehicleId(AgencyAndIdLibrary.convertFromString(vehicleId));
    record.setScheduleDeviation(scheduleDeviation);

    _vehicleLocationListener.handleVehicleLocationRecord(record);

    return new ModelAndView("redirect:/vehicle-location-record.action");
  }

  @RequestMapping("/vehicle-location-record!reset.action")
  public ModelAndView submit(@RequestParam() String vehicleId) {

    AgencyAndId vid = AgencyAndIdLibrary.convertFromString(vehicleId);
    _vehicleLocationListener.resetVehicleLocation(vid);

    return new ModelAndView("redirect:/vehicle-location-record.action");
  }

  private long convertTime(String time) throws ParseException {
    if (time.matches("^\\d+$"))
      return Long.parseLong(time);
    Date date = _format.parse(time);
    return date.getTime();
  }
}
