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
package org.onebusaway.admin.service.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.admin.service.bundle.api.AuthenticatedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides API methods for user operations
 * @author abelsare
 *
 */
@Path("/users")
@Component
public class UserResource extends AuthenticatedResource {

	private UserManagementService userManagementService;
	private final ObjectMapper mapper = new ObjectMapper();
	private static Logger log = LoggerFactory.getLogger(UserResource.class);
	
	@GET
	@Produces("application/json")
	public Response getUserNames(@QueryParam("search") String searchString) {
		
		if (!isAuthorized()) {
			return Response.noContent().build();
		}
		
		log.info("Getting matching user names");
		
		List<String> matchingUserNames = userManagementService.getUserNames(searchString);
		
		Response response = constructResponse(matchingUserNames);
		
		log.info("Returning response from getUserNames");
		
		return response;
	}
	
	@Path("/getUserDetails")
	@GET
	@Produces("application/json")
	public Response getDetail(@QueryParam("user") String userName) {
		
		if (!isAuthorized()) {
			return Response.noContent().build();
		}

		log.info("Getting user detail for user : {}", userName);
		
		UserDetail userDetail = userManagementService.getUserDetail(userName);
		
		Response response = constructResponse(userDetail);
		
		log.info("Returning response from getUserDetail");
		
		return response;
	}
	
	private Response constructResponse(Object result) {
		final StringWriter sw = new StringWriter();
		final MappingJsonFactory jsonFactory = new MappingJsonFactory();
		Response response = null;
		try {
			final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
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

	/**
	 * @param userManagementService the userManagementService to set
	 */
	@Autowired
	public void setUserManagementService(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	
	
}
