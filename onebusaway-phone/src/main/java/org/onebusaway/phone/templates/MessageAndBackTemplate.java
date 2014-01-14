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
package org.onebusaway.phone.templates;

import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/message_and_back")
public class MessageAndBackTemplate extends AbstractAgiTemplate {

  public MessageAndBackTemplate() {
    super(true);
  }

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack stack = context.getValueStack();
    String message = stack.findString("message");
    String nextAction = stack.findString("nextAction");

    if (message != null)
      addMessage(message);
    else
      System.err.println("no message specified");

    if (nextAction != null) {
      setNextAction(nextAction);
    } else {
      addAction(".*\\*", "/back");
      setNextAction("/back");
    }
  }
}
