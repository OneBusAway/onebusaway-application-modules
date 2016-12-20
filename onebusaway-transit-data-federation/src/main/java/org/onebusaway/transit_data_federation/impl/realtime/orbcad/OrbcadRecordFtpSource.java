/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime.orbcad:name=OrbcadRecordFtpSource")
public class OrbcadRecordFtpSource extends AbstractOrbcadRecordSource {

  private static final int TIMEOUT_IN_SECONDS = 10;

  private static Logger _log = LoggerFactory.getLogger(OrbcadRecordFtpSource.class);

  private Set<String> _paths = new HashSet<String>();

  private CsvEntityReader _reader;

  private FTPClient _ftpClient = null;

  private FtpDataSource _dataSource;

  private String _dataDirectory;

  private int _maxDownloadCount = 1;

  private transient int _totalFtpFiles = 0;

  private transient int _newFtpFiles = 0;

  public void setDataSource(FtpDataSource dataSource) {
    _dataSource = dataSource;
  }

  public void setDataDirectory(String dataDirectory) {
    _dataDirectory = dataDirectory;
  }

  /****
   * JMX Attributes
   ***/

  @ManagedAttribute
  public int getTotalFtpFiles() {
    return _totalFtpFiles;
  }

  @ManagedAttribute
  public int getNewFtpFiles() {
    return _newFtpFiles;
  }

  /****
   * Setup and Teardown
   ****/

  @PostConstruct
  public void start() throws SocketException, IOException {
    _log.info("starting orbcad ftp download client");
    super.start();
  }

  @PreDestroy
  public void stop() throws IOException {
    _log.info("stopping orbcad ftp download client");
    super.stop();

    if (_ftpClient != null)
      _ftpClient.disconnect();
  }

  /****
   * Private Methods
   ****/

  @Override
  protected void setup() {
    _reader = new CsvEntityReader();

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addEntityClass(OrbcadRecord.class);
    _reader.setEntitySchemaFactory(entitySchemaFactory);

    _reader.addEntityHandler(new RecordHandler());
  }

  @Override
  protected synchronized void handleRefresh() throws IOException {

    try {

      if (_ftpClient == null)
        reconnectFtp();

      List<String> toDownload = getUpdatedFilesToDownload();
      downloadUpdatedFiles(toDownload);

    } catch (IOException ex) {
      _log.error("error refreshing avl files", ex);
      disconnectFtpClient();
    }
  }

  private void reconnectFtp() throws SocketException, IOException {

    _log.info("attempting to establish ftp connection");

    disconnectFtpClient();

    _ftpClient = new FTPClient();

    _ftpClient.setConnectTimeout(TIMEOUT_IN_SECONDS * 1000);
    _ftpClient.setDataTimeout(TIMEOUT_IN_SECONDS * 1000);
    _ftpClient.setDefaultTimeout(TIMEOUT_IN_SECONDS * 1000);

    _ftpClient.connect(_dataSource.getServername(), _dataSource.getPort());
    _ftpClient.login(_dataSource.getUsername(), _dataSource.getPassword());

    _ftpClient.enterLocalPassiveMode();
    _log.info("ftp connection established");
  }

  private List<String> getUpdatedFilesToDownload() throws IOException {
    long t1 = SystemTime.currentTimeMillis();

    FTPListParseEngine engine = _ftpClient.initiateListParsing(_dataDirectory);

    Set<String> paths = new HashSet<String>();
    List<String> toDownload = new ArrayList<String>();

    while (engine.hasNext()) {
      FTPFile[] files = engine.getNext(25); // "page size" you want
      for (FTPFile file : files) {
        String path = _dataDirectory + "/" + file.getName();
        paths.add(path);
        if (!_paths.contains(path))
          toDownload.add(path);
      }
    }

    _totalFtpFiles = paths.size();
    _newFtpFiles = toDownload.size();

    long t2 = SystemTime.currentTimeMillis();

    if (_log.isDebugEnabled())
      _log.debug("file listing time: " + (t2 - t1) + " totalFiles: "
          + paths.size() + " newFiles: " + toDownload.size());

    _paths = paths;

    if (_maxDownloadCount > 0 && toDownload.size() > _maxDownloadCount) {
      List<String> reduced = new ArrayList<String>(_maxDownloadCount);
      for (int i = 0; i < _maxDownloadCount; i++)
        reduced.add(toDownload.get(toDownload.size() - _maxDownloadCount + i));
      toDownload = reduced;
    }

    return toDownload;
  }

  private void downloadUpdatedFiles(List<String> toDownload) throws IOException {
    for (String path : toDownload) {

      _log.debug("downloading path: {}", path);

      long t3 = SystemTime.currentTimeMillis();
      InputStream in = _ftpClient.retrieveFileStream(path);

      if (!FTPReply.isPositivePreliminary(_ftpClient.getReplyCode())) {
        _log.warn("error initiating file transfer: "
            + _ftpClient.getReplyCode() + " " + _ftpClient.getReplyString());
        continue;
      }

      _reader.readEntities(OrbcadRecord.class, in);
      in.close();

      if (!_ftpClient.completePendingCommand()) {
        _log.warn("error completing file transfer: "
            + _ftpClient.getReplyCode() + " " + _ftpClient.getReplyString());
        continue;
      }

      long t4 = SystemTime.currentTimeMillis();
      if (_log.isDebugEnabled())
        _log.info("file download time: " + (t4 - t3));
    }
  }

  private void disconnectFtpClient() {
    try {
      if (_ftpClient != null)
        _ftpClient.disconnect();
    } catch (Throwable t) {

    } finally {
      _ftpClient = null;
    }
  }
}
