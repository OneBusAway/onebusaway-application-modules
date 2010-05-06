package org.onebusaway.users.impl.validation;

import org.onebusaway.users.services.validation.KeyValidationProvider;
import org.onebusaway.users.utility.DigesterSignature;

import java.util.Map;

class KeyValidationProviderV1Impl implements KeyValidationProvider {

  private String _privateSalt;

  public void setPrivateSalt(String salt) {
    _privateSalt = salt;
  }

  public String getId() {
    return "v1";
  }

  public String generateKey(String input) {
    return DigesterSignature.generateKey(_privateSalt, input);
  }

  public boolean isValidKey(String key) {
    return DigesterSignature.isValidKey(_privateSalt, key);
  }

  @Override
  public void getKeyInfo(String subKey, Map<String, String> info) {
    String value = DigesterSignature.getDecodedValue(subKey);
    info.put("decodedValue", value);
  }
}
