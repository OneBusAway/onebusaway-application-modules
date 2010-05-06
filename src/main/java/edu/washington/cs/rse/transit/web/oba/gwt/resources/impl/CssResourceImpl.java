/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.gwt.resources.impl;

import com.steadystate.css.parser.SACParserCSS2;
import com.steadystate.css.sac.DocumentHandlerExt;

import edu.washington.cs.rse.transit.web.oba.gwt.resources.CssDataResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.CssResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.DataResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.LocalResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.ResourcePrototype;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssResourceImpl extends ResourcePrototypeImpl implements CssDataResource,
    LocalResource {

  private static Pattern URL_PATTERN = Pattern.compile("^@url (\\w+) (\\w+);$");

  private URL _localURL;

  private File _tempDir;

  private boolean _initialized = false;

  private String _outputValue;

  private File _outputFile;

  private URL _outputUrl;

  public CssResourceImpl(ImmutableResourceBundleImpl resource, String name,
      URL localURL, File tempDir) {
    super(resource, name);
    _localURL = localURL;
    _tempDir = tempDir;
  }

  /*****************************************************************************
   * {@link CssResource} Interface
   ****************************************************************************/

  public String getText() {
    initialize();
    return _outputValue;
  }

  public String getUrl() {
    String text = getText();
    String hash = ResourceSupport.getHash(text);
    return constructURL(hash, "css");
  }

  /*****************************************************************************
   * {@link LocalResource} Interface
   ****************************************************************************/

  public URL getLocalUrl() {
    initialize();
    return _outputUrl;
  }

  public String getRemoteUrl() {
    return getUrl();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private synchronized void initialize() {

    if (_initialized)
      return;

    _initialized = true;

    try {
      _outputValue = parse();
      String key = ResourceSupport.getHash(_outputValue);
      String url = constructURL(key, "css");

      _outputFile = new File(_tempDir, url);
      _outputUrl = _outputFile.toURL();

      File parent = _outputFile.getParentFile();
      if (parent != null && !parent.exists())
        parent.mkdirs();

      BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFile));
      writer.write(_outputValue);
      writer.close();

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String parse() throws IOException {
    InputStreamReader reader2 = new InputStreamReader(_localURL.openStream());
    InputSource source2 = new InputSource(reader2);
    Parser p = new SACParserCSS2();
    DocumentHandlerImpl handler = new DocumentHandlerImpl();
    p.setDocumentHandler(handler);
    p.parseStyleSheet(source2);
    reader2.close();
    return handler.getResults();
  }

  private class DocumentHandlerImpl implements DocumentHandlerExt {

    private Map<String, String> _substitutions = new HashMap<String, String>();

    private StringBuilder _buffer = new StringBuilder();

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
      System.out.println("start font face");
    }

    public void endFontFace() throws CSSException {
      System.out.println("end font face");
    }

    public void importStyle(String uri, SACMediaList media,
        String defaultNamespaceUri) throws CSSException {

    }

    public void ignorableAtRule(String atRule) throws CSSException {
      Matcher m = URL_PATTERN.matcher(atRule);
      if (m.matches()) {
        String name = m.group(1);
        String value = m.group(2);
        handleUrl(name, value);
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

    private void handleUrl(String name, String value) {
      ResourcePrototype resource = _resource.getResource(value);

      if (resource == null)
        throw new IllegalStateException("unknown resource method: " + value);

      if (!(resource instanceof DataResource))
        throw new IllegalStateException(
            "resource is not instance of DataResource: name=" + value
                + " value=" + resource);

      DataResource data = (DataResource) resource;
      String url = data.getUrl();
      url = addContext(url);
      _substitutions.put(name, "url(\"" + url + "\")");
    }

    private String substitute(String id) {
      if (_substitutions.containsKey(id))
        id = _substitutions.get(id);
      return id;
    }

  }
}
