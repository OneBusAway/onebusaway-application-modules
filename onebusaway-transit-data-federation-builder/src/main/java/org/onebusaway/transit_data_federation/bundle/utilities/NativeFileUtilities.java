/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Another collection of file utilities.  These are implemented via native
 * O/S commands and should be converted to pure java at some point.
 */
public class NativeFileUtilities {

    private static Logger _log = LoggerFactory.getLogger(NativeFileUtilities.class);

    public int tarcvf(String baseDir, String[] paths, String filename) {
        Process process = null;
        try {
            StringBuffer cmd = new StringBuffer();
            cmd.append("tar -c -f " + filename + " -z -C " + baseDir + "  ");
            for (String path : paths) {
                cmd.append(path + " ");
            }
            _log.info("exec:" + cmd.toString());
            process = Runtime.getRuntime().exec(cmd.toString());
            return process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFiles(File from, File to) {
        _log.debug("copying " + from + " to " + to);
        try {
            if (!from.exists())
                return;
            if (from.equals(to) || to.getParent().equals(from))
                return;
            if (to.exists() && to.isDirectory()) {
                if (from.exists() && from.isDirectory()) {
                    org.apache.commons.io.FileUtils.copyDirectory(from, to, true);
                    return;  // Added to prevent dupe dir.  JP 10/14/15
                }
                String file = this.parseFileName(from.toString());
                to = new File(to.toString() + File.separator + file);
                _log.debug("constructed new destination=" + to.toString());
            }

            if (from.isDirectory()) {
                to.mkdirs();
                File[] files = from.listFiles();
                if (files == null)
                    return;
                for (File fromChild : files) {
                    File toChild = new File(to, fromChild.getName());
                    copyFiles(fromChild, toChild);
                }
            } else {
                org.apache.commons.io.FileUtils.copyFile(from, to, true/*preserve date*/);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    public String parseFileName(String urlString) {
        return parseFileName(urlString, "/");
    }

    public String parseFileName(String urlString, String seperator) {
        if (urlString == null) return null;
        int i = urlString.lastIndexOf(seperator);
        // check to see we don't have a trailing slash
        if (i > 0 && i+1 < urlString.length()) {

            return urlString.substring(i+1, urlString.length());
        }
        // we have a trailing slash, recurse removing trailing slash
        if (i >= 0) {
            return parseFileName(urlString.substring(0, urlString.length()-1));
        }
        // did not find a slash, return filename as is
        return urlString;
    }

}
