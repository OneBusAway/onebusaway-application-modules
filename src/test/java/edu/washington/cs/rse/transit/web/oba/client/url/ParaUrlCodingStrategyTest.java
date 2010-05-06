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
package edu.washington.cs.rse.transit.web.oba.client.url;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.washington.cs.rse.transit.web.oba.common.client.url.ParaUrlCodingStrategy;

import junit.framework.TestCase;

public class ParaUrlCodingStrategyTest extends TestCase {

    private ParaUrlCodingStrategy _strategy = new ParaUrlCodingStrategy();

    public void testEncode01() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("a", "b");
        m.put("1", "2");
        String v = _strategy.getParamMapAsString(m);
        assertEquals("a(b)1(2)", v);

        Map<String, String> m2 = _strategy.getParamStringAsMap(v);
        assertEquals(m, m2);
    }

    public void testEncode02() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("a", "b");
        m.put("d", "one two");
        String v = _strategy.getParamMapAsString(m);
        assertEquals("a(b)d(one two)", v);

        Map<String, String> m2 = _strategy.getParamStringAsMap(v);
        assertEquals(m, m2);
    }

    public void testEncode03() {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("a", "b");
        m.put("(d)", "e(f)g(h)i");
        String v = _strategy.getParamMapAsString(m);
        assertEquals("a(b)%28d%29(e%28f%29g%28h%29i)", v);

        Map<String, String> m2 = _strategy.getParamStringAsMap(v);
        assertEquals(m, m2);
    }

}
