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

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onebusaway.admin.service.BundleArchiverService;
import org.onebusaway.admin.service.bundle.BundleStager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonParser;

@Component
@Scope("singleton")
public class LocalBundleArchiverServiceImpl implements BundleArchiverService {

  private static Logger _log = LoggerFactory.getLogger(LocalBundleArchiverServiceImpl.class);

  @Autowired
  private BundleStager bundleStager;

  private String getBundleId(File bundleDir) {
    try {
      String bundleId = new JsonParser().parse(
          new FileReader(bundleDir.getAbsolutePath() + File.separator
              + "outputs" + File.separator + "metadata.json")).getAsJsonObject().get(
          "id").getAsString();
      return bundleId;
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public Response getArchiveBundleList() {
    JSONArray response = new JSONArray();
    try {
      for (File datasetDir : new File(bundleStager.getBuiltBundleDirectory()).listFiles()) {
        String buildsDir = datasetDir.getAbsolutePath() + File.separator
            + "builds";
        try {
          for (File bundleDir : new File(buildsDir).listFiles()) {
            JSONObject bundleResponse = new JSONObject();
            bundleResponse.put("id", getBundleId(bundleDir));
            bundleResponse.put("dataset", datasetDir.getName());
            bundleResponse.put("name", bundleDir.getName());
            response.put(bundleResponse);
          }
        } catch (Exception e1) {
          _log.error("Failed to read from: " + buildsDir);
        }
      }
      return Response.ok(response.toString(), "application/json").build();
    } catch (Exception e2) {
      return Response.serverError().build();
    }
  }

  @Override
  public Response getArchiveBundleByName(String dataset, String name) {
    File bundleDir = new File(bundleStager.getBuiltBundleDirectory() + "/"
        + dataset + "/builds/" + name);
    return getContents(bundleDir);
  }

  @Override
  public Response getArchiveBundleById(String id) {
    return getArchiveBundleById(id, null);
  }
  
  @Override
  public Response getArchiveBundleById(String id, String filter) {
    try {
      for (File datasetDir : new File(bundleStager.getBuiltBundleDirectory()).listFiles()) {
        File buildsDir = new File(datasetDir.getAbsolutePath() + "/builds");
        if (buildsDir.exists() && buildsDir.listFiles().length > 0) {
          for (File bundleDir : buildsDir.listFiles()) {
            try {
              if (getBundleId(bundleDir).equals(id)) {
                return getContents(bundleDir, filter);
              }
            } catch (Exception e1) {
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Response.serverError().build();
  }

  private Response getContents(File bundleDir) {
      return getContents(bundleDir, null);
  }
  
  @SuppressWarnings("unchecked")
  private Response getContents(File bundleDir, String filter) {
    Collection<File> files = FileUtils.listFiles(bundleDir,
        new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
    JSONArray response = new JSONArray();
    for (File file : files) {
      URI relativeName = bundleDir.toURI().relativize(file.toURI());
      if (filter != null) {
        if (relativeName.toString().contains(filter)) {
          response.put(relativeName);
        } 
      } else {
        response.put(relativeName);
      }
    }
    return Response.ok(response.toString(), "application/json").build();
  }

  @Override
  public Response getFileByName(String dataset, String name, String file) {
    try {
      return Response.ok(
          new File(bundleStager.getBuiltBundleDirectory() + "/" + dataset
              + "/builds/" + name + "/" + file), "application/json").build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  @Override
  public Response getFileById(String id, String file) {
    try {
      for (File datasetDir : new File(bundleStager.getBuiltBundleDirectory()).listFiles()) {
        File buildsDir = new File(datasetDir.getAbsolutePath() + "/builds");
        if (buildsDir.exists() && buildsDir.listFiles().length > 0) {
          for (File bundleDir : buildsDir.listFiles()) {
            try {
              if (getBundleId(bundleDir).equals(id)) {
                String filepath = bundleDir.getAbsolutePath() + "/" + file;
                return Response.ok(new File(filepath), "application/json").build();
              }
            } catch (Exception e1) {
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Response.serverError().build();
  }
}
