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
package org.onebusaway.admin.service.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.admin.util.ConfigurationKeyTranslator;
import org.onebusaway.util.impl.configuration.ConfigParameter;
import org.onebusaway.util.services.configuration.ConfigurationService;

/**
 * Tests {@link ParametersService}
 * @author abelsare
 *
 */
public class ParametersServiceImplTest {

	@Mock
	private ConfigurationService configurationService;
	
	private ConfigurationKeyTranslator keyTranslator;
	
	private ParametersServiceImpl service;
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		keyTranslator = new ConfigurationKeyTranslator();
		
		service = new ParametersServiceImpl();
		service.setConfigurationService(configurationService);
		service.setKeyTranslator(keyTranslator);
	}
	
	@Test
	public void testGetParameters() {

		String config = getClass().getResource("config.json").getFile();
		service.setConfigFile(config);

		Map<String,String> displayParameters = service.getParameters();
		
		assertTrue("Expecting agency to be in map",
				displayParameters.containsKey("1_hideScheduleInfo"));

		assertEquals("Expecting value to be associated with the translated key", "true",
						displayParameters.get("1_hideScheduleInfo"));
		
	}

	@Test
	public void testSaveParameters() throws Exception {
		Map<String, List<ConfigParameter>> configParameters = createTestParams();
		File tmpFile = File.createTempFile("config-", ".json");
		tmpFile.deleteOnExit();
		URI uri = getClass().getResource("config.json").toURI();
		File configFile = new File(uri.getPath());
		assertTrue(configFile.exists());
		assertTrue(configFile.isFile());
		Files.copy(configFile, tmpFile);
		assertTrue(tmpFile.exists());
		assertTrue(tmpFile.isFile());
		service.setConfigFile(tmpFile.toURI().getPath());
		configParameters.get("1").get(0).setValue("tada_surprise");
		boolean success = service.saveParameters(configParameters);
		assertFileHasText(tmpFile, "tada_surprise");
		assertTrue("Expecting save operation to be successful", success);
		
	}

	private void assertFileHasText(File file, String searchText) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[fis.available()];
		fis.read(data);
		String text = new String(data);
		Matcher m = Pattern.compile(searchText).matcher(text);
		assertTrue("expected to find " + searchText, m.find());
	}

	@Test
	public void testSaveParametersException() throws Exception {
		Map<String, List<ConfigParameter>> configParameters = createTestParams();
		// an empty file throws an exception, we must have a valid json file
		File tmpFile = File.createTempFile("config-", ".json");
		tmpFile.deleteOnExit();
		service.setConfigFile(tmpFile.toURI().getPath());
		// force an exception and then confirm success is false
		boolean success = service.saveParameters(configParameters);
		
		assertFalse("Expecting save operation to fail", success);
		
	}

	private Map<String, List<ConfigParameter>> createTestParams() {
		Map<String, List<ConfigParameter>> configParameters = new HashMap<>();
		ConfigParameter param = new ConfigParameter();
		param.setDisplayName("A City Transit Agency");
		param.setKey("1_hideScheduleInfo");
		param.setValue("true");
		configParameters.put("1", Arrays.asList(param));
		return configParameters;
	}

}
