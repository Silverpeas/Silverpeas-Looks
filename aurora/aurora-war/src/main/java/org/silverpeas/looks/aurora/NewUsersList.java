package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.user.model.User;

import java.util.List;

/**
 * @author Nicolas
 */
public class NewUsersList {

  List<User> users;
  List<String> fields;
  boolean avatar;

  public NewUsersList(List<User> users) {
    this.users = users;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(final List<User> users) {
    this.users = users;
  }

  public List<String> getFields() {
    return fields;
  }

  public void setFields(final List<String> fields) {
    this.fields = fields;
  }

  public boolean isAvatar() {
    return avatar;
  }

  public void setAvatar(final boolean avatar) {
    this.avatar = avatar;
  }
}
