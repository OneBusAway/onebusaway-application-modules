/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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

import org.onebusaway.admin.service.BundleUploadService;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.util.BundleInfo;
import org.onebusaway.admin.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
/**
 * Service to manage the status of uploading GTFS to admin console back-end
 */
public class LocalBundleUploadServiceImpl implements BundleUploadService {

  private JsonUtil jsonUtil = new JsonUtil();
  @Autowired
  private BundleInfo bundleInfo;
  private Map<String, UploadResponse> statusMap = new HashMap<>();

  private static Logger _log = LoggerFactory.getLogger(LocalBundleUploadServiceImpl.class);

  @Autowired
  private FileService fileService;

  @Override
  public Response register(String agencyId, String bundleDir, String uploadType, String uploadUrl) {
    UploadResponse status = new UploadResponse();
    status.status = "initial";

    String target = fileService.getBucketName()
            + File.separator + bundleDir
            + File.separator + "gtfs_latest"
            + File.separator + agencyId
            + File.separator + agencyId + ".zip";
    _log.info("requesting thread for copy of {} to {} for {}", uploadUrl, target, agencyId);
    UploadThread thread = new UploadThread(status, bundleInfo,
            agencyId, bundleDir, target, uploadUrl);
    thread.start();
    statusMap.put(hash(agencyId, bundleDir),
                  status);
    try {
      return Response.ok(jsonSerializer(status)).build();
    } catch (IOException e) {
      _log.error("parsing error", e);
    }
    return Response.serverError().build();
  }

  @Override
  public Response query(String agencyId, String bundleDir) {
    UploadResponse uploadResponse = statusMap.get(hash(agencyId, bundleDir));
    if (uploadResponse == null) {
      return Response.serverError().build();
    }
    try {
      return Response.ok(jsonSerializer(uploadResponse)).build();
    } catch (IOException e) {
      _log.error("parsing error", e);
    }
    return Response.serverError().build();
  }

  private String hash(String agencyId, String bundleDir) {
    return agencyId + "." + bundleDir;
  }

  private String jsonSerializer(Object object) throws IOException {
    return jsonUtil.serialize(object);
  }

  public static class UploadThread extends Thread {
    private String agencyId;
    private String bundleDir;
    private String target;
    private String uploadUrl;
    private UploadResponse status;
    private BundleInfo bundleInfo;
    public UploadThread(UploadResponse status, BundleInfo bundleInfo, String agencyId, String bundleDir, String target,
                        String uploadUrl) {
      this.status = status;
      this.target = target;
      this.uploadUrl = uploadUrl;
      this.bundleDir = bundleDir;
      this.bundleInfo = bundleInfo;
      this.agencyId = agencyId;
    }
    public void run() {
      // Copy file
      try {
        status.status = "in queue";
        URL website = new URL(uploadUrl);
        File targetPath = new File(target);
        targetPath.mkdirs();
        status.status = "downloading";
        Files.copy(website.openStream(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        status.status = "done";
        Date uploadDate = new Date();
        status.uploadDate = new SimpleDateFormat("MMM dd yyyy").format(uploadDate);
        boolean updateSuccess = bundleInfo.addFileToBundleForDirectoryName(bundleDir,
                agencyId,
                "http",
                uploadDate);
        if (!updateSuccess) {
          status.status = "json error";
        }
      } catch (Exception e) {
        status.status = "error";
        _log.info(e.getMessage());
      }
    }
  }

  public static class UploadResponse {
    private String status;
    private String uploadDate;
    public String getStatus() {
      return status;
    }
    public String getUploadDate() {
      return uploadDate;
    }
  }
}
