/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
import org.onebusaway.admin.service.bundle.BundleValidationService;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * does the actual validation for GtfsFullValidationTask, and stores the results in 
 * GtfsFullValidationTaskJobResult object.
 */
public class GtfsFullValidationTaskJob implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(GtfsFullValidationTaskJob.class);
  
  private BundleValidationService validateService;
  private GtfsBundle gtfsBundle;
  private String outputFile;
  private GtfsFullValidationTaskJobResult result;
  
  public GtfsFullValidationTaskJob(BundleValidationService validateService, 
      GtfsBundle gtfsBundle, 
      String outputFile,
      GtfsFullValidationTaskJobResult result) {
    this.validateService = validateService;
    this.gtfsBundle = gtfsBundle;
    this.outputFile = outputFile;
    this.result = result;
  }
  
  public void run() {
    try {
      String gtfsFilePath = gtfsBundle.getPath().toString();
      _log.info(gtfsBundle.getPath().toString());

      validateService.installAndValidateGtfs(gtfsFilePath, outputFile);
      checkOutputForErrors(gtfsBundle.getDefaultAgencyId(), outputFile);
    } catch (Throwable t) {
      _log.error("exception with gtfsBundle=" + gtfsBundle, t);
    } finally {
      result.setDone();
    }
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
      Elements select = doc.select(".issueHeader:containsOwn(Errors:) ~ ul");
      if (select == null) return;
      Element first = select.first();
      if (first == null) return;
      Elements validationErrors = first.select("li");
      if (validationErrors != null && validationErrors.hasText()) {
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
          result.addError(errorMsgText + "," + errorDetailText);
          
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

