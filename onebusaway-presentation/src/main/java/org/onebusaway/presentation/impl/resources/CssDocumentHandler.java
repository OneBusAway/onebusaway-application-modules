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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.presentation.services.resources.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

import com.steadystate.css.sac.DocumentHandlerExt;

/*****************************************************************************
 * Internal Classes
 ****************************************************************************/

class CssDocumentHandler implements DocumentHandlerExt {

  private static Logger _log = LoggerFactory.getLogger(CssDocumentHandler.class);

  private static final Pattern _obaResourcePattern = Pattern.compile("^oba-resource\\((.*)\\)$");

  private StringBuilder _buffer = new StringBuilder();

  private ResourceService _resourceService;

  private Locale _locale;

  public CssDocumentHandler(ResourceService resourceService, Locale locale) {
    _resourceService = resourceService;
    _locale = locale;
  }

  public String getResults() {
    return _buffer.toString();
  }

  public void startDocument(InputSource source) throws CSSException {

  }

  public void endDocument(InputSource source) throws CSSException {

  }

  public void comment(String comment) throws CSSException {

  }

  public void startFontFace() throws CSSException {
    _log.debug("start font face");
  }

  public void endFontFace() throws CSSException {
    _log.debug("end font face");
  }

  public void importStyle(String uri, SACMediaList media,
      String defaultNamespaceUri) throws CSSException {

  }

  public void ignorableAtRule(String atRule) throws CSSException {

  }

  public void namespaceDeclaration(String arg0, String arg1)
      throws CSSException {

  }

  public void startMedia(SACMediaList media) throws CSSException {

  }

  public void endMedia(SACMediaList media) throws CSSException {

  }

  public void startPage(String name, String pseudoPage) throws CSSException {

  }

  public void endPage(String name, String pseudoPage) throws CSSException {

  }

  public void startSelector(SelectorList selectors) throws CSSException {
    for (int i = 0; i < selectors.getLength(); i++) {
      Selector selector = selectors.item(i);
      if (i > 0)
        _buffer.append(',');
      _buffer.append(selector.toString());
    }
    _buffer.append("{");
  }

  public void property(String name, LexicalUnit unit, boolean important)
      throws CSSException {

    _buffer.append(name);
    _buffer.append(':');
    int index = 0;
    while (unit != null) {
      if (index > 0)
        _buffer.append(" ");
      String v = unit.toString();
      v = substitute(v);
      _buffer.append(v);
      unit = unit.getNextLexicalUnit();
      index++;
    }
    _buffer.append(';');
  }

  public void endSelector(SelectorList selectors) throws CSSException {
    _buffer.append('}').append('\n');
  }

  public void charset(String characterEncoding) throws CSSException {

  }

  /***************************************************************************
   * 
   **************************************************************************/

  private String substitute(String id) {

    if (id.startsWith("oba-")) {
      Matcher m = _obaResourcePattern.matcher(id);
      if (m.matches()) {
        String url = m.group(1);

        if (url.startsWith("\"") && url.endsWith("\""))
          url = url.substring(1, url.length() - 1);
        else if (url.startsWith("'") && url.endsWith("'"))
          url = url.substring(1, url.length() - 1);

        String externalUrl = _resourceService.getExternalUrlForResource(url,
            _locale);

        if (externalUrl == null) {
          _log.warn("unknown resource: " + url);
          externalUrl = "";
        }

        return "url(\"" + externalUrl + "\")";
      }
    }

    return id;
  }
}