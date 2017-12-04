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
package org.onebusaway.transit_data_federation_webapp.controllers;

import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BundleManagementInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (isEnabled())
            return true;
        response.sendError(403);
        return false;
    }

    private boolean isEnabled() {
        // if you are in playback mode, you may need to switch bundles
        String enabledStr = System.getProperty(SystemTime.TIME_ADJUSTMENT_KEY);
        if (enabledStr == null) return false;
        if ( "true".equalsIgnoreCase(enabledStr)) {
            return true;
        }

        // otherwise check environment and allow if dev
        String devStr = System.getProperty("env");
        if (devStr == null) return false;
        return "true".equalsIgnoreCase(devStr);
    }
}
