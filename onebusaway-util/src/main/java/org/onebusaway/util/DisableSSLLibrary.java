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
package org.onebusaway.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DisableSSLLibrary {
  public static void disableSSL(HttpsURLConnection conn) throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sc = SSLContext.getInstance("TLS");
    
    TrustManager tm = new X509TrustManager() {

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
    
    sc.init(null, new TrustManager[] { tm }, new java.security.SecureRandom());
    conn.setSSLSocketFactory(sc.getSocketFactory());
    conn.setHostnameVerifier(
        new HostnameVerifier(){
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        }
    );
  }
}
