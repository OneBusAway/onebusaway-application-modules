package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.List;

import org.onebusaway.gtfs_realtime.archiver.service.VehiclePositionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vehicleIds")
public class GetRealtimeVehicleIds {
	
	private VehiclePositionDao _vehiclePositionDao;
	
	@Autowired
	public void setVehiclePositionDao(VehiclePositionDao dao) {
	    _vehiclePositionDao = dao;
	}
	
	@RequestMapping()
	public List<String> getVehicleIds() {
		return _vehiclePositionDao.getAllVehicleIds();
	}
	
}
