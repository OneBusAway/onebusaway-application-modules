package org.onebusaway.transit_data_federation.model.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.MessageDigest;

public class BundleFileItem implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private String filename;
  
  private String md5;
  
  public BundleFileItem() {}

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }
  
  public boolean verifyMd5(File filename) throws Exception {
    MessageDigest md5Hasher = MessageDigest.getInstance("MD5");

    FileInputStream in = new FileInputStream(filename.getPath());
    byte data[] = new byte[1024];
    while(true) {
      int readBytes = in.read(data, 0, data.length);

      if(readBytes < 0) {
        break;
      }
      
      md5Hasher.update(data, 0, readBytes);
    }

    byte messageDigest[] = md5Hasher.digest();
    StringBuffer hexString = new StringBuffer();

    for(int i=0; i < messageDigest.length; i++) {
      String hex = Integer.toHexString(0xFF & messageDigest[i]); 
      
      if(hex.length() == 1) {
        hexString.append('0');
      }

      hexString.append(hex);
    }
    
    return hexString.toString().equals(this.md5);
  }

}
