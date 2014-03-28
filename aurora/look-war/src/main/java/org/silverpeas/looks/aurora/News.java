package org.silverpeas.looks.aurora;

import java.io.Serializable;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class News implements Serializable {

  private static final long serialVersionUID = -2850550696131967217L;

  private PublicationDetail publication;
  private boolean haveContent = false;

  public News(PublicationDetail publication, boolean haveContent) {
    this.publication = publication;
    this.haveContent = haveContent;
  }

  public PublicationDetail getPublicationDetail() {
    return publication;
  }

  public boolean haveContent() {
    return haveContent;
  }
  
  public String getThumbnailURL() {
    String thumbnail = publication.getImage();
    if (!StringUtil.isDefined(thumbnail)) {
      thumbnail = "/weblib/sdis84/look/images/photo_actu.jpg";
    } else if (thumbnail.startsWith("/")) {
      thumbnail += "&Size=133x100";
    } else {
      thumbnail =
          FileServerUtils.getUrl(publication.getPK().getComponentName(), "vignette",
              publication.getImage(), publication.getImageMimeType(), "images");
    }
    return thumbnail;
  }
}