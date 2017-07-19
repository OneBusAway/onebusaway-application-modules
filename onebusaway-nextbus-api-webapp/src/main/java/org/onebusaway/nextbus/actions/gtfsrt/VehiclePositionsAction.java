package org.onebusaway.nextbus.actions.gtfsrt;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.nextbus.actions.api.NextBusApiBase;
import org.onebusaway.nextbus.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class VehiclePositionsAction extends NextBusApiBase  implements
        ModelDriven<FeedMessage> {

    private static Logger _log = LoggerFactory.getLogger(VehiclePositionsAction.class);

    @Autowired
    private HttpUtil _httpUtil;

    public static final String VEHICLE_UPDATES_COMMAND = "/command/gtfs-rt/vehiclePositions";

    private String agencyId;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public DefaultHttpHeaders index() {
        return new DefaultHttpHeaders("success");
    }

    @Override
    public FeedMessage getModel() {
        FeedMessage message = null;
        String gtfsrtUrl = getServiceUrl() + agencyId + VEHICLE_UPDATES_COMMAND;
        try{
            message = _httpUtil.getFeedMessage(gtfsrtUrl, 30);
        }
        catch(Exception e){
            _log.error(e.getMessage());
        }
        return message;
    }
}
