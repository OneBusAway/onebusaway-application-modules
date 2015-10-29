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
			@RequestParam(value="startDate") long startDate,
			@RequestParam(value="endDate") long endDate) {
		
		return _vehiclePositionDao.getVehiclePositions(vehicleId, new Date(startDate), new Date(endDate));
	}
	
}
