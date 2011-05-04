package org.onebusaway.api.impl.apns;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.api.services.apns.ApplePushNotificationService;
import org.onebusaway.utility.IOLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;

class ApplePushNotificationServiceImpl implements ApplePushNotificationService {

  private static Logger _log = LoggerFactory.getLogger(ApplePushNotificationServiceImpl.class);

  private Resource _keystoreResource;

  private String _keystorePassword;

  private boolean _production = false;

  private ApnsService _service;

  private ScheduledExecutorService _executor;

  private File _inactiveDevicesPath;

  private ConcurrentMap<String, Date> _inactiveDevices = new ConcurrentHashMap<String, Date>();

  public void setKeystoreResource(Resource keystoreResource) {
    _keystoreResource = keystoreResource;
  }

  public void setKeystorePassword(String keystorePassword) {
    _keystorePassword = keystorePassword;
  }

  public void setProduction(boolean production) {
    _production = production;
  }

  public void setInactiveDevicesPath(File inactiveDevicesPath) {
    _inactiveDevicesPath = inactiveDevicesPath;
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

    loadInactiveDevices();

    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(new InactiveDevicesTask(), 0, 12,
        TimeUnit.HOURS);
  }

  @PreDestroy
  public void stop() throws IOException {

    saveInactiveDevices();

    if (_executor != null)
      _executor.shutdownNow();
    if (_service != null)
      _service.stop();
  }

  /****
   * {@link ApplePushNotificationService} Interface
   ****/

  public void pushNotification(String deviceToken, String payload) {

    if (_inactiveDevices.containsKey(deviceToken)) {
      _log.info("attempt to send push notification to inactive device: "
          + deviceToken);
      return;
    }

    _service.push(deviceToken, payload);
  }

  /****
   * Private Methods
   ****/

  private void loadInactiveDevices() {

    if (_inactiveDevicesPath == null || !_inactiveDevicesPath.exists())
      return;

    try {

      BufferedReader reader = IOLibrary.getFileAsBufferedReader(_inactiveDevicesPath);
      String line = null;

      Date now = new Date();
      while ((line = reader.readLine()) != null) {
        _inactiveDevices.put(line, now);
      }

      reader.close();

    } catch (IOException ex) {
      _log.warn("error reading inactive devices from " + _inactiveDevicesPath,
          ex);
    }
  }

  private void saveInactiveDevices() {

    if (_inactiveDevicesPath == null)
      return;

    try {
      PrintWriter writer = new PrintWriter(
          IOLibrary.getFileAsWriter(_inactiveDevicesPath));
      for (String deviceToken : _inactiveDevices.keySet()) {
        writer.println(deviceToken);
      }
      writer.close();
    } catch (IOException ex) {
      _log.warn("error reading inactive devices from " + _inactiveDevicesPath,
          ex);
    }
  }

  private class InactiveDevicesTask implements Runnable {

    @Override
    public void run() {

      Map<String, Date> inactiveDevices = _service.getInactiveDevices();
      _inactiveDevices.putAll(inactiveDevices);
      saveInactiveDevices();
    }
  }

}
