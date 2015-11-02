package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs_realtime.archiver.service.VehiclePositionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;

@Controller
public class VehiclePositionController {
  
	private VehiclePositionDao _vehiclePositionDao;
	
	@Autowired
	public void setVehiclePositionDao(VehiclePositionDao dao) {
	    _vehiclePositionDao = dao;
	}
	

	@RequestMapping(value="/vehicleIds")
	public @ResponseBody List<String> getVehicleIds() {
		return _vehiclePositionDao.getAllVehicleIds();
	}
	
	@RequestMapping(value="/vehiclePositions")
	public @ResponseBody List<VehiclePositionModel> getVehiclePositions(
			@RequestParam(value="vehicleId") String vehicleId,
			@RequestParam(value="startDate", required=false, defaultValue="-1") long start,
			@RequestParam(value="endDate", required=false, defaultValue="-1") long end) {
		
	  // startDate and endDate are null if not present in request params.
	  Date startDate = (start > 0) ? new Date(start) : null,
	      endDate = (end > 0) ? new Date(end) : null;
	  
		return _vehiclePositionDao.getVehiclePositions(vehicleId, startDate, endDate);
	}
	
}
