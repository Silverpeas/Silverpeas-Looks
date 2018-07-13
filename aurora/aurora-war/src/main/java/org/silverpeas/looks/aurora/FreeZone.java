package org.silverpeas.looks.aurora;

public class FreeZone {

  private String title;
  private String content;

  public FreeZone(String content) {
    setContent(content);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }
}
