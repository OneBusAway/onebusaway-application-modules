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
package edu.washington.cs.rse.transit.web.oba.common.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.History;

public class TargetSupport {

    protected void newTarget(String target, Object... params) {
        History.newItem(getTarget(target, params));
    }

    protected void newTargetWithMap(String target, Map<String, String> params) {
        History.newItem(getTargetWithMap(target, params));
    }

    protected String getTarget(String target, Object... params) {
        Map<String, String> p = getParamsAsMap(params);
        return getTargetWithMap(target, p);
    }

    protected String getTargetWithMap(String target, Map<String, String> params) {
        return AbstractApplication.getApp().getTargetWithMap(target, params);
    }

    protected void handleException(Throwable ex) {
        AbstractApplication.getApp().handleException(ex);
    }

    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private Map<String, String> getParamsAsMap(Object... params) {
        if (params.length % 2 != 0)
            throw new IllegalArgumentException("Number of params must be even (key-value pairs)");
        Map<String, String> p = new LinkedHashMap<String, String>();
        for (int i = 0; i < params.length; i += 2)
            p.put(params[i].toString(), params[i + 1].toString());
        return p;
    }
}
