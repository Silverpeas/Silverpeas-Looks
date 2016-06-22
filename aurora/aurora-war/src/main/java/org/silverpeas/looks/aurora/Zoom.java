package org.silverpeas.looks.aurora;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class Zoom {
  
  private String content;
  private PublicationDetail pub;
  public static final String SEPARATOR = "<hr />";
  
  public Zoom(PublicationDetail pub, String content) {
    this.pub = pub;
    this.content = content;
  }
  
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public PublicationDetail getPub() {
    return pub;
  }
  public void setPub(PublicationDetail pub) {
    this.pub = pub;
  }
  
  public String getBeginning() {
    if (StringUtil.isDefined(getContent()) && getContent().contains(SEPARATOR)) {
      return getContent().substring(0, getContent().indexOf(SEPARATOR));
    }
    return getContent();
  }
  
  

}
