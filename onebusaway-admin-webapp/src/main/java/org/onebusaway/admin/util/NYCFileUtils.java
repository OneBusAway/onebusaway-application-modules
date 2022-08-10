/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.onebusaway.transit_data_federation.bundle.utilities.NativeFileUtilities;
import org.onebusaway.util.FileUtility;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of file handling utilities making life easier when working with
 * Java but specific to the admin module needs.  More Generic functions exist
 * in org.onebusaway.nyc.util.impl.FileUtility.
 * 
 */
public class NYCFileUtils {
  private static Logger _log = LoggerFactory.getLogger(NYCFileUtils.class);

  private String _workingDirectory = null;
  // more generic file handling functions belong in nyc.util
  private FileUtility _fileUtil = new FileUtility();
  // native utilities that should be converted to pure java and migrated to FileUtility
  private NativeFileUtilities _nativeFileUtil = new NativeFileUtilities();

  public NYCFileUtils() {
    _workingDirectory = System.getProperty("java.io.tmpdir");
  }

  public NYCFileUtils(String workingDirectory) {
    _workingDirectory = workingDirectory;
  }

  /**
   * Retrieve a file at the remote URL. The file will be named the last portion
   * of the URL, following the conventions of the UNIX tool wget.
   */
  public void wget(String urlString) {
    URL url;
    InputStream is = null;
    BufferedInputStream bis = null;
    FileOutputStream fos = null;
    String fileName = parseFileName(urlString);
    _log.info("downloading " + urlString + " to fileName " + _workingDirectory
        + File.separatorChar + fileName);
    try {
      url = new URL(urlString);
      is = url.openStream();
      bis = new BufferedInputStream(is);
      fos = new FileOutputStream(_workingDirectory
          + File.separatorChar + fileName);
      IOUtils.copy(bis, fos);
    } catch (Exception any) {
      throw new RuntimeException(any);
    } finally {
      if (bis != null)
        try {
          bis.close();
        } catch (Exception e1) {
        }
      if (fos != null)
        try {
          fos.close();
        } catch (Exception e2) {
        }
    }
  }

  /**
   * Copy the input stream to the given destinationFileName (which includes path
   * and filename).
   */
  public void copy(InputStream source, String destinationFileName) {
    _fileUtil.copy(source, destinationFileName);
  }
  
  public String parseFileName(String urlString, String seperator) {
    return _nativeFileUtil.parseFileName(urlString, seperator);
  }
  
  public String parseFileName(String urlString) {
    return _nativeFileUtil.parseFileName(urlString);
  }

  public String parseDirectory(String urlString) {
    if (urlString == null) return null;
    int i = urlString.lastIndexOf("/");
    if (i+1 < urlString.length()) {
      return urlString.substring(0, i);
    }
    return urlString;
  }
  
  public String parseFileNameMinusExtension(String urlString) {
    if (urlString == null) return null;
    int i = urlString.lastIndexOf("/");
    if (i > 0 && i+1 < urlString.length()) {
      urlString = urlString.substring(i+1, urlString.length());
    }    
    i = urlString.lastIndexOf(".");
    if (i > 0) {
      urlString = urlString.substring(0, i);
    }
    return urlString;
  }

  public String parseExtension(String urlString) {
    int i = urlString.lastIndexOf(".");
    if (i < urlString.length()) {
      return urlString.substring(0, i);
    }
    return urlString;
  }

  public String parseBucket(String s3path) {
    if (s3path.indexOf("s3://") == -1) {
      throw new RuntimeException(
          "Invalid s3path, missing protocol s3://; path=" + s3path);
    }
    int start = s3path.indexOf("/", 5);
    int end = s3path.indexOf("/", start + 1);
    return s3path.substring(start, end);
  }

  public String parseKey(String s3path) {
    if (s3path.indexOf("s3://") == -1) {
      throw new RuntimeException(
          "Invalid s3path, missing protocol s3://; path=" + s3path);
    }
    int bucketStart = s3path.indexOf("/", 5);
    int start = s3path.indexOf("/", bucketStart + 1);
    return s3path.substring(start, s3path.length());
  }
  public String parseAgency(String path) {
    if (!path.contains("_")) return null;
    int start = path.lastIndexOf("/");
    if (start == -1) { 
      start = 0;
    } else {
      start = start + 1; // advance past "/"
    }
    int stop = path.indexOf("_", start);
    return path.substring(start, stop);
  }

  /**
   * untar and uncompress a tar file (.tar.gz)
   */
  public int tarzxf(String tarFile) {
    Process process = null;
    try {
      String[] cmds = {
          "tar",
          "zxC",
          _workingDirectory,
          "-f",
          tarFile
      };
      debugCmds(cmds);
      process = Runtime.getRuntime().exec(cmds);
      return process.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * set UNIX permissions. The permissions string is passed through as is so it
   * can be of the octal format 777 or o+x format.
   */
  public int chmod(String permissions, String destinationFileName) {
    Process process = null;
    try {
      String[] cmds = {
          "chmod",
          permissions,
          destinationFileName
      };
      debugCmds(cmds);
      process = Runtime.getRuntime().exec(cmds);
      return process.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void copyFiles(File from, File to) {
    _nativeFileUtil.copyFiles(from, to);
  }

  public String createTmpDirectory() {
    String tmpDir = System.getProperty("java.io.tmpdir") + File.separator
        + "tmp" + SystemTime.currentTimeMillis();
    boolean created = new File(tmpDir).mkdir();
    // if directory already exists, try again
    if (!created) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        return null;
      }
     _log.error("issue creating tmpDir=" + tmpDir);
      return createTmpDirectory();
    }
    return tmpDir;
  }

  /**
   * unix style unzip.
   */
  public int unzip(String zipFileName, String outputDirectory) {
    Process process = null;
    try {
      String[] cmds = {
          "unzip",
          "-o",
          zipFileName,
          "-d",
          outputDirectory
      };
      process = Runtime.getRuntime().exec(cmds);
      StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
      StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");
      errorGobbler.start();
      outputGobbler.start();
      return process.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Write file contents to the given fileName.
   * @param fileName
   * @param contents
   */
  public void createFile(String fileName, StringBuffer contents) {
    try {
      File file = new File(fileName);
      FileWriter fw = new FileWriter(file);
      fw.append(contents);
      fw.close();
    } catch (IOException ioe) {
      _log.error(ioe.toString(), ioe);
      throw new RuntimeException(ioe);
    }
  }

  public void moveFile(String srcFileName, String destFileName) throws Exception {
    _fileUtil.moveFile(srcFileName, destFileName);
  }

  public int tarcvf(String baseDir, String[] paths, String filename) {
    return _nativeFileUtil.tarcvf(baseDir, paths, filename);
  }

  public static void copyFile(File srcFile, File destFile) {
    _log.debug("copyFile(" + srcFile + "=>" + destFile);
		try {
			org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
		} catch (IOException e) {
			_log.error("copyFile failed:", e);
			throw new RuntimeException(e);
		}
  }
	  

  public InputStream read(String filename) {
    File file = new File(filename);
    if (file.exists()) {
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        _log.error(e.toString(), e);
        throw new RuntimeException(e);
      }
    } else {
      _log.info("file not found for read(" + filename + ")");
    }
    return null;
  }

  public static String escapeFilename(String s) {
    return s.replace(" ", "\\ ");
  }
  
  public static void debugCmds(String[] array) {
    StringBuffer sb = new StringBuffer();
    sb.append("exec:");
    for (String s :array){
      sb.append(s + " ");
    }
    _log.info(sb.toString());
  }


  /**
   * debug sub shells, from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.htm
   *
   */
  class StreamGobbler extends Thread
  {
      InputStream is;
      String type;
      
      StreamGobbler(InputStream is, String type)
      {
          this.is = is;
          this.type = type;
      }
      
      public void run()
      {
          try
          {
              InputStreamReader isr = new InputStreamReader(is);
              BufferedReader br = new BufferedReader(isr);
              String line=null;
              while ( (line = br.readLine()) != null)
                _log.info(type + ">" + line);
              } catch (IOException ioe)
                {
                  ioe.printStackTrace();  
                }
      }
  }



}
