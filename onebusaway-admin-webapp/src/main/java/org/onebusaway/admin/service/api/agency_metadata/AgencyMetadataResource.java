/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.api.agency_metadata;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.agency_metadata.model.AgencyMetadata;
import org.onebusaway.agency_metadata.service.AgencyMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/agency")
@Component
@Scope("singleton")
public class AgencyMetadataResource {

  private final ObjectMapper mapper = new ObjectMapper();
  private static Logger log = LoggerFactory.getLogger(AgencyMetadataResource.class);

  @Autowired
  private AgencyMetadataService _agencyMetadataService;

  @Path("/list")
  @GET
  @Produces("application/json")
  public Response listAgencyMetadata() throws JsonGenerationException,
      JsonMappingException, IOException {
    try {
      List<AgencyMetadata> agencyMetadata =  _agencyMetadataService.getAllAgencyMetadata();
      Response response = constructResponse(agencyMetadata);
    return response;
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new WebApplicationException(e, Response.serverError().build());
    }
  }

  @Path("/create")
  @GET
  @Produces("application/json")
  public Response createAgencyMetadata (
    @QueryParam("gtfs_id") String gtfs_id, 
    @QueryParam("name") String name, 
    @QueryParam("short_name") String short_name, 
    @QueryParam("legacy_id") String legacy_id,
    @QueryParam("gtfs_feed_url") String gtfs_feed_url, 
    @QueryParam("gtfs_rt_feed_url") String gtfs_rt_feed_url, 
    @QueryParam("bounding_box") String bounding_box, 
    @QueryParam("ntd_id") String ntd_id,
    @QueryParam("agency_message") String agency_message
    ) throws JsonGenerationException, JsonMappingException, IOException {
    
    log.info("Starting createAgencyMetadata with gtfs_id: " + gtfs_id + ", name: " 
        + name + ", short_name: " + short_name +", legacy_id: " + legacy_id + ", gtfs_feed_url: " 
        + gtfs_feed_url + ", gtfs_rt_feed_url: " + gtfs_rt_feed_url + ", bounding_box: "
        + bounding_box + ", ntd_id: " + ntd_id + ", agency_message: " + agency_message);
    
    String message = "Agency Metadata record created for: " + name;
    
    try {
      _agencyMetadataService.createAgencyMetadata(gtfs_id, name, 
          short_name, legacy_id, gtfs_feed_url, gtfs_rt_feed_url, 
          bounding_box, ntd_id, agency_message);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new WebApplicationException(e, Response.serverError().build());
    }
    Response response = constructResponse(message);
    return response;
  }

  @Path("/update/{agencyIndex}")
  @GET
  @Produces("application/json")
  public Response updateAgencyMetadata (
    @PathParam("agencyIndex") String agencyIndex,
    @QueryParam("gtfs_id") String gtfs_id, 
    @QueryParam("name") String name, 
    @QueryParam("short_name") String short_name, 
    @QueryParam("legacy_id") String legacy_id,
    @QueryParam("gtfs_feed_url") String gtfs_feed_url, 
    @QueryParam("gtfs_rt_feed_url") String gtfs_rt_feed_url, 
    @QueryParam("bounding_box") String bounding_box, 
    @QueryParam("ntd_id") String ntd_id,
    @QueryParam("agency_message") String agency_message
    )  throws JsonGenerationException, JsonMappingException, IOException {
        
    log.info("Starting updateAgencyMetadata with agencyIndex: " + agencyIndex + ", gtfs_id: "
      + gtfs_id + ", name: " + name + ", short_name: " + short_name +", legacy_id: "
      + legacy_id + ", gtfs_feed_url: " + gtfs_feed_url + ", gtfs_rt_feed_url: " 
      + gtfs_rt_feed_url + ", bounding_box: " + bounding_box + ", ntd_id: " + ntd_id
      + ", agency_message: " + agency_message);

    String message = "Agency Metadata updated: " + agencyIndex;
    try {
      long id = Long.parseLong(agencyIndex);
      _agencyMetadataService.updateAgencyMetadata(id, gtfs_id, name, 
          short_name, legacy_id, gtfs_feed_url, gtfs_rt_feed_url, 
          bounding_box, ntd_id, agency_message);
    } catch (Exception e) {
      log.error(e.getMessage());
      message = e.getMessage();
    }
    Response response = constructResponse(message);
    return response;
  }

  @Path("/delete/{agencyIndex}")
  @GET
  @Produces("application/json")
  public Response deleteAgencyMetadata (@PathParam("agencyIndex")
  String agencyIndex) throws JsonGenerationException, JsonMappingException,
      IOException {
    String message = "Agency Metadata deleted: " + agencyIndex;
    try {
      long index = Long.parseLong(agencyIndex);
      _agencyMetadataService.delete(index);
    } catch (Exception e) {
      log.error(e.getMessage());
      message = "Failed to delete key: " + e.getMessage();
    }
    Response response = constructResponse(message);
    log.info("Returning deleteAgencyMetadata result.");
    return response;
  }

  // Private methods

  private Response constructResponse(Object result) {
    final StringWriter sw = new StringWriter();
    final MappingJsonFactory jsonFactory = new MappingJsonFactory();
    Response response = null;
    try {
      final com.fasterxml.jackson.core.JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
      mapper.writeValue(jsonGenerator, result);
      response = Response.ok(sw.toString()).build();
    } catch (JsonGenerationException e) {
      log.error("Error generating response JSON");
      response = Response.serverError().build();
      e.printStackTrace();
    } catch (JsonMappingException e) {
      log.error("Error mapping response to JSON");
      response = Response.serverError().build();
      e.printStackTrace();
    } catch (IOException e) {
      log.error("I/O error while creating response JSON");
      response = Response.serverError().build();
      e.printStackTrace();
    }
    
    return response; 
  }

}
