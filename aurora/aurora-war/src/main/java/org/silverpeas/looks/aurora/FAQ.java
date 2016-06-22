package org.silverpeas.looks.aurora;

import org.silverpeas.components.questionreply.model.Question;

/**
 * @author Nicolas Eysseric
 */
public class FAQ {

  private Question question = null;
  private boolean canAskAQuestion = false;

  public FAQ(Question question) {
    this.question = question;
  }

  public void setCanAskAQuestion(boolean canAsk) {
    this.canAskAQuestion = canAsk;
  }

  public boolean isCanAskAQuestion() {
    return canAskAQuestion;
  }

  public Question getQuestion() {

    return question;
  }
}
