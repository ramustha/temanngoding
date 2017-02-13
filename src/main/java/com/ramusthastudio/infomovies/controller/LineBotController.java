package com.ramusthastudio.infomovies.controller;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.ramusthastudio.infomovies.database.Dao;
import com.ramusthastudio.infomovies.model.Event;
import com.ramusthastudio.infomovies.model.JoinEvent;
import com.ramusthastudio.infomovies.model.Payload;
import com.ramusthastudio.infomovies.model.Users;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

@RestController
@RequestMapping(value = "/linebot")
public class LineBotController {
  //inisialisasi channel secret
  @Autowired
  @Qualifier("com.linecorp.channel_secret")
  String lChannelSecret;

  //inisialisasi channel access token
  @Autowired
  @Qualifier("com.linecorp.channel_access_token")
  String lChannelAccessToken;

  @Autowired
  Dao mDao;

  private String displayName;
  private Payload payload;
  private String jObjGet = " ";

  @RequestMapping(value = "/callback", method = RequestMethod.POST)
  public ResponseEntity<String> callback(
      @RequestHeader("X-Line-Signature") String aXLineSignature,
      @RequestBody String aPayload) {
    // compose body
    final String text = String.format("The Signature is: %s",
        (aXLineSignature != null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");

    System.out.println(text);

    final boolean valid = new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);

    System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));

    //Get events from source
    if (aPayload != null && aPayload.length() > 0) {
      System.out.println("Payload: " + aPayload);
    }

    Gson gson = new Gson();
    payload = gson.fromJson(aPayload, Payload.class);

    //Variable initialization
    String msgText = " ";
    String idTarget = " ";
    String eventType = payload.events[0].type;

    //Get event's type
    if (eventType.equals("join")) {
      if (payload.events[0].source.type.equals("group")) {
        replyToUser(payload.events[0].replyToken, "Hello Group");
      }
      if (payload.events[0].source.type.equals("room")) {
        replyToUser(payload.events[0].replyToken, "Hello Room");
      }
    } else if (eventType.equals("follow")) {
      greetingMessage();
    } else if (eventType.equals("message")) {    //Event's type is message
      if (payload.events[0].source.type.equals("group")) {
        idTarget = payload.events[0].source.groupId;
      } else if (payload.events[0].source.type.equals("room")) {
        idTarget = payload.events[0].source.roomId;
      } else if (payload.events[0].source.type.equals("user")) {
        idTarget = payload.events[0].source.userId;
      }
      System.out.println("idTarget "+idTarget);
      //Parsing message from users
      if (!payload.events[0].message.type.equals("text")) {
        greetingMessage();
      } else {

        msgText = payload.events[0].message.text;
        msgText = msgText.toLowerCase();

        if (!msgText.contains("bot leave")) {
          if (msgText.contains("id") || msgText.contains("find") || msgText.contains("join") || msgText.contains("teman")) {
            processText(payload.events[0].replyToken, idTarget, msgText);
          } else {
            try {
              getEventData(msgText, payload, idTarget);
            } catch (IOException e) {
              System.out.println("Exception is raised ");
              e.printStackTrace();
            }
          }
        } else {
          if (payload.events[0].source.type.equals("group")) {
            leaveGR(payload.events[0].source.groupId, "group");
          } else if (payload.events[0].source.type.equals("room")) {
            leaveGR(payload.events[0].source.roomId, "room");
          }
        }

      }
    }

    return new ResponseEntity<String>(HttpStatus.OK);
  }

  //method untuk mengirim pesan saat ada users menambahkan bot sebagai teman
  private void greetingMessage() {
    getUserProfile(payload.events[0].source.userId);
    String greetingMsg =
        "Hi " + displayName + "! Pengen datang ke event developer tapi males sendirian? Aku bisa mencarikan kamu pasangan.";
    String action = "Lihat daftar event";
    String title = "Welcome";
    buttonTemplate(greetingMsg, action, action, title);

  }

  //method untuk mengirimkan pesan ke semua teman
  private void multicastMsg(String eventID, String userID) {
    List<String> listId = new ArrayList<>();
    List<JoinEvent> self = mDao.getByEventId("%" + eventID + "%");
    if (self.size() > 0) {
      for (int i = 0; i < self.size(); i++) {
        listId.add(self.get(i).getUser_id());
        listId.remove(userID);
      }
    }
    System.out.println(listId);
    String msg = "Hi, ada teman baru telah bergabung di event " + eventID;
    Set<String> stringSet = new HashSet<String>(listId);
    ButtonsTemplate buttonsTemplate = new ButtonsTemplate(null, null, msg,
        Collections.singletonList(new MessageAction("Lihat Teman", "teman #" + eventID)));
    TemplateMessage templateMessage = new TemplateMessage("List Teman", buttonsTemplate);
    Multicast multicast = new Multicast(stringSet, templateMessage);
    try {
      Response<BotApiResponse> response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .multicast(multicast)
          .execute();
      System.out.println(response.code() + " " + response.message());
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //method untuk membuat button template
  private void buttonTemplate(String message, String label, String action, String title) {
    ButtonsTemplate buttonsTemplate = new ButtonsTemplate(null, null, message,
        Collections.singletonList(new MessageAction(label, action)));
    TemplateMessage templateMessage = new TemplateMessage(title, buttonsTemplate);
    PushMessage pushMessage = new PushMessage(payload.events[0].source.userId, templateMessage);
    try {
      Response<BotApiResponse> response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .pushMessage(pushMessage)
          .execute();
      System.out.println(response.code() + " " + response.message());
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //method untuk memanggil dicoding event open api
  private void getEventData(String userTxt, Payload ePayload, String targetID) throws IOException {

    // Act as client with GET method
    String URI = "https://www.dicoding.com/public/api/events?limit=5";
    System.out.println("URI: " + URI);

    CloseableHttpAsyncClient c = HttpAsyncClients.createDefault();

    try {
      c.start();
      //Use HTTP Get to retrieve data
      HttpGet get = new HttpGet(URI);

      Future<HttpResponse> future = c.execute(get, null);
      org.apache.http.HttpResponse responseGet = future.get();
      System.out.println("HTTP executed");
      System.out.println("HTTP Status of response: " + responseGet.getStatusLine().getStatusCode());

      // Get the response from the GET request
      BufferedReader brd = new BufferedReader(new InputStreamReader(responseGet.getEntity().getContent()));

      StringBuffer resultGet = new StringBuffer();
      String lineGet = "";
      while ((lineGet = brd.readLine()) != null) {
        resultGet.append(lineGet);
      }
      System.out.println("Got result");

      // Change type of resultGet to JSONObject
      jObjGet = resultGet.toString();
      System.out.println("Event responses: " + jObjGet);
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    } finally {
      c.close();
    }

    Gson mGson = new Gson();
    Event event = mGson.fromJson(jObjGet, Event.class);

    if (userTxt.equals("lihat daftar event")) {
      System.out.println("lihat daftar event");
      pushMessage(targetID, "Aku akan mencarikan event aktif di dicoding! Dengan syarat : Kasih tau dong LINE ID kamu (pake \'id @\' ya). Contoh :");
      pushMessage(targetID, "id @john");
    } else if (userTxt.contains("summary")) {
      pushMessage(targetID, event.getData().get(Integer.parseInt(String.valueOf(userTxt.charAt(1))) - 1).getSummary());
    } else {
      pushMessage(targetID, "Hi " + displayName + ", aku belum  mengerti maksud kamu. Silahkan ikuti petunjuk ya :)");
      greetingMessage();
    }
  }

  //Method untuk reply message
  private void replyToUser(String rToken, String messageToUser) {
    TextMessage textMessage = new TextMessage(messageToUser);
    ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);
    try {
      Response<BotApiResponse> response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .replyMessage(replyMessage)
          .execute();
      System.out.println("Reply Message: " + response.code() + " " + response.message());
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //method untuk mendapatkan profile users (users id, display name, image, status)
  private void getUserProfile(String userId) {
    Response<UserProfileResponse> response =
        null;
    try {
      response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .getProfile(userId)
          .execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (response.isSuccessful()) {
      UserProfileResponse profile = response.body();
      System.out.println(profile.getDisplayName());
      System.out.println(profile.getPictureUrl());
      System.out.println(profile.getStatusMessage());
      displayName = profile.getDisplayName();
    } else {
      System.out.println(response.code() + " " + response.message());
    }
  }


  //Method untuk push message
  private void pushMessage(String sourceId, String txt) {
    System.out.println("sourceId ::"+sourceId + " txt " + txt);
    TextMessage textMessage = new TextMessage(txt);
    PushMessage pushMessage = new PushMessage(sourceId, textMessage);
    System.out.println("pushMessage ::"+pushMessage.toString());
    try {
      Response<BotApiResponse> response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .pushMessage(pushMessage)
          .execute();
      System.out.println("pushMessage ::"+response.code() + " " + response.message());
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //method untuk template message berupa carousel
  private void carouselTemplateMessage(String sourceId) {
    Gson mGson = new Gson();
    Event event = mGson.fromJson(jObjGet, Event.class);

    int i;
    String image, owner, name, id, link;
    CarouselColumn column;
    List<CarouselColumn> carouselColumn = new ArrayList<>();
    for (i = 0; i < event.getData().size(); i++) {
      image = event.getData().get(i).getImage_path();
      owner = event.getData().get(i).getOwner_display_name();
      name = event.getData().get(i).getName();
      id = String.valueOf(event.getData().get(i).getId());
      link = event.getData().get(i).getLink();

      column = new CarouselColumn(image, name.substring(0, (name.length() < 40) ? name.length() : 40), owner,
          Arrays.asList(new MessageAction("Summary", "[" + String.valueOf(i) + "]" + " Summary : " + name),
              new URIAction("View Page", link),
              new MessageAction("Join Event", "join event #" + id)));
      carouselColumn.add(column);
    }

    CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumn);
    TemplateMessage templateMessage = new TemplateMessage("Your search result", carouselTemplate);
    PushMessage pushMessage = new PushMessage(sourceId, templateMessage);
    try {
      Response<BotApiResponse> response = LineMessagingServiceBuilder
          .create(lChannelAccessToken)
          .build()
          .pushMessage(pushMessage)
          .execute();
      System.out.println(response.code() + " " + response.message());
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //Method for leave group or room
  private void leaveGR(String id, String type) {
    try {
      if (type.equals("group")) {
        Response<BotApiResponse> response = LineMessagingServiceBuilder
            .create(lChannelAccessToken)
            .build()
            .leaveGroup(id)
            .execute();
        System.out.println(response.code() + " " + response.message());
      } else if (type.equals("room")) {
        Response<BotApiResponse> response = LineMessagingServiceBuilder
            .create(lChannelAccessToken)
            .build()
            .leaveRoom(id)
            .execute();
        System.out.println(response.code() + " " + response.message());
      }
    } catch (IOException e) {
      System.out.println("Exception is raised ");
      e.printStackTrace();
    }
  }

  //method yang berisi keyword dan trigger yang berhubungan dengan database
  private void processText(String aReplyToken, String aUserId, String aText) {
    System.out.println("message text: " + aText + " from: " + aUserId);

    String[] words = aText.trim().split("\\s+");
    String intent = words[0];
    System.out.println("intent: " + intent);
    String msg = " ";

    String lineId = " ";
    String eventId = " ";

    if (intent.equalsIgnoreCase("id")) {
      String target = words.length > 1 ? words[1] : "";
      if (target.length() <= 3) {
        msg = "Need more than 3 character to find users";
      } else {
        lineId = aText.substring(aText.indexOf("@") + 1);
        getUserProfile(payload.events[0].source.userId);
        String status = regLineID(aUserId, lineId, displayName);
        String message = status + "\nHi, berikut adalah event aktif yang bisa kamu pilih :";
        pushMessage(aUserId, message);
        carouselTemplateMessage(aUserId);
        return;
      }
    } else if (intent.equalsIgnoreCase("join")) {
      eventId = aText.substring(aText.indexOf("#") + 1);
      getUserProfile(payload.events[0].source.userId);
      lineId = findUser(aUserId);
      joinEvent(eventId, aUserId, lineId, displayName);
      return;
    } else if (intent.equalsIgnoreCase("teman")) {
      eventId = aText.substring(aText.indexOf("#") + 1);
      String txtMessage = findEvent(eventId);
      replyToUser(aReplyToken, txtMessage);
      return;
    }

    // if msg is invalid
    if (msg == " ") {
      replyToUser(aReplyToken, "Message invalid");
    }
  }

  //method mendaftarkan LINE ID
  private String regLineID(String aUserId, String aLineId, String aDisplayName) {
    String regStatus;
    String exist = findUser(aUserId);
    if (exist == "Users not found") {
      int reg = mDao.registerLineId(aUserId, aLineId, aDisplayName);
      if (reg == 1) {
        regStatus = "Yay berhasil mendaftar!";
      } else {
        regStatus = "yah gagal mendaftar :(";
      }
    } else {
      regStatus = "Anda sudah terdaftar";
    }

    return regStatus;
  }

  //method untuk mencari users terdaftar di database
  private String findUser(String aUSerId) {
    String txt = "";
    List<Users> self = mDao.getByUserId("%" + aUSerId + "%");
    if (self.size() > 0) {
      for (int i = 0; i < self.size(); i++) {
        Users users = self.get(i);
        txt = getUserString(users);
      }

    } else {
      txt = "Users not found";
    }
    return txt;
  }

  private String getUserString(Users users) {
    return users.line_id;
  }

  //method untuk bergabung dalam event
  private void joinEvent(String eventID, String aUserId, String lineID, String aDisplayName) {
    String joinStatus;
    String exist = findFriend(eventID, aUserId);
    if (Objects.equals(exist, "Event not found")) {
      int join = mDao.joinEvent(eventID, aUserId, lineID, aDisplayName);
      if (join == 1) {
        joinStatus = "Kamu berhasil bergabung pada event ini. Berikut adalah beberapa teman yang bisa menemani kamu. Silahkan invite LINE ID berikut menjadi teman di LINE kamu ya :)";
        buttonTemplate(joinStatus, "Lihat Teman", "teman #" + eventID, "List Teman");
        multicastMsg(eventID, aUserId);
      } else {
        pushMessage(aUserId, "yah gagal bergabung :(");
      }
    } else {
      buttonTemplate("Anda sudah tergabung di event ini", "Lihat Teman", "teman #" + eventID, "List Teman");
    }

  }

  //method untuk mencari data di table event berdasarkan event id
  private String findEvent(String eventID) {
    String txt = "Daftar teman di event " + eventID + " :";
    List<JoinEvent> self = mDao.getByEventId("%" + eventID + "%");
    if (self.size() > 0) {
      for (int i = 0; i < self.size(); i++) {
        JoinEvent joinEvent = self.get(i);
        txt = txt + "\n\n";
        txt = txt + getEventString(joinEvent);
      }

    } else {
      txt = "Event not found";
    }
    return txt;
  }

  //method untuk melihat teman terdaftar di dalam suatu event
  private String findFriend(String eventID, String userID) {
    String txt = "Daftar teman di event " + eventID + " :";
    List<JoinEvent> self = mDao.getByJoin(eventID, userID);
    if (self.size() > 0) {
      for (int i = 0; i < self.size(); i++) {
        JoinEvent joinEvent = self.get(i);
        txt = txt + "\n\n";
        txt = txt + getEventString(joinEvent);
      }

    } else {
      txt = "Event not found";
    }
    return txt;
  }

  private String getEventString(JoinEvent joinEvent) {
    return String.format("Display Name: %s\nLINE ID: %s\n", joinEvent.getDisplay_name(), "http://line.me/ti/p/~" + joinEvent.getLine_id());
  }

}
