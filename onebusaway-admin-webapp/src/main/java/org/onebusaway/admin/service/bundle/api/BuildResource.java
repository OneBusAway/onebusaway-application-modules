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
package org.onebusaway.admin.service.bundle.api;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.service.BundleRequestService;
import org.onebusaway.admin.service.exceptions.DateValidationException;
import org.onebusaway.util.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Path("/build")
@Component
@Scope("singleton")
public class BuildResource extends AuthenticatedResource {
	@Autowired
	private BundleRequestService _bundleService;
	private LoggingService loggingService;
	private String directoryName;
	private String bundleName;
	private String startDate;
	private String endDate;
	private String emailTo;
	private String comment;
	private boolean archive = false;
	private boolean consolidate = false;
	private boolean predate = false;
	
	private final ObjectMapper _mapper = new ObjectMapper();
	private static Logger _log = LoggerFactory.getLogger(BuildResource.class);

	@Path("/create")
	@POST
	@Produces("application/json")
	public Response build(
			@FormParam("bundleDirectory") String bundleDirectory,
			@FormParam("bundleName") String bundleName,
			@FormParam("email") String email,
			@FormParam("bundleStartDate") String bundleStartDate,
			@FormParam("bundleEndDate") String bundleEndDate,
			@FormParam("bundleComment") String bundleComment,
			@FormParam("archive") boolean archiveFlag,
			@FormParam("consolidate") boolean consolidateFlag,
			@FormParam("predate") boolean predateFlag) {
		_log.info("in build....");
		Response response = null;
		directoryName = bundleDirectory;
		this.bundleName = bundleName;
		startDate = bundleStartDate;
		endDate = bundleEndDate;
		emailTo = email;
		comment = bundleComment;
		archive = archiveFlag;
		consolidate = consolidateFlag;
		predate = predateFlag;
		
		if (!isAuthorized()) {
			_log.warn("build unauthorized!");
			return Response.noContent().build();
		}
		
		BundleBuildResponse buildResponse = null;

		try {
			_log.info("validating dates....");
			validateDates(bundleStartDate, bundleEndDate);
			_log.info("dates valid");
		} catch(DateValidationException e) {
			try {
				buildResponse = new BundleBuildResponse();
				buildResponse.setException(e);
				response = constructResponse(buildResponse);
			} catch (Exception any) {
				_log.error("exception in build:", any);
				response = Response.serverError().build();
			}
		}

		//Proceed only if date validation passes
		if(response == null) {
			BundleBuildRequest buildRequest = new BundleBuildRequest();
			buildRequest.setBundleDirectory(bundleDirectory);
			buildRequest.setBundleName(bundleName);
			buildRequest.setEmailAddress(email);
			buildRequest.setBundleStartDate(bundleStartDate);
			buildRequest.setBundleEndDate(bundleEndDate);
			buildRequest.setBundleComment(bundleComment);
			buildRequest.setArchiveFlag(archive);
			buildRequest.setConsolidateFlag(consolidate);
			buildRequest.setPredate(predate);
			
			String session = RequestContextHolder.currentRequestAttributes().getSessionId();
			buildRequest.setSessionId(session);
			
			try {
				String message = "Starting bundle building process for bundle '" + buildRequest.getBundleName()
						+ "' initiated by user : " + _currentUserService.getCurrentUserDetails().getUsername();
				_log.warn(message);
				String component = System.getProperty("admin.chefRole");
				loggingService.log(component, Level.INFO, message);
				buildResponse =_bundleService.build(buildRequest);
				buildResponse = _bundleService.buildBundleResultURL(buildResponse.getId());
				response = constructResponse(buildResponse);
			} catch (Exception any) {
				_log.error("exception in build:", any);
				response = Response.serverError().build();
			}
		} else {
			_log.warn("something went wrong with validation");
		}

		return response;
	}

	private Response constructResponse(BundleBuildResponse buildResponse)
			throws IOException, JsonGenerationException, JsonMappingException {
		final StringWriter sw = new StringWriter();
		final MappingJsonFactory jsonFactory = new MappingJsonFactory();
		final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
		_mapper.writeValue(jsonGenerator, buildResponse);
		return Response.ok(sw.toString()).build();
	}

	/**
	 * Performs various validations on start and end dates
	 * @param bundleStartDateString
	 * @param bundleEndDateString
	 */
	protected void validateDates(String bundleStartDateString, String bundleEndDateString) throws DateValidationException {
		if(StringUtils.isBlank(bundleStartDateString)) {
			throw new DateValidationException(bundleStartDateString, bundleEndDateString);
		}
		if(StringUtils.isBlank(bundleEndDateString)) {
			throw new DateValidationException(bundleStartDateString, bundleEndDateString);
		}
		
		DateTime startDate = ISODateTimeFormat.date().parseDateTime(bundleStartDateString);
		DateTime endDate = ISODateTimeFormat.date().parseDateTime(bundleEndDateString);
		
		if(startDate.isAfter(endDate)) {
			throw new DateValidationException(bundleStartDateString, bundleEndDateString);
		}
	}

	@Path("/{id}/list")
	@GET
	@Produces("application/json")
	public Response list(@PathParam("id") String id) {
		Response response = null;
		if (!isAuthorized()) {
			return Response.noContent().build();
		}
		BundleBuildResponse buildResponse = _bundleService.lookupBuildRequest(id);
		String dirName = buildResponse.getBundleDirectoryName();
		if (dirName == null || dirName.isEmpty()) {
  		buildResponse.setBundleBuildName(bundleName);
  		buildResponse.setBundleDirectoryName(directoryName);
  		buildResponse.setBundleEmailTo(emailTo);
  		buildResponse.setBundleStartDate(startDate);
  		buildResponse.setBundleEndDate(endDate);
  		buildResponse.setBundleComment(comment);
		}
		try {
			final StringWriter sw = new StringWriter();
			final MappingJsonFactory jsonFactory = new MappingJsonFactory();
			final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
			_mapper.writeValue(jsonGenerator, buildResponse);
			response = Response.ok(sw.toString()).build();
		} catch (Exception any){
			_log.error("exception looking up build:", any);
			response = Response.serverError().build();
		}
		return response;
	}

	@Path("/{id}/url")
	@GET
	@Produces("application/json")
	public Response url(@PathParam("id") String id) {
		Response response = null;
		if (!isAuthorized()) {
			return Response.noContent().build();
		}
		BundleBuildResponse buildResponse = _bundleService.lookupBuildRequest(id);
		try {
			response = constructResponse(buildResponse);
		} catch (Exception any){
			_log.error("exception looking up build:", any);
			response = Response.serverError().build();
		}
		return response;
	}

	/**
	 * @param loggingService the loggingService to set
	 */
	@Autowired
	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}

	public LoggingService getLoggingService() {
		return loggingService;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
