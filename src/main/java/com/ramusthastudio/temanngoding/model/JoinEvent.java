package com.ramusthastudio.temanngoding.model;

public class JoinEvent {
  private long id;
  private String event_id;
  private String user_id;
  private String line_id;
  private String display_name;

  public JoinEvent(Long aId, String aEventId, String aUserId, String aLineId, String aDisplayName) {
    id = aId;
    event_id = aEventId;
    user_id = aUserId;
    line_id = aLineId;
    display_name = aDisplayName;
  }

  public JoinEvent() {

  }

  public long getId() {
    return id;
  }
  public String getEvent_id() {
    return event_id;
  }
  public String getUser_id() {
    return user_id;
  }
  public String getLine_id() {
    return line_id;
  }
  public String getDisplay_name() {
    return display_name;
  }
}
