package com.ramusthastudio.infomovies.model;

public class Users {
  public long id;
  public String user_id;
  public String line_id;
  public String display_name;

  public Users(long aId, String aUserId, String aLineId, String aDisplayName) {
    id = aId;
    user_id = aUserId;
    line_id = aLineId;
    display_name = aDisplayName;
  }

  public Users() {

  }
}
