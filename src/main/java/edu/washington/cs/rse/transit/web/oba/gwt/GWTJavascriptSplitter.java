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
package edu.washington.cs.rse.transit.web.oba.gwt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GWTJavascriptSplitter {

    private static Pattern FILE_PATTERN = Pattern.compile("^([0-9a-fA-F]+)\\.cache\\.html$");

    private static String LAST_FUNCTION = "^function (\\w+)\\(\\)\\{\\}$";

    public static void main(String[] args) throws Exception {
        File dir = new File(
                "/Users/bdferris/l10n/edu.washington.cs.rse.transit/target/gwt/edu.washington.cs.rse.transit.web.oba.iphone.OneBusAwayIPhoneApplication");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return FILE_PATTERN.matcher(name).matches();
            }
        });

        if (files == null)
            return;

        for (File file : files) {
            GWTJavascriptSplitter splitter = new GWTJavascriptSplitter(file);
            splitter.run();
        }
    }

    private File _targetFile;

    private int _maxLength = 20000;

    public GWTJavascriptSplitter(File file) {
        _targetFile = file;
    }

    public void run() throws Exception {
        String name = _targetFile.getName();
        Matcher m = FILE_PATTERN.matcher(name);

        if (!m.matches()) {
            System.err.println("inappropriate input file: " + _targetFile.getAbsolutePath());
            return;
        }

        String key = m.group(1);
        File parent = _targetFile.getParentFile();

        System.out.println(_targetFile);
        System.out.println("  key=" + key);
        File tmp = new File(_targetFile.getAbsolutePath() + ".saved");
        _targetFile.renameTo(tmp);

        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(_targetFile)));

        BufferedReader reader = new BufferedReader(new FileReader(tmp));

        String lastFunctionLine = getLastFunction(reader);
        reader.close();
        reader = new BufferedReader(new FileReader(tmp));

        String line = null;
        boolean inScript = false;
        boolean writtenLastFunction = false;

        int scriptIndex = 0;
        int lengthWritten = 0;
        PrintWriter scriptWriter = null;

        while ((line = reader.readLine()) != null) {
            if (line.endsWith("<body><script><!--")) {
                inScript = true;
                int index = line.indexOf("<script><!--");
                writer.println(line.substring(0, index));

                if (!writtenLastFunction) {
                    if (scriptWriter == null) {
                        String path = key + "-" + (scriptIndex++) + ".cache.js";
                        writer.println("<script type=\"text/javascript\" src=\"" + path + "\"></script>");
                        scriptWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(parent, path))));
                        lengthWritten = 0;
                    }
                    scriptWriter.println(lastFunctionLine);
                    lengthWritten += lastFunctionLine.length();
                    writtenLastFunction = true;
                }
            } else if (line.startsWith("--></script></body></html>")) {
                inScript = false;
                int index = line.indexOf("</body>");
                writer.println(line.substring(index));
            } else if (inScript) {

                if (line.equals(lastFunctionLine))
                    continue;

                if (lengthWritten + line.length() > _maxLength) {

                    // Close a previously opened file
                    if (scriptWriter != null) {
                        scriptWriter.close();
                        scriptWriter = null;
                    }

                    String path = key + "-" + (scriptIndex++) + ".cache.js";
                    writer.println("<script type=\"text/javascript\" src=\"" + path + "\"></script>");
                    scriptWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(parent, path))));
                    lengthWritten = 0;

                }

                scriptWriter.println(line);
                lengthWritten += line.length();
            } else {
                writer.println(line);
            }

        }

        reader.close();
        writer.close();

        if (scriptWriter != null)
            scriptWriter.close();
    }

    private String getLastFunction(BufferedReader reader) throws IOException {

        String line = null;
        boolean inScript = false;

        String lastFunction = null;

        while ((line = reader.readLine()) != null) {
            if (line.endsWith("<body><script><!--")) {
                inScript = true;
            } else if (line.startsWith("--></script></body></html>")) {
                inScript = false;
            } else if (inScript) {
                if (line.matches(LAST_FUNCTION))
                    lastFunction = line;
            }
        }

        return lastFunction;
    }
}
