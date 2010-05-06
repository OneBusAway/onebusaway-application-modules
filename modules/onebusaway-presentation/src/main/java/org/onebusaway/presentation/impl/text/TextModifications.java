package org.onebusaway.presentation.impl.text;

import org.onebusaway.presentation.services.text.TextModification;

import java.util.ArrayList;
import java.util.List;

public class TextModifications implements TextModification {

  private List<TextModification> _modifications = new ArrayList<TextModification>();

  public void setModifications(List<TextModification> modifications) {
    _modifications = modifications;
  }

  public void addModification(TextModification modification) {
    _modifications.add(modification);
  }

  public String modify(String input) {
    for (TextModification modification : _modifications)
      input = modification.modify(input);
    return input;
  }
}
