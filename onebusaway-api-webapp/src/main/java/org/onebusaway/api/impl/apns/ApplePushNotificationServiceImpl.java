package org.onebusaway.api.impl.apns;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.api.services.apns.ApplePushNotificationService;
import org.springframework.core.io.Resource;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;

class ApplePushNotificationServiceImpl implements ApplePushNotificationService {

  private Resource _keystoreResource;

  private String _keystorePassword;

  private boolean _production = false;

  private ApnsService _service;

  public void setKeystoreResource(Resource keystoreResource) {
    _keystoreResource = keystoreResource;
  }

  public void setKeystorePassword(String keystorePassword) {
    _keystorePassword = keystorePassword;
  }

  public void setProduction(boolean production) {
    _production = production;
  }

  @PostConstruct
  public void start() throws Exception {
    ApnsServiceBuilder builder = APNS.newService();
    InputStream in = _keystoreResource.getInputStream();
    builder.withCert(in, _keystorePassword);

    if (_production)
      builder.withProductionDestination();
    else
      builder.withSandboxDestination();

    _service = builder.build();
    _service.start();
  }

  @PreDestroy
  public void stop() throws IOException {
    _service.stop();
  }

  /****
   * {@link ApplePushNotificationService} Interface
   ****/

  public void pushNotification(String deviceToken, String payload) {
    _service.push(deviceToken, payload);
  }
}
