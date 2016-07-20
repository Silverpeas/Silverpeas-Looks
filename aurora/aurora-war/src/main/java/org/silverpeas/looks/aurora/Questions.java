package org.silverpeas.looks.aurora;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.util.URLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class Questions {

  private List<Question> list = new ArrayList<Question>();
  private boolean canAskAQuestion = false;
  private String appId;

  public void setCanAskAQuestion(boolean canAsk) {
    this.canAskAQuestion = canAsk;
  }

  public boolean isCanAskAQuestion() {
    return canAskAQuestion;
  }

  protected void setAppId(String appId) {
    this.appId = appId;
  }

  public String getRequestURL() {
    return "/RquestionReply/"+appId+"/CreateQQuery";
  }

  public String getAppURL() {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, appId);
  }

  public List<Question> getList() {
    return list;
  }

  public void add(Question q) {
    list.add(q);
  }

}
