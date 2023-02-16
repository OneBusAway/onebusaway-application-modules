package org.onebusaway.admin.service.api;

import org.onebusaway.admin.service.api.ConfigResource;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/config")
public class ConfigApiAction {

    @Autowired
    ConfigurationService _configurationService;

    @GET
    @Produces("application/json")
    public Response list(){
        Map<String, String> config = _configurationService.getConfigFromLocalFile();
        String json = config.toString();
        Response result = Response.ok(json).build();
        return result;
    }

    private static Logger _log = LoggerFactory.getLogger(ConfigResource.class);
}
