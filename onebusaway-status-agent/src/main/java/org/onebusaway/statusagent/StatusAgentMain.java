/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.statusagent;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.onebusaway.statusagent.model.IcingaResponse;
import org.onebusaway.statusagent.model.IcingaItem;

public class StatusAgentMain {

    public static void main(String[] args) throws IOException {

        //TODO: get these from configuration somehow
//        String baseUrl = _config.getConfigurationValueAsString("icinga.baseUrl", null);
//        String command = _config.getConfigurationValueAsString("icinga.command", null);

        String baseUrl = "http://localhost:9191/icinga-web/web/api/service/";
        String command = "filter[AND(SERVICEGROUP_NAME|=|devportal;)]/columns[SERVICE_NAME|SERVICE_DISPLAY_NAME|SERVICE_CURRENT_STATE|SERVICE_OUTPUT|SERVICE_PERFDATA]/order(SERVICE_NAME;ASC)/countColumn=SERVICE_ID/authkey=wDQAACOdJrNEtOSpHSx8edb0z/json";

        // be careful -- by default we aren't configured to do anything!
        if (baseUrl == null || command == null) {
            System.out.println("missing required configuration for status group: baseUrl="
                    + baseUrl + ", command=" + command);
            return;//can't go any farther
        }

        try {
            HttpClient client = new HttpClient();
            String url = baseUrl + encode(command);
            HttpMethod method = new GetMethod(url);

            client.executeMethod(method);
            InputStream result = method.getResponseBodyAsStream();

            ObjectMapper mapper = new ObjectMapper();
            IcingaResponse response = mapper.readValue(result, IcingaResponse.class);

            if (response.getResult().length > 0) {

                Reader reader = Resources.getResourceAsReader("mybatis_config.xml");
                SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
                SqlSession session = sqlSessionFactory.openSession();

                //remove all records in table
                int count = session.delete("IcingaItem.delete");
                System.out.println("num records deleted = " + count);

                //insert records just retrieved
                for (IcingaItem i : response.getResult()) {
                    session.insert("IcingaItem.insert", i);
                    System.out.println("record inserted successfully: " + i.getServiceName());
                }

                session.commit();
                session.close();
            }
            else System.out.println("No Icinga items in response");
        } catch (IOException e) {
            System.out.println("Exception getting Icinga data " + e);
        }
    }

    private static String encode(String command) throws UnsupportedEncodingException {
        StringBuffer encoded = new StringBuffer();
        if (command == null) return encoded.toString();
        String[] tokens = command.split("/");
        for (String token : tokens) {
            String enc = URLEncoder.encode(token, "utf-8");
            encoded.append("/").append(enc);
        }

        return encoded.toString();
    }
}
