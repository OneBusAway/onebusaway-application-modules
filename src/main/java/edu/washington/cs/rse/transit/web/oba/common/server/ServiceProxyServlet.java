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
package edu.washington.cs.rse.transit.web.oba.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServiceProxyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ServiceProxyServlet() {

    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        URL url = new URL("http://localhost:8080/edu.washington.cs.rse.transit/onebusaway");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        for (Enumeration<?> en = req.getHeaderNames(); en.hasMoreElements();) {
            String key = en.nextElement().toString();
            String value = req.getHeader(key);
            connection.setRequestProperty(key, value);
        }

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);

        InputStream in = req.getInputStream();
        OutputStream out = connection.getOutputStream();

        transferStreams(in, out);

        out.flush();
        out.close();

        in.close();

        in = connection.getInputStream();
        out = resp.getOutputStream();

        resp.setStatus(connection.getResponseCode());

        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                for (String value : entry.getValue())
                    resp.addHeader(key, value);
            }
        }
        
        transferStreams(in, out);

        in.close();
        connection.disconnect();

        out.flush();
        out.close();

        resp.flushBuffer();
    }

    private void transferStreams(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];

        while (true) {
            int rc = in.read(buffer);
            if (rc == -1)
                break;
            out.write(buffer, 0, rc);
        }
    }
}
