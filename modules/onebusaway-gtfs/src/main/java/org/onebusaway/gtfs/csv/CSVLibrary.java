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
package org.onebusaway.gtfs.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVLibrary {

  private enum EParseState {
    DATA, DATA_IN_QUOTES, END_QUOTE
  };

  public static String escapeValue(String value) {
    if (value.indexOf(',') != -1 || value.indexOf('"') != -1)
      value = "\"" + value.replaceAll("\"", "\"\"") + "\"";
    return value;
  }

  public static String getArrayAsCSV(double[] args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (double v : args) {
      if (seenFirst)
        csv.append(',');
      else
        seenFirst = false;
      csv.append(v);
    }
    return csv.toString();
  }

  public static <T> String getArrayAsCSV(T[] args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (T v : args) {
      if (seenFirst)
        csv.append(',');
      else
        seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public static <T> String getIterableAsCSV(Iterable<T> args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (T v : args) {
      if (seenFirst)
        csv.append(',');
      else
        seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public static String getAsCSV(Object... args) {
    StringBuilder csv = new StringBuilder();
    boolean seenFirst = false;
    for (Object v : args) {
      if (seenFirst)
        csv.append(',');
      else
        seenFirst = true;
      csv.append(escapeValue(v.toString()));
    }
    return csv.toString();
  }

  public static final void parse(InputStream is, CSVListener handler)
      throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    parse(reader, handler);
  }

  public static final void parse(File input, CSVListener handler)
      throws Exception {
    BufferedReader r = new BufferedReader(new FileReader(input));
    parse(r, handler);
  }

  public static void parse(BufferedReader r, CSVListener handler)
      throws IOException, Exception {
    String line = null;

    while ((line = r.readLine()) != null) {
      List<String> values = parse(line);
      handler.handleLine(values);
    }

    r.close();
  }

  public static final List<String> parse(String line) {

    StringBuilder token = new StringBuilder();
    List<StringBuilder> tokens = new ArrayList<StringBuilder>();
    if (line.length() > 0)
      tokens.add(token);

    EParseState state = EParseState.DATA;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      switch (state) {
        case DATA:
          switch (c) {
            case '"':
              state = EParseState.DATA_IN_QUOTES;
              break;
            case ',':
              token = new StringBuilder();
              tokens.add(token);
              break;
            default:
              token.append(c);
              break;
          }
          break;
        case DATA_IN_QUOTES:
          switch (c) {
            case '"':
              state = EParseState.END_QUOTE;
              break;
            default:
              token.append(c);
              break;
          }
          break;
        case END_QUOTE:
          switch (c) {
            case '"':
              token.append('"');
              state = EParseState.DATA_IN_QUOTES;
              break;
            case ',':
              token = new StringBuilder();
              tokens.add(token);
              state = EParseState.DATA;
              break;
            default:
              token.append(c);
              state = EParseState.DATA;
              break;
          }
          break;
      }
    }
    List<String> retro = new ArrayList<String>(tokens.size());
    for (StringBuilder b : tokens)
      retro.add(b.toString());
    return retro;
  }
}
