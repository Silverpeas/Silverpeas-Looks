package org.silverpeas.looks.aurora;

import java.util.Objects;

/**
 * @author Nicolas
 */
public class NewsListButton {

  private String label;
  private String param;

  public NewsListButton(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getParam() {
    return param;
  }

  public void setParam(final String param) {
    this.param = param;
  }

  public String getParamAsCSS() {
    return "taxonomy-"+param.replace(':','-').replace('/','_');
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NewsListButton that = (NewsListButton) o;
    return param.equals(that.param);
  }

  @Override
  public int hashCode() {
    return Objects.hash(param);
  }
}
