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
package edu.washington.cs.rse.transit.web.oba.gwt.resources;

import com.google.gwt.libideas.resources.client.ResourcePrototype;
import com.steadystate.css.parser.CSSOMParser;

import junit.framework.TestCase;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

public class ImmutableResourceBundleFactoryTest extends TestCase {

  public void test01() throws IOException {

    ImmutableResourceBundleFactory factory = new ImmutableResourceBundleFactory(
        new DefaultURLStrategy());
    factory.addResource("test", Test01Resources.class);

    Map<String, ImmutableResourceBundle> bundles = factory.getBundles();

    assertEquals(1, bundles.size());
    assertTrue(bundles.containsKey("test"));

    ImmutableResourceBundle bundle = bundles.get("test");

    assertTrue(bundle instanceof Test01Resources);

    Test01Resources r = (Test01Resources) bundle;

    ResourcePrototype[] rs = r.getResources();
    assertTrue(rs.length == 2);

    ResourcePrototype r1 = r.getResource("getImage");
    assertEquals("getImage", r1.getName());
    assertTrue(r1 instanceof DataResource);
    DataResource imgResource = (DataResource) r1;
    assertEquals("/test/getImage-eeb24432c1fb06cecf02c1f0842249d3.cache.png",
        imgResource.getUrl());

    ResourcePrototype r2 = r.getResource("getStylesheet");
    assertEquals("getStylesheet", r2.getName());
    CssResource cssResource = (CssResource) r2;

    String css = cssResource.getText();

    System.out.println(css);

    StringReader reader = new StringReader(css);
    InputSource source = new InputSource(reader);
    CSSOMParser parser = new CSSOMParser();
    CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, "");
    CSSRuleList rules = stylesheet.getCssRules();
    assertEquals(1, rules.getLength());
    CSSRule rule = rules.item(0);
    assertEquals(CSSRule.STYLE_RULE, rule.getType());
    CSSStyleRule styleRule = (CSSStyleRule) rule;
    CSSStyleDeclaration style = styleRule.getStyle();
    String value = style.getPropertyValue("background-image");
    assertEquals(
        "url(/test/getImage-eeb24432c1fb06cecf02c1f0842249d3.cache.png)", value);
  }
}
