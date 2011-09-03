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
package org.onebusaway.webapp.gwt.common.url;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParaUrlCodingStrategy implements UrlCodingStrategy {

    public Map<String, String> getParamStringAsMap(String params) {

        Map<String, String> m = new LinkedHashMap<String, String>();

        String key = "";
        String value = "";
        boolean inKey = true;

        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (inKey) {
                if (c == '(')
                    inKey = false;
                else
                    key += c;
            } else {
                if (c == ')') {
                    m.put(unescapeString(key), unescapeString(value));
                    key = "";
                    value = "";
                    inKey = true;
                } else {
                    value += c;
                }
            }
        }
        return m;
    }

    public String getParamMapAsString(Map<String, String> params) {

        StringBuilder b = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = escapeString(entry.getKey());
            String value = escapeString(entry.getValue());
            b.append(key).append('(').append(value).append(')');
        }

        return b.toString();
    }

    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private String escapeString(String value) {
        value = value.replaceAll("\\(", "%28");
        value = value.replaceAll("\\)", "%29");
        return value;
    }

    private static String unescapeString(String value) {
        value = value.replaceAll("%29", ")");
        value = value.replaceAll("%28", "(");
        return value;
    }
}
