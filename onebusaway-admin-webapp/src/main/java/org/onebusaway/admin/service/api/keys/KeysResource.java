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
package org.onebusaway.admin.service.api.keys;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/keys")
@Component
@Scope("singleton")
public class KeysResource {

	  private final ObjectMapper mapper = new ObjectMapper();
	  private static Logger log = LoggerFactory.getLogger(KeysResource.class);

	  @Autowired
	  private UserService _userService;

	  @Autowired
	  private UserPropertiesService _userPropertiesService;

	  public KeysResource() {
	  }

	  @Path("/list")
	  @GET
	  @Produces("application/json")
	  public Response listKeys() throws JsonGenerationException,
	      JsonMappingException, IOException {
	    log.info("Starting listKeys");

	    try {
	      List<String> apiKeys = 
	        _userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);
	      Response response = constructResponse(apiKeys);
		  log.info("Returning response from listKeys");
		  return response;
	    } catch (Exception e) {
	      log.error(e.getMessage());
	      throw new WebApplicationException(e, Response.serverError().build());
	    }
	  }

	  @Path("/list/{apiKey}")
	  @GET
	  @Produces("application/json")
	  public Response listKeyDetails(@PathParam("apiKey") String apiKey)
		throws JsonGenerationException, JsonMappingException, IOException {
	    log.info("Starting listKeyDetails");

	    try {
        UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
        UserIndex userIndex = _userService.getUserIndexForId(key);
  
        if (userIndex == null)
          throw new Exception("API key " + apiKey + " not found (userIndex null).");
  
        User user = userIndex.getUser();
        UserBean bean = _userService.getUserAsBean(user);     
        Map<String, String> result = new HashMap<String, String>();
        result.put("keyValue", apiKey);
        result.put("contactName", bean.getContactName());
        result.put("contactCompany", bean.getContactCompany());
        result.put("contactEmail", bean.getContactEmail());
        result.put("contactDetails", bean.getContactDetails());
        Response response = constructResponse(result);
        log.info("Returning response from listKeyDetails");
        return response;
	    } catch (Exception e) {
	      log.error(e.getMessage());
	      throw new WebApplicationException(e, Response.serverError().build());
	    }
	  }

	  @Path("/create")
	  @GET
	  @Produces("application/json")
	  public Response createKey(
	    @DefaultValue("") @QueryParam("name") String name, 
	    @DefaultValue("") @QueryParam("company") String company, 
	    @DefaultValue("") @QueryParam("email") String email, 
	    @DefaultValue("") @QueryParam("details") String details
	    ) throws JsonGenerationException,
	      JsonMappingException, IOException {
	    log.info("Starting createKey with no parameter");
	    return createKey(UUID.randomUUID().toString(), name, company, 
	      email, details);
	  }

	  @Path("/create/{keyValue}")
	  @GET
	  @Produces("application/json")
	  public Response createKey(
	    @PathParam("keyValue") String keyValue,
	    @DefaultValue("") @QueryParam("name") String name, 
	    @DefaultValue("") @QueryParam("company") String company, 
	    @DefaultValue("") @QueryParam("email") String email, 
	    @DefaultValue("") @QueryParam("details") String details
	    )  throws JsonGenerationException, JsonMappingException, IOException {
	        
	    log.info("Starting createKey with keyValue: " + keyValue + ", name: " 
	      + name + ", company: " + company +", email: " + email + ", details: " 
	      + details);

	    String message = "API Key created: " + keyValue;
	    try {
	      saveOrUpdateKey(keyValue, 0L, name, company, email, details);
	    } catch (Exception e) {
	      log.error(e.getMessage());
	      message = e.getMessage();
	    }
	    Response response = constructResponse(message);
	    log.info("Returning createKey result.");
	    return response;
	  }

	  @Path("/update/{keyValue}")
	  @GET
	  @Produces("application/json")
	  public Response updateKey(
	    @PathParam("keyValue") String keyValue,
	    @QueryParam("name") String name, 
	    @QueryParam("company") String company, 
	    @QueryParam("email") String email, 
	    @QueryParam("details") String details
	    )  throws JsonGenerationException, JsonMappingException, IOException {
	        
	    log.info("Starting updateKey with keyValue: " + keyValue + ", name: " 
	      + name + ", company: " + company +", email: " + email + ", details: " 
	      + details);

	    String message = "API Key updated: " + keyValue;
	    try {
	      updateKeyContactInfo(keyValue, name, company, email, details);
	    } catch (Exception e) {
	      log.error(e.getMessage());
	      message = e.getMessage();
	    }
	    Response response = constructResponse(message);
	    log.info("Returning updateKey result.");
	    return response;
	  }

	  @Path("/delete/{keyValue}")
	  @GET
	  @Produces("application/json")
	  public Response deleteKey(@PathParam("keyValue")
	  String keyValue) throws JsonGenerationException, JsonMappingException,
	      IOException {
	    log.info("Starting deleteKey with parameter " + keyValue);

	    String message = "API Key deleted: " + keyValue;
	    try {
	      delete(keyValue);
	    } catch (Exception e) {
	      log.error(e.getMessage());
	      message = "Failed to delete key: " + e.getMessage();
	    }
	    Response response = constructResponse(message);
	    log.info("Returning deleteKey result.");
	    return response;
	  }

	  // Private methods

	  private void saveOrUpdateKey(String apiKey, Long minApiRequestInterval, 
	      String contactName, String contactCompany, String contactEmail, 
	      String contactDetails) throws Exception {
	    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
	    UserIndex userIndexForId = _userService.getUserIndexForId(key);
	    if (userIndexForId != null) {
	      throw new Exception("API key " + apiKey + " already exists.");
	    }
	    UserIndex userIndex = 
	      _userService.getOrCreateUserForIndexKey(key, "", true);

	    _userPropertiesService.authorizeApi(userIndex.getUser(),
	        minApiRequestInterval);
	    
	    // Set the API Key contact info
	    User user = userIndex.getUser();
	    _userPropertiesService.updateApiKeyContactInfo(user, contactName, 
        contactCompany, contactEmail, contactDetails);

	    // Clear the cached value here
	    _userService.getMinApiRequestIntervalForKey(apiKey, true);
	  }

	  private void updateKeyContactInfo(String apiKey, String contactName, 
	      String contactCompany, String contactEmail, 
	      String contactDetails) throws Exception {
	    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
	    UserIndex userIndex = _userService.getUserIndexForId(key);

	    if (userIndex == null)
	      throw new Exception("API key " + apiKey + " not found (userIndex null).");

	    User user = userIndex.getUser();
      UserBean bean = _userService.getUserAsBean(user);
      String keyContactName = bean.getContactName();
      String keyContactCompany = bean.getContactCompany();
      String keyContactEmail = bean.getContactEmail();
      String keyContactDetails = bean.getContactDetails();
      
      if (contactName != null) {
        keyContactName = contactName;
      }	    
      if (contactCompany != null) {
        keyContactCompany = contactCompany;
      }	    
      if (contactEmail != null) {
        keyContactEmail = contactEmail;
      }	    
      if (contactDetails != null) {
        keyContactDetails = contactDetails;
      }
	    
	    _userPropertiesService.updateApiKeyContactInfo(user, keyContactName, 
        keyContactCompany, keyContactEmail, keyContactDetails);
	  }
	  
	  private void delete(String apiKey) throws Exception {
	    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
	    UserIndex userIndex = _userService.getUserIndexForId(key);

	    if (userIndex == null)
	      throw new Exception("API key " + apiKey + " not found (userIndex null).");

	    User user = userIndex.getUser();

	    boolean found = false;
	    for (UserIndex index : user.getUserIndices()) {
	      if (index.getId().getValue().equalsIgnoreCase(key.getValue())) {
	        userIndex = index;
	        found = true;
	        break;
	      }
	    }
	    if (!found)
	      throw new Exception("API key " + apiKey + " not found (no exact match).");

	    _userService.removeUserIndexForUser(user, userIndex.getId());

	    if (user.getUserIndices().isEmpty())
	      _userService.deleteUser(user);

	    // Clear the cached value here
	    try {
	    _userService.getMinApiRequestIntervalForKey(apiKey, true);
	    } catch (Exception e) {
	      // Ignore this
	    }
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
}
