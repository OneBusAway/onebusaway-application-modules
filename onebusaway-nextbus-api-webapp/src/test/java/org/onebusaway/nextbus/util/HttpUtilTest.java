/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.nextbus.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpUtilTest {
	
	private HttpUtilImpl httpUtil;

	@Mock
	private CloseableHttpResponse response;

	@Mock
	private HttpEntity entity;

	@Mock
	private StatusLine statusLine;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		httpUtil = new HttpUtilImpl();
	}
	
	/**
	 * Only individual query string parameter names or values should be encoded, not the entire URL.
	 * Query String separator characters such as '&' and key-value seperators such as '=' should not be encoded 
	 */
	@Test
	public void testUrlParamterAndValuesEncoding() {
		
		String preEncodedUrlWithPipe = "http://localhost:8080/api/v1/key/123456/agency/1/command/predictions?"
				+ "rs=16E|6010&rs=52|6010&rs=53|6010&rs=54|6010&rs=D4|6010&format=json";	
		String expectedEncodedUrlWithPipe =  "http://localhost:8080/api/v1/key/123456/agency/1/command/predictions?"
				+ "rs=16E%7C6010&rs=52%7C6010&rs=53%7C6010&rs=54%7C6010&rs=D4%7C6010&format=json";
		assertEquals(expectedEncodedUrlWithPipe,httpUtil.getEncodedUrl(preEncodedUrlWithPipe));
		
		
		String preEncodedUrlWithSpacePipe = "http://localhost:8080/api/v1/key/123456/agency/1/command/predictions?"
				+ "rs=16E|6010&rs=52 |6010&rs=53 |6010&rs=54 |6010&rs=D4 |6010&format=json";
		String expectedEncodedUrlWithSpacePipe =  "http://localhost:8080/api/v1/key/123456/agency/1/command/predictions?"
				+ "rs=16E%7C6010&rs=52+%7C6010&rs=53+%7C6010&rs=54+%7C6010&rs=D4+%7C6010&format=json";
		assertEquals(expectedEncodedUrlWithSpacePipe,httpUtil.getEncodedUrl(preEncodedUrlWithSpacePipe));
	}
	
	/**
	 * If no question mark separator is available, original unencoded URL is returned 
	 */
	@Test
	public void testNoQuerySeparatorEncoding() {
		String preEncodedPredictionsUrl = "http://localhost:8080/api/v1/key/123456/agency/1/command/predictions/"
				+ "rs=16E|6010&rs=52|6010&rs=53|6010&rs=54|6010&rs=D4|6010&format=json";
		
		assertEquals(preEncodedPredictionsUrl,httpUtil.getEncodedUrl(preEncodedPredictionsUrl));
	}

	@Test
	public void testEmptyResponseError(){
		try{
			httpUtil.getEntity(null);
		}
		catch(Exception e){
			assertEquals("Received a Null response", e.getMessage());
		}
	}

	@Test
	public void testEmptyEntityError(){
		when(response.getEntity()).thenReturn(null);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		try{
			httpUtil.getEntity(response);
		}
		catch(Exception e){
			assertEquals("Failed to retrieve a valid HttpEntity object from the response", e.getMessage());
		}
	}

	@Test
	public void testBadStatusError(){
		when(response.getEntity()).thenReturn(null);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
		try{
			httpUtil.getEntity(response);
		}
		catch(Exception e){
			assertEquals("HTTP Response: " + HttpStatus.SC_NOT_FOUND, e.getMessage());
		}
	}
}
