/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides logging to multiple CSV files.  First call header("a,list,of,fields"),
 * then log(any,object,for,fields);
 * 
 * TODO: tests, add checking to ensure that headers have same number of fields
 * as entries
 * 
 */
public class MultiCSVLogger {
  private static final String CHANGE_LOG = "change_log.csv";
  private String diff_log_filename = "diff_log.csv";
  private Logger _log = LoggerFactory.getLogger(MultiCSVLogger.class);
  private static DateFormat SIMPLE_DATE = new SimpleDateFormat();
  private ArrayList<MultiCSVLoggerSummarizeListener> listeners = new ArrayList<MultiCSVLoggerSummarizeListener>();

  private HashMap<String, Log> logs;
  
  private File basePath;
  
  public void setBasePath(File path) {
    this.basePath = path;
  }

  public void addListener(MultiCSVLoggerSummarizeListener listener) {
    listeners.add(listener);
  }
  
  class Log {
    int lines;
    PrintStream stream;
    Log(String file) {
      FileOutputStream outputStream;
      try {
        outputStream = new FileOutputStream(new File(basePath, file), true);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
      stream = new PrintStream(outputStream);
    }
  }

  public MultiCSVLogger() {
    logs = new HashMap<String, Log>();
  }
  
  @PostConstruct
  public void postConstruct() {
    // integration tests may not have a path
    if (basePath == null) {
      basePath = new File(System.getProperty("java.io.tmpdir"));
      _log.warn("MultiCSVLogger initialized without path:  using "
          + basePath);
    }
    if (!basePath.exists()) {
      basePath.mkdirs();
    }
  }

  public void logCSV(String file, String csv) {
   Log log = logs.get(file);
   if (log == null) {
     throw new RuntimeException("log called before header for file " + file);
   }
   log.lines += 1;
   log.stream.print(csv);
   log.stream.print("\n");
  }
  
  public void log(String file, Object... args) {
    Log log = logs.get(file);
    if (log == null) {
      _log.warn("log called before header for file " + file);
      header(file, "MISSING");
      log = logs.get(file);
    }
    log.lines += 1;
    for (int i = 0; i < args.length; ++i) {
      Object arg = args[i];
      String argStr = "" + arg; //arg.toString() fails for null, while this works
      if (argStr.contains(",") || argStr.contains("\"")) {
        argStr = "\"" + argStr.replace("\"", "\"\"") + "\"";
      }

      log.stream.print(argStr);
      if (i != args.length - 1)
        log.stream.print(",");
    }
    log.stream.print("\n");
  }

  public void header(String file, String header){
    header(file, header, null);
  }
  
  public void header(String file, String header, String comment) {
    Log log = logs.get(file);
    if (log == null) {
      log = new Log(file);
      logs.put(file, log);
    } else {
      throw new RuntimeException("header called more than once for file " + file);
    }
    log.stream.println(header);
    if (comment!=null && !comment.equals("")){
    	for (String s : comment.split("\n")){
    		log.stream.println(SIMPLE_DATE.format(new Date())+","+s);
    	}
    }
  }

  public boolean hasHeader(String file) {
    return logs.get(file) != null;
  }
  
  public void changelogHeader() {
    changelogHeader(null);
  }
  
  public void changelogHeader(String comment) {
    String file = CHANGE_LOG;
    Log log = logs.get(file);
    if (log == null) {
      header(file, "date,message", comment);  
    } else {
      if (comment != null) {
        changelog(comment);
      }
    }
  }
  
  public void changelog(String s) {
    log(CHANGE_LOG, SIMPLE_DATE.format(new Date()), s);
  }
  
  public void difflogHeader(String filename) {
	    diff_log_filename = filename;
	    Log log = logs.get(diff_log_filename);
	    if (log == null) {
	      header(diff_log_filename, "line,diff");  
	    }
	  }
		  
  public void difflog(int linenum, String content) {
    log(diff_log_filename, linenum, content);
  }
	  
  public void summarize() {
    // tell our listeners we are about to summarize our results
    for (MultiCSVLoggerSummarizeListener l : listeners) {
      l.summarize();
    }
    
    FileOutputStream outputStream;
    try {
      outputStream = new FileOutputStream(new File(basePath, "summary.csv"), true);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    PrintStream stream = new PrintStream(outputStream);
    stream.println("filename,description,lines");
    for (Map.Entry<String, Log> entry : logs.entrySet()) {
      Log log = entry.getValue();
      String name = entry.getKey().replace("_", " ").replace(".csv", "");
      String filename = entry.getKey().replace(",", "_");
      stream.println(filename + "," + name + "," + log.lines);
    }
  }
  
  public void clear() {
    for (File file : basePath.listFiles()) {
      if (file.getName().endsWith(".csv") && !CHANGE_LOG.equals(file.getName())) {
        file.delete();
      }
    }
    
  }
}
