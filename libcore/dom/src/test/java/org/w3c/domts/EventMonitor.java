/*
 * Copyright (c) 2001-2004 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.domts;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *   This is a utility implementation of EventListener
 *      that captures all events and provides access
 *      to lists of all events by mode
 */
public class EventMonitor
    implements EventListener {
  private final List atEvents = new ArrayList();
  private final List bubbledEvents = new ArrayList();
  private final List capturedEvents = new ArrayList();
  private final List allEvents = new ArrayList();

  public EventMonitor() {
  }

  public void handleEvent(Event evt) {
    switch (evt.getEventPhase()) {
      case Event.CAPTURING_PHASE:
        capturedEvents.add(evt);
        break;

      case Event.BUBBLING_PHASE:
        bubbledEvents.add(evt);
        break;

      case Event.AT_TARGET:
        atEvents.add(evt);
        break;
    }
    allEvents.add(evt);
  }

  public List getAllEvents() {
    return new ArrayList(allEvents);
  }

  public List getBubbledEvents() {
    return new ArrayList(bubbledEvents);
  }

  public List getAtEvents() {
    return new ArrayList(atEvents);
  }

  public List getCapturedEvents() {
    return new ArrayList(capturedEvents);
  }
}
