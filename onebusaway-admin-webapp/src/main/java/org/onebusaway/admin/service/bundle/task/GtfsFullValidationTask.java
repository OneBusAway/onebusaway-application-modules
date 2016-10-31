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
package org.onebusaway.admin.service.bundle.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.bundle.BundleValidationService;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class GtfsFullValidationTask implements  Runnable {
  private static Logger _log = LoggerFactory.getLogger(GtfsFullValidationTask.class);
  protected ApplicationContext _applicationContext;

  private BundleRequestResponse requestResponse;
  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }

  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }

  @Autowired
  private BundleValidationService _validateService;

  @Autowired
  protected MultiCSVLogger _logger;

  public void setValidateService(BundleValidationService validateService) {
    _validateService = validateService;
  }

  public void setLogger(MultiCSVLogger logger) {
    _logger = logger;
  }

  @Override
  public void run() {
    _log.info("GtfsFullValidationTask Starting");
    // Only run  this on a Final build
    if (!requestResponse.getRequest().getArchiveFlag()) {
      _log.info("archive flag not set, GtfsFullValidationTask exiting");
      return;
    }

    GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
      File gtfsFile = gtfsBundle.getPath();
      String gtfsFileName = gtfsFile.getName();
      String gtfsFilePath = gtfsBundle.getPath().toString();
      String outputFile = requestResponse.getResponse().getBundleOutputDirectory() 
          + "/" + gtfsFileName + ".html";
      _log.info(gtfsBundle.getPath().toString());
      try {
        _validateService.installAndValidateGtfs(gtfsFilePath, outputFile);
        _logger.header(gtfsFileName + ".html", "", "");  // To add into summary.csv
        checkOutputForErrors(gtfsBundle.getDefaultAgencyId(), outputFile);
      } catch (Exception any) {
        _log.error("GtfsFullValidationTask failed:", any);
      } finally {
        _log.info("GtfsFullValidationTask Exiting");
      }
    }
    return;
  }

  protected GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }
/** This method will parse the validation output HTML file checking to see
 * if any errors were found during the validation.  If any were, a summary csv
 * file is created listing the errors.
 *
 * @param agencyId - the agency id of the HTML file being checked
 * @param outputFile - the name of the HTML file to be checked.
 * @throws IOException
 */
  private void checkOutputForErrors(String agencyId, String outputFile)
      throws IOException {
    File validationHtmlFile = new File(outputFile);
    Document doc = Jsoup.parse(validationHtmlFile, "UTF-8");
    Elements validationErrors = doc.select(".issueHeader:containsOwn(Errors:) ~ ul").first().select("li");
    if (validationErrors != null && validationErrors.hasText()) {
      String csvFileName = agencyId + "_gtfs_validation_errors.csv";
      _logger.header(csvFileName, "Error Message, Error Detail");
      for (Node parentNode : validationErrors) { // for each <li>
        String errorMsgText = "";
        String errorDetailText = "";
        for (Node node : parentNode.childNodes()) {
          if (node instanceof TextNode) {
            errorMsgText += ((TextNode) node).text();
          } else if (node instanceof Element) {
            String tagName = ((Element)node).tagName();
            if (tagName.equals("br")) {
              errorMsgText += " ";
            } else if (tagName.equals("div")) {
              errorMsgText += parseDivData(node);
            } else if (tagName.equals("table")) {
              errorDetailText = parseTableData(node);
            } else {
              errorMsgText += ((Element)node).text();
            }
          }
        }
        _logger.logCSV(csvFileName, errorMsgText + "," + errorDetailText);
      }
    }
  }

  private String parseDivData (Node divNode) {
    String parsedText = "";
    for (Node node : divNode.childNodes()) {
      if (node instanceof TextNode) {
        parsedText += ((TextNode) node).text();
      } else if (node instanceof Element) {
        String tagName = ((Element)node).tagName();
        if (tagName.equals("br")) {
          parsedText += " ";
        } else if (tagName.equals("div")) {
          parsedText += parseDivData(node);
        } else if (tagName.equals("table")) {
          parsedText += parseTableData(node);
        } else {
          parsedText += " " + ((Element)node).text();
        }
      }
    }
    return parsedText;
  }

  private String parseTableData (Node node) {
    String parsedData = "";
    // Get the headers and data from the table
    Element detailsTable = ((Element)node).getElementsByClass("dump").first();
    List<String> headers = new ArrayList<>();
    List<String> details = new ArrayList<>();
    Iterator<Element> headerIterator = detailsTable.select("th").iterator();
    Iterator<Element> detailsIterator = detailsTable.select("td").iterator();
    while (headerIterator.hasNext() && detailsIterator.hasNext()) {
      headers.add(headerIterator.next().text());
      details.add(detailsIterator.next().text());
    }
    for (int i=0; i<headers.size(); i++) {
      parsedData += headers.get(i) + ":" + details.get(i) + "  ";
    }
    return parsedData;
  }
}
