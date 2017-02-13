package com.ramusthastudio.infomovies.database;

import com.ramusthastudio.infomovies.model.JoinEvent;
import com.ramusthastudio.infomovies.model.Users;
import java.util.List;

public interface Dao {
  public List<Users> get();
  public List<Users> getByUserId(String aUserId);
  public int registerLineId(String aUserId, String aLineId, String aDisplayName);
  public int joinEvent(String aEventId, String aUserId, String aLineId, String aDisplayName);
  public List<JoinEvent> getEvent();
  public List<JoinEvent> getByEventId(String aEventId);
  public List<JoinEvent> getByJoin(String aEventId, String aUserId);
}
