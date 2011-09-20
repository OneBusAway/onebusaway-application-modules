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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.steadystate.css.parser.SACParserCSS2;
import com.steadystate.css.sac.DocumentHandlerExt;

/*****************************************************************************
 * Internal Classes
 ****************************************************************************/

class CssDocumentHandlerImpl implements DocumentHandlerExt {

  private static Logger _log = LoggerFactory.getLogger(CssDocumentHandlerImpl.class);

  private static final String AT_RULE_URL = "@url";

  private static final String AT_RULE_SPRITE = "@sprite";

  private Map<String, String> _substitutions = new HashMap<String, String>();

  private StringBuilder _buffer = new StringBuilder();

  private ClientBundleContext _context;

  private ClientBundleImpl _parentBundle;

  public CssDocumentHandlerImpl(ClientBundleContext context,
      ClientBundleImpl parentBundle) {
    _context = context;
    _parentBundle = parentBundle;
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

    if (atRule.startsWith(AT_RULE_SPRITE)) {

      String snippet = atRule.substring(AT_RULE_SPRITE.length());
      snippet = snippet.trim();
      parse(snippet);

    } else if (atRule.startsWith(AT_RULE_URL)) {
      String snippet = atRule.substring(AT_RULE_URL.length());
      snippet = snippet.trim();
      Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+);$");
      Matcher matcher = pattern.matcher(snippet);
      if (matcher.matches()) {
        String name = matcher.group(1);
        String resource = matcher.group(2);
        handleUrl(name, resource);
      }
    }
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

    if (name.equals("gwt-image")) {
      String v = unit.toString();
      ResourcePrototype resource = _parentBundle.getResource(v);

      if (resource == null) {
        _log.warn("unknown resource: " + v);
        return;
      }

      if (!(resource instanceof ImageResource)) {
        _log.warn("expected ImageResource");
        return;
      }

      ImageResource img = (ImageResource) resource;
      _buffer.append("background-image:url(" + img.getURL() + ");");

    } else {
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
  }

  public void endSelector(SelectorList selectors) throws CSSException {
    _buffer.append('}').append('\n');
  }

  public void charset(String characterEncoding) throws CSSException {

  }

  /***************************************************************************
   * 
   **************************************************************************/

  private void handleUrl(String name, String value) {
    ResourcePrototype resource = _parentBundle.getResource(value);

    if (resource == null)
      throw new IllegalStateException("unknown resource name=" + name
          + " value: " + value);

    if (!(resource instanceof ResourceWithUrl))
      throw new IllegalStateException(
          "resource is not instance of ResourceWithUrl: name=" + value
              + " value=" + resource);

    ResourceWithUrl data = (ResourceWithUrl) resource;
    String url = data.getUrl();
    url = _context.addContext(url);
    _substitutions.put(name, "url(\"" + url + "\")");
  }

  private String substitute(String id) {
    if (_substitutions.containsKey(id))
      id = _substitutions.get(id);
    return id;
  }

  private void parse(String snippet) throws CSSException {

    try {
      BufferedReader reader = new BufferedReader(new StringReader(snippet));
      InputSource source = new InputSource(reader);
      Parser p = new SACParserCSS2();
      p.setDocumentHandler(this);
      p.parseStyleSheet(source);
      reader.close();
    } catch (IOException ex) {
      throw new CSSException(ex);
    }
  }
}