/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

  public String generateKey(String input, String... arguments) {
    return DigesterSignature.generateKey(_privateSalt, input);
  }

  public boolean isValidKey(String key, String... arguments) {
    return DigesterSignature.isValidKey(_privateSalt, key);
  }

  @Override
  public void getKeyInfo(Map<String, String> info, String subKey, String... arguments) {
    String value = DigesterSignature.getDecodedValue(subKey);
    info.put("decodedValue", value);
  }
}
