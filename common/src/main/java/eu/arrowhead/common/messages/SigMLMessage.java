/*
 *  Copyright (c) 2018 Jens Eliasson, Luleå University of Technology
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.Utility;
import java.util.Vector;
import java.util.Iterator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SigMLMessage{

  @Valid
  @NotNull
  public int p;

  public Vector<SenMLMessage> sml = null;

  public SigMLMessage() {
    this.p = 0;
  }

  public SigMLMessage(short p) {
    this.p = p;

  }

  public void setp(short p) {
    this.p = p;
  }

  public int getp() {
    return p;
  }

  public void setSenML(Vector<SenMLMessage> msg) {
    this.sml = msg;
  }

  public Vector<SenMLMessage> getSenML() {
    return sml;
  }

  public String toString() {
    return Utility.toPrettyJson(null, this);
  }

  /*public SigMLMessage(short p, Vector<SenMLMessage> sml) {
    this.p = p;
    this.sml = sml;
  }*/

}
