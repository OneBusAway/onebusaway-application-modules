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
package org.onebusaway.admin.service.bundle.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.bundle.BundleProvider;
import org.onebusaway.admin.bundle.BundlesListMessage;
import org.onebusaway.admin.bundle.model.Bundle;
import org.onebusaway.admin.bundle.model.BundleStatus;
import org.onebusaway.admin.json.JsonTool;
import org.onebusaway.admin.service.BundleStagerService;
import org.onebusaway.admin.service.bundle.BundleStager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;


@Component
@Scope("singleton")
public class LocalBundleStagerServiceImpl implements BundleStagerService{

    private static Logger _log = LoggerFactory.getLogger(LocalBundleStagerServiceImpl.class);

    private ExecutorService _executorService = null;
    
    @Autowired
    @Qualifier("bundleStagingProvider")
    private BundleProvider bundleProvider;
    
    @Autowired
    private BundleStager bundleStager;
    
    @Autowired
    private JsonTool jsonTool;

    private Map<String, BundleStatus> _stageMap = new HashMap<String, BundleStatus>();
    private Integer jobCounter = 0;

    @PostConstruct
    public void setup() {
        _executorService = Executors.newFixedThreadPool(1);
    }
    
    @PreDestroy
    public void stop() {
      _executorService.shutdownNow();
    }
    
    public BundleStatus lookupStagedRequest(String id) {
      return _stageMap.get(id);
    }
    
    public void setBundleProvider(BundleProvider bundleProvider) {
      this.bundleProvider = bundleProvider;
    }

    public void setJsonTool(JsonTool jsonTool) {
      this.jsonTool = jsonTool;
    }
    

    /**
     * request bundles at /var/lib/obanyc/bundles/staged/{environment} be staged
     * @param environment string representing environment (dev/staging/prod/qa)
     * @return status object with id for querying status
     */
    @Override
    public Response stage(String environment, String bundleDir, String bundlePath) {
      _log.info("Starting staging(" + environment + ")...");
      BundleStatus status = new BundleStatus();
      status.setId(getNextId());
      _stageMap.put(status.getId(), status);
      _executorService.execute(new StageThread(status, environment, bundleDir, bundlePath));
      _log.info("stage request complete");

      try {
        String jsonStatus = jsonSerializer(status);
        return Response.ok(jsonStatus).build();
      } catch (Exception e) {
        _log.error("exception serializing response:", e);
      }
      return Response.serverError().build();
    }

    /**
     * query the status of a requested bundle deployment
     * @param id the id of a BundleStatus
     * @return a serialized version of the requested BundleStatus, null otherwise
     */
    @Override
    public Response stageStatus(String id) {
      BundleStatus status = this.lookupStagedRequest(id);
      try {
        String jsonStatus = jsonSerializer(status);
        return Response.ok(jsonStatus).build();
      } catch (Exception e) {
        _log.error("exception serializing response:", e);
      }
      return Response.serverError().build();
    }
    
    @Override
    public Response getBundleList() {
      _log.info("Starting getBundleList.");
  
      List<Bundle> bundles = bundleProvider.getBundles();
  
      Response response;
  
      if (bundles != null) {
        BundlesListMessage bundlesMessage = new BundlesListMessage();
        bundlesMessage.setBundles(bundles);
        bundlesMessage.setStatus("OK");
  
        final BundlesListMessage bundlesMessageToWrite = bundlesMessage;
  
        StreamingOutput output = new StreamingOutput() {
  
          @Override
          public void write(OutputStream out) throws IOException,
              WebApplicationException {
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out));
  
            jsonTool.writeJson(writer, bundlesMessageToWrite);
  
            writer.close();
            out.close();
  
          }
        };
        response = Response.ok(output, "application/json").build();
      } else {
        response = Response.serverError().build();
      }

      _log.info("Returning Response in getBundleList.");
      return response;
    }

    @Override
    public Response getBundleFile(String bundleId, String relativeFilename) {

      _log.info("starting getBundleFile for relative filename " + relativeFilename + " in bundle " + bundleId);

      boolean requestIsForValidBundleFile = bundleProvider.checkIsValidBundleFile(
          bundleId, relativeFilename);
      if (!requestIsForValidBundleFile) {
        throw new WebApplicationException(new IllegalArgumentException(
            relativeFilename + " is not listed in bundle metadata."),
            Response.Status.BAD_REQUEST);
      }

      final File requestedFile;
      try {
        requestedFile = bundleProvider.getBundleFile(bundleId, relativeFilename);

      } catch (FileNotFoundException e) {
        _log.info("FileNotFoundException loading " + relativeFilename + " in "
            + bundleId + " bundle.");
        throw new WebApplicationException(e,
            Response.Status.INTERNAL_SERVER_ERROR);
      }

      long fileLength = requestedFile.length();

      StreamingOutput output = new StreamingOutput() {

        @Override
        public void write(OutputStream os) throws IOException,
            WebApplicationException {

          FileChannel inChannel = null;
          WritableByteChannel outChannel = null;

          try {
            inChannel = new FileInputStream(requestedFile).getChannel();
            outChannel = Channels.newChannel(os);

            inChannel.transferTo(0, inChannel.size(), outChannel);
          } finally {
            if (outChannel != null)
              outChannel.close();
            if (inChannel != null)
              inChannel.close();
          }

        }
      };


      ContentDisposition cd = ContentDisposition.builder("file").filename(requestedFile.getName()).build();

      Response response = Response.ok(output, MediaType.APPLICATION_OCTET_STREAM).header(
          "Content-Disposition", cd).header("Content-Length", fileLength).build();

      _log.info("Returning Response in getBundleFile");

      return response;
    }

    
    /**
     * Trivial implementation of creating unique Ids. Security is not a
     * requirement here.
     */
    private String getNextId() {
      return "" + inc();
    }

    private Integer inc() {
      synchronized (jobCounter) {
        jobCounter++;
      }
      return jobCounter;
    }
    
    /*private String getBundleDirectory() {
      return bundleStager.getStagedBundleDirectory();
    }*/
    
    private String jsonSerializer(Object object) throws IOException{
      //serialize the status object and send to client -- it contains an id for querying
      final StringWriter sw = new StringWriter();
      final MappingJsonFactory jsonFactory = new MappingJsonFactory();
      final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(jsonGenerator, object);
      return sw.toString();
    }
    
    /**
     * Thread to perform the actual deployment of the bundle.
     *
     */
    private class StageThread implements Runnable {
      private BundleStatus status;
      private String environment;
      private String bundleDir;
      private String bundleName;
      
      public StageThread(BundleStatus status, String environment, String bundleDir, String bundleName){
        this.status = status;
        this.environment = environment;
        this.bundleDir = bundleDir;
        this.bundleName = bundleName;
      }
      
      @Override
      public void run() {
        bundleStager.stage(status, environment, bundleDir, bundleName);
      }
    }
}
