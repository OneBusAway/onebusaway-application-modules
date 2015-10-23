package org.onebusaway.util.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.util.impl.configuration.ConfigurationServiceClientFileImpl;
import org.onebusaway.util.impl.configuration.ConfigurationServiceImpl;
import org.onebusaway.util.rest.RestApiLibrary;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplLocalTest {

  @SuppressWarnings("unused")
  @Mock
  private RefreshService refreshService;

  @Mock
  private ConfigurationServiceClient mockApiLibrary;

  @Mock
  private RestApiLibrary mockRestApiLibrary;

  @InjectMocks
  private ConfigurationServiceImpl service;

  @Before
  public void setupLocalConfig() throws Exception {
	  
	String config = getClass().getResource("config.json").getFile();

    ConfigurationServiceClient configServiceClient = new ConfigurationServiceClientFileImpl(config);
    
    service.setConfigurationServiceClient(configServiceClient);

    service.refreshConfiguration();
  }

  @Test
  public void testGetExistingValueFromTDM() throws Exception {
    assertEquals("120",service.getConfigurationValueAsString("display.staleTimeout", "190"));
  }

 /* @Test
  public void testGetExistingValueFromTDMDefaultOverridden() throws Exception {
    assertEquals(service.getConfigurationValueAsString("tdm.crewAssignmentRefreshInterval", "30"), "20");
  }*/
}
