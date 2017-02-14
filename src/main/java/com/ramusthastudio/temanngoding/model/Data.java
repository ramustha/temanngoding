package com.ramusthastudio.temanngoding.model;

public class Data {
  private int id;
  private String name;
  private String summary;
  private String description;
  private String address;
  private String begin_time;
  private String end_time;
  private int quota;
  private String owner_display_name;
  private String image_path;
  private String header_image;
  private String city_name;
  private String link;
  private int registrants;
  private int attenders;

  public int getId() {
    return id;
  }
  public Data setId(int aId) {
    id = aId;
    return this;
  }
  public String getName() {
    return name;
  }
  public Data setName(String aName) {
    name = aName;
    return this;
  }
  public String getSummary() {
    return summary;
  }
  public Data setSummary(String aSummary) {
    summary = aSummary;
    return this;
  }
  public String getDescription() {
    return description;
  }
  public Data setDescription(String aDescription) {
    description = aDescription;
    return this;
  }
  public String getAddress() {
    return address;
  }
  public Data setAddress(String aAddress) {
    address = aAddress;
    return this;
  }
  public String getBegin_time() {
    return begin_time;
  }
  public Data setBegin_time(String aBegin_time) {
    begin_time = aBegin_time;
    return this;
  }
  public String getEnd_time() {
    return end_time;
  }
  public Data setEnd_time(String aEnd_time) {
    end_time = aEnd_time;
    return this;
  }
  public int getQuota() {
    return quota;
  }
  public Data setQuota(int aQuota) {
    quota = aQuota;
    return this;
  }
  public String getOwner_display_name() {
    return owner_display_name;
  }
  public Data setOwner_display_name(String aOwner_display_name) {
    owner_display_name = aOwner_display_name;
    return this;
  }
  public String getImage_path() {
    return image_path;
  }
  public Data setImage_path(String aImage_path) {
    image_path = aImage_path;
    return this;
  }
  String getHeader_image() {
    return header_image;
  }
  public Data setHeader_image(String aHeader_image) {
    header_image = aHeader_image;
    return this;
  }
  public String getCity_name() {
    return city_name;
  }
  public Data setCity_name(String aCity_name) {
    city_name = aCity_name;
    return this;
  }
  public String getLink() {
    return link;
  }
  public Data setLink(String aLink) {
    link = aLink;
    return this;
  }
  public int getRegistrants() {
    return registrants;
  }
  public Data setRegistrants(int aRegistrants) {
    registrants = aRegistrants;
    return this;
  }
  public int getAttenders() {
    return attenders;
  }
  public Data setAttenders(int aAttenders) {
    attenders = aAttenders;
    return this;
  }

  @Override public String toString() {
    return "Data{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", summary='" + summary + '\'' +
        ", description='" + description + '\'' +
        ", address='" + address + '\'' +
        ", begin_time='" + begin_time + '\'' +
        ", end_time='" + end_time + '\'' +
        ", quota=" + quota +
        ", owner_display_name='" + owner_display_name + '\'' +
        ", image_path='" + image_path + '\'' +
        ", header_image='" + header_image + '\'' +
        ", city_name='" + city_name + '\'' +
        ", link='" + link + '\'' +
        ", registrants=" + registrants +
        ", attenders=" + attenders +
        '}';
  }
}
