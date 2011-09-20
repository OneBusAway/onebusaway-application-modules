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
package org.onebusaway.presentation.impl.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;

import org.onebusaway.presentation.services.resources.ResourceService;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

import com.steadystate.css.parser.SACParserCSS2;

public class CssResourceTransformationStrategy implements
    ResourceTransformationStrategy {

  static Pattern URL_PATTERN = Pattern.compile("^@url (\\w+) (\\w+);$");

  private Locale _locale;

  public CssResourceTransformationStrategy(Locale locale) {
    _locale = locale;
  }

  /****
   * {@link ResourceTransformationStrategy} Interface
   ****/

  @Override
  public boolean requiresTransformation() {
    return true;
  }

  @Override
  public void transformResource(ResourceService resourceService,
      URL sourceResource, File targetResource) {

    try {

      String outputText = parse(resourceService, sourceResource);

      BufferedWriter writer = new BufferedWriter(new FileWriter(targetResource));
      writer.write(outputText);
      writer.close();

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private String parse(ResourceService resourceService, URL url)
      throws IOException {
    InputStreamReader reader2 = new InputStreamReader(url.openStream());
    InputSource source2 = new InputSource(reader2);
    Parser p = new SACParserCSS2();
    CssDocumentHandler handler = new CssDocumentHandler(resourceService,_locale);
    p.setDocumentHandler(handler);
    p.parseStyleSheet(source2);
    reader2.close();
    return handler.getResults();
  }
}
