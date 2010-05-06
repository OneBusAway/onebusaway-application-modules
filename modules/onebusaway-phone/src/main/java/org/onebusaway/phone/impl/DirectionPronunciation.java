package org.onebusaway.phone.impl;

import org.onebusaway.presentation.services.text.TextModification;

public class DirectionPronunciation implements TextModification {

  public String modify(String direction) {

    direction = direction.toUpperCase();

    if (direction.equals("N"))
      return "north";
    if (direction.equals("NW"))
      return "north west";
    if (direction.equals("W"))
      return "west";
    if (direction.equals("SW"))
      return "south west";
    if (direction.equals("S"))
      return "south";
    if (direction.equals("SE"))
      return "south east";
    if (direction.equals("E"))
      return "east";
    if (direction.equals("NE"))
      return "north east";
    return direction.toLowerCase();

  }
}
