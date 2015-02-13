package org.silverpeas.looks.aurora;

import java.io.File;
import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public final class HeaderImageHelper {

  private static File[] images;

  static {
    ResourceLocator settings =
        new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.CG11", "");
    String imagesRepository = settings.getString("header.images.directory");
    try {
      Collection<File> cImages = FileFolderManager.getAllImages(imagesRepository);
      images = cImages.toArray(new File[cImages.size()]);
    } catch (UtilException e) {
      SilverTrace.error("lookCG11", "HeaderImageHelper.init", "", e);
    }
  }

  public static int getNbImages() {
    if (images != null) {
      return images.length;
    }
    return -1;
  }

  public static File getImage(int n) {
    if (images != null) {
      return images[n];
    }
    return null;
  }

}
