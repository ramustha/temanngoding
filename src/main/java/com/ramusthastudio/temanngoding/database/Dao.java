package com.ramusthastudio.temanngoding.database;

import com.ramusthastudio.temanngoding.model.JoinEvent;
import com.ramusthastudio.temanngoding.model.Users;
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
