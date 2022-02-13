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
package org.onebusaway.admin.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.util.NYCFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskFileServiceImpl implements FileService {

	private static Logger _log = LoggerFactory.getLogger(DiskFileServiceImpl.class);
	private String _basePath;
	private String _gtfsPath;
	private String _stifPath;
	private String _auxPath;
	private String _buildPath;
	private String _configPath;
	@Override
	public void setup() {
		_log.info("DiskFileServiceImpl setup");
	}

	@Override
	public void setUser(String user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBucketName(String bucketName) {
		_basePath = bucketName;
	}

	@Override
	public void setGtfsPath(String gtfsPath) {
		_gtfsPath = gtfsPath;
	}

	@Override
	public String getGtfsPath() {
		return _gtfsPath;
	}

  @Override
  public void setAuxPath(String auxPath) {
    _auxPath = auxPath;
  }

  @Override
  public String getAuxPath() {
    return _auxPath;
  }

	@Override
	public void setBuildPath(String buildPath) {
		_buildPath = buildPath;
	}

	@Override
	public String getConfigPath() {
		return _configPath;
	}

	@Override
	public void setConfigPath(String configPath) {
		_configPath = configPath;
	}

	@Override
	public String getBuildPath() {
		return _buildPath;
	}

	@Override
	public String getBucketName() {
		return _basePath;
	}

	@Override
	public boolean bundleDirectoryExists(String filename) {
		File f = new File(_basePath, filename);
		return f.exists();
	}

	@Override
	public boolean createBundleDirectory(String filename) {
		File f = new File(_basePath, filename);
		return f.mkdirs();
	}

  @Override
  public boolean deleteBundleDirectory(String filename) {
    File dir = new File(_basePath, filename);
    return deleteFile(dir);
  }

	@Override
	public List<String[]> listBundleDirectories(int maxResults) {
		ArrayList<String[]> bundleDirs = new ArrayList<String[]>();
		_log.info("listBundleDirectories(" + maxResults + ")");
		try {
			File baseDir = new File(_basePath);
			String[] list = baseDir.list();
			if (list == null) {
				_log.info("empty list for bundleDirectories at basepath=" + _basePath);
				return bundleDirs;
			}
			// need filename/flag/modified date
			for (String dir : list) {
				_log.info("bundle dir: |" + dir + "|");
				File fDir = new File(baseDir, dir);
				String lastModified = new Date(fDir.lastModified()).toString();
				// Since the bundle directory date does not get updated when a build
				// is done, get the date on the builds sub-directory.
				File buildDir = new File(fDir, _buildPath);
				if (buildDir.exists()) {
					lastModified = new Date(buildDir.lastModified()).toString();
				}
				String[] a = {dir, " ", lastModified};
				bundleDirs.add(a);
			}
		} catch (Throwable t) {
			_log.error("Exception retrieving listBundleDiretories=", t, t);
		} finally {
			_log.info("exiting listBundleDirectories");
		}
		return bundleDirs;
	}

	@Override
	public String getBundleDirTimestamp(String dir) {
	  File baseDir = new File(_basePath);
	  File fDir = new File(baseDir, dir);
	  String timestamp =  new Date(fDir.lastModified()).toString();
	  return timestamp;
	}

	@Override
	public String get(String s3path, String tmpDir) {
		NYCFileUtils fs = new NYCFileUtils();
		File srcFile = new File(_basePath, s3path);
		File destFile = new File(tmpDir);
		fs.copyFiles(srcFile, destFile);
		return tmpDir + File.separator + fs.parseFileName(s3path);
	}

	@Override
	public InputStream get(String s3Path) {
		try {

		File f = new File(_basePath, s3Path);
		FileInputStream in = new FileInputStream(f);
		return in;
		} catch (FileNotFoundException e) {
			_log.error("get failed(" + s3Path + "):", e);
			throw new RuntimeException(e);
		}
	}

	@Override
  // this method supports multiple syntaxes: 
  // copy dir to dir
  // copy file to file
  // copy file to dir
	public String put(String key, String directory) {
		_log.debug("put(" + key + ", " + directory + ")");
		NYCFileUtils fs = new NYCFileUtils();
		String baseDirectoryName = _basePath + File.separator + fs.parseDirectory(key);
		File baseDirectory = new File(baseDirectoryName);
		if (!baseDirectory.exists()) {
		  baseDirectory.mkdirs();
		}
		String destFileName = _basePath + File.separator + key;
		File destLocation = new File(destFileName);
		File srcLocation = new File(directory);
		
		try {
			_log.debug("cp " + srcLocation + " " + destLocation);
      		fs.copyFiles(srcLocation, destLocation);
		} catch (Exception e) {
		  _log.error("put failed(" + key + ", " + directory + "):", e);
		}
		
		return null;
	}

	@Override
	public List<String> list(String directory, int maxResults) {
	  _log.info("list(" + _basePath +"/"+ directory + ")");
		File dir = new File(_basePath, directory);
		if (dir.list() == null) {
		  return new ArrayList<String>();
		}
		ArrayList<String> fullPaths = new ArrayList<String>();
		for (String file : dir.list()) {
		  File checkFile = new File(_basePath + File.separator + directory, file);
		  
		  if (checkFile.isDirectory()) {
		    // recurse
		    fullPaths.addAll(list(directory + File.separator + file, -1));
		  } else {
		    // TODO add a switch or param for this?
		    fullPaths.add(directory + File.separator + file);
		  }
		}
		_log.debug("list(" + directory + ")=" + fullPaths);
		return fullPaths;
	}

	@Override
	public String createOutputFilesZip(String directoryName) {
		// TODO
		_log.error("empty createOutputFileZip(" + directoryName + "):  please implement");
		return null;
	}

	@Override
	public void validateFileName(String fileName) {
		if(fileName.length() == 0) {
			throw new RuntimeException("File name contains characters that could lead to directory " +
					"traversal attack");
		}
		if(new File(fileName).isAbsolute()) {
			throw new RuntimeException("File name contains characters that could lead to directory " +
					"traversal attack");
		}
		if(fileName.contains("../") || fileName.contains("./")) {
			throw new RuntimeException("File name contains characters that could lead to directory " +
					"traversal attack");
		}
	}

	private boolean deleteFile(File file) {

	  if (file.isDirectory()) {
	    if (file.list().length == 0){
	      return file.delete();
	    } else {
	      String files[] = file.list();
	      for (String temp : files) {
	        File fileDelete = new File(file, temp);
	        //recursive delete
	        deleteFile(fileDelete);
	      }
	      return file.delete();
	    }
	  } else {
	    return file.delete();
	  }
	}

}
