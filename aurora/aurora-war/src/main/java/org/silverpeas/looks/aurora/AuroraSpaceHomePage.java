/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.looks.aurora;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.Shortcut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.looks.aurora.AuroraSpaceHomePageZone.MAIN;
import static org.silverpeas.looks.aurora.AuroraSpaceHomePageZone.RIGHT;

public class AuroraSpaceHomePage {

  private Space space;
  private String userLanguage;
  private ComponentInstLight backOfficeApp;
  private DataRecord data;
  private LookAuroraHelper look;
  private Form customForm;

  public static final String TEMPLATE_NAME = "auroraspacehomepage.xml";

  public AuroraSpaceHomePage(LookAuroraHelper look, SpaceInstLight space) {
    this.look = look;
    this.userLanguage = look.getLanguage();
    this.space = new Space(space, userLanguage);
    initBackOffice();
  }

  public Space getSpace() {
    String intro = getFieldWysiwygValue("intro");
    space.setIntro(intro);
    String picture = getFieldImageURL("picture");
    space.setPicture(picture);
    return space;
  }

  public List<PublicationDetail> getPublications() {
    int nbPublicationsToDisplay = 0;
    if (data == null && look.getSettings("space.homepage.latestpublications", true)) {
      nbPublicationsToDisplay = look.getSettings("space.homepage.latestpublications.nb", 5);
    } else if (data != null) {
      nbPublicationsToDisplay = getFieldIntValue("latestPublications");
    }
    if (nbPublicationsToDisplay > 0) {
      return look.getLastUpdatedPublicationsSince(getSpace().getId(), 0, nbPublicationsToDisplay);
    }
    return emptyList();
  }

  public NewsList getNews() {
    final int nbNews = ofNullable(getFieldValue("newsLimit"))
        .filter(StringUtil::isInteger)
        .map(Integer::parseInt)
        .map(l -> Math.min(l, look.getSettings("space.homepage.news.nb.max", 10)))
        .orElse(look.getSettings("space.homepage.news.nb.default", 5));
    final boolean displayNews = nbNews > 0 && isEnabled("space.homepage.news", "displayNews");
    final NewsList newsList;
    if (displayNews) {
      final boolean includeSubSpaces = getFieldBooleanValue("newsOfSubSpaces");
      final boolean importantOnly = getFieldBooleanValue("newsImportantOnly");
      newsList = look.getNewsOfSpace(getSpace().getId(), includeSubSpaces, importantOnly, nbNews);
      ofNullable(getFieldValue("newsZone"))
          .filter(StringUtil::isDefined)
          .map(String::toUpperCase)
          .map(AuroraSpaceHomePageZone::valueOf)
          .ifPresent(newsList::setZone);
      ofNullable(getFieldValue("newsRenderingType"))
          .filter(StringUtil::isDefined)
          .map(String::toUpperCase)
          .map(NewsList.RenderingType::valueOf)
          .ifPresent(newsList::setRenderingType);
      newsList.setImageSize(look.getSettings("space.homepage.news.image.width", "800") + "x" +
          look.getSettings("space.homepage.news.image.height", EMPTY));
    } else {
      newsList = new NewsList(Collections.emptyList(), null);
    }
    return newsList;
  }

  public Map<AuroraSpaceHomePageZone, FreeZone> getFreeZones() {
    return EnumSet.of(MAIN, RIGHT)
        .stream()
        .flatMap(p -> {
          final String prefix = p.name().toLowerCase();
          return ofNullable(getFieldWysiwygValue(prefix + "FreeContentValue"))
              .filter(StringUtil::isDefined)
              .map(FreeZone::new)
              .map(z -> {
                z.setTitle(getFieldValue(prefix + "FreeContentTitle"));
                return Pair.of(p, z);
              })
              .stream();
        })
        .collect(toMap(Pair::getFirst, Pair::getSecond));
  }

  public boolean isTaxonomyEnabled() {
    return !"0".equals(getTaxonomyMode());
  }

  public String getTaxonomyMode() {
    return getFieldValue("displayTaxonomy");
  }

  public List<Space> getSubSpaces() {
    boolean display = isEnabled("space.homepage.subspaces", "displaySubspaces");
    if (display) {
      OrganizationController oc = OrganizationController.get();
      // get allowed subspaces
      String[] subspaceIds = oc.getAllowedSubSpaceIds(look.getUserId(), getSpace().getId());
      List<Space> subspaces = new ArrayList<>();
      for (String subspaceId : subspaceIds) {
        subspaces.add(new Space(oc.getSpaceInstLightById(subspaceId), userLanguage));
      }
      return subspaces;
    }
    return emptyList();
  }

  public List<App> getApps() {
    boolean display = isEnabled("space.homepage.apps", "displayApps");
    if (display) {
      List<App> apps = new ArrayList<>();
      List<ComponentInstLight> components = getAllowedComponents(true, null);
      for (ComponentInstLight component : components) {
        apps.add(new App(component, userLanguage));
      }
      return apps;
    }
    return emptyList();
  }

  public List<UserDetail> getAdmins() {
    boolean displayAdmins = isEnabled("space.homepage.admins", "displayAdmins");
    if (displayAdmins) {
      return look.getSpaceAdmins(getSpace().getId());
    }
    return emptyList();
  }

  public NextEvents getNextEvents() {
    boolean displayEvents = isEnabled("space.homepage.events", "displayEvents");
    if (displayEvents) {
      // get allowed apps
      List<String> allowedAppIds = new ArrayList<>();
      List<ComponentInstLight> components = getAllowedComponents(false, "almanach");
      for (ComponentInstLight component : components) {
         allowedAppIds.add(component.getId());
      }
      return look.getNextEvents(allowedAppIds, true, 5, false);
    }
    return null;
  }

  private List<ComponentInstLight> getAllowedComponents(boolean visibleOnly, String name) {
    OrganizationController oc = OrganizationController.get();
    String[] appIds = oc.getAvailCompoIdsAtRoot(getSpace().getId(), look.getUserId());
    List<ComponentInstLight> components = new ArrayList<>();
    Predicate<ComponentInstLight> canBeGet = c -> !visibleOnly || !c.isHidden();
    Predicate<ComponentInstLight> matchesName =
        c -> StringUtil.isNotDefined(name) || c.getName().equals(name);
    for (String appId : appIds) {
      ComponentInstLight component = oc.getComponentInstLight(appId);
      if (canBeGet.test(component) && matchesName.test(component)) {
          components.add(component);
      }
    }
    return components;
  }

  public List<Media> getLatestMedias() {
    boolean display = isEnabled("space.homepage.medias", "displayMedias");
    if (display) {
      List<ComponentInstLight> galleries = getAllowedComponents(false,"gallery");
      if (CollectionUtil.isNotEmpty(galleries)) {
        ComponentInstLight gallery = galleries.get(0);
        return MediaServiceProvider.getMediaService().getLastRegisteredMedia(gallery.getId());
      }
    }
    return emptyList();
  }

  public String getSecondPicture() {
    return getFieldImageURL("secondPicture");
  }

  public String getSecondPictureLink() {
    return getFieldValue("secondPictureLink");
  }

  public List<User> getUsers() {
    List<User> users = new ArrayList<>();
    final String userIds = getFieldValue("users");
    if (StringUtil.isDefined(userIds)) {
      String[] ids = userIds.split(",");
      for (String id : ids) {
        User user = User.getById(id);
        if (user.isActivatedState()) {
          users.add(user);
        }
      }
    }
    return users;
  }

  public String getUsersLabel() {
    return getFieldValue("usersLabel");
  }

  public List<Shortcut> getShortcuts() {
    List<Shortcut> shortcuts = new ArrayList<>();
    if (getData() != null) {
      for (int i=1; i<=10; i++) {
        Shortcut shortcut = getShortcut("shortcut", String.valueOf(i));
        if (shortcut != null) {
          shortcuts.add(shortcut);
        }
      }
    }
    return shortcuts;
  }

  public NewUsersList getNewUsers() {
    if (getFieldBooleanValue("newUsers")) {
      return look.getSpaceNewUsersList(getSpace().getId());
    }
    return null;
  }

  private Shortcut getShortcut(String name, String nb) {
    Shortcut shortcut = null;
    String url = getFieldValue(name+"URL"+nb);
    if (StringUtil.isDefined(url)) {
      String icon = getFieldImageURL(name+"Icon"+nb);
      String target = getFieldValue(name+"Target"+nb);
      String alt = getFieldValue(name+"Label"+nb);
      shortcut = new Shortcut(icon, target, url, alt);
    }
    return shortcut;
  }

  private boolean isEnabled(String lookParamName, String templateParamName) {
    boolean enabled = (data == null && look.getSettings(lookParamName, true));
    if (data != null) {
      enabled = getFieldBooleanValue(templateParamName);
    }
    return enabled;
  }

  private void initBackOffice() {
    ComponentInstLight app = look.getConfigurationApp(getSpace().getId());
    if (app != null) {
      setBackOfficeApp(app);
      setData(getConfigurationData(app.getId()));
      setCustomForm(app.getId());
    }
  }

  private ComponentInstLight getBackOfficeApp() {
    return backOfficeApp;
  }

  private void setBackOfficeApp(final ComponentInstLight backOfficeApp) {
    this.backOfficeApp = backOfficeApp;
  }

  private DataRecord getData() {
    return data;
  }

  private void setData(final DataRecord data) {
    this.data = data;
  }

  private DataRecord getConfigurationData(String appId) {
    return getConfigurationData(appId, "xmlTemplate");
  }

  private DataRecord getConfigurationData(String appId, String paramName) {
    PublicationTemplate pubTemplate = getTemplate(appId, paramName);
    return getDataRecord(pubTemplate);
  }

  private boolean getFieldBooleanValue(String name) {
    return StringUtil.getBooleanValue(getFieldValue(name));
  }

  private int getFieldIntValue(String name) {
    String value = getFieldValue(name);
    if (StringUtil.isInteger(value)) {
      return Integer.parseInt(value);
    }
    return 0;
  }

  private String getFieldImageURL(String name) {
    if (getBackOfficeApp() == null || data == null) {
      return "";
    }
    try {
      Field field = data.getField(name);
      if (field != null) {
        String fieldValue = field.getValue();
        if (fieldValue != null) {
          String attachmentId =
              fieldValue.substring(fieldValue.indexOf('_') + 1, fieldValue.length());
          Optional<String> attachmentUrl = getAttachmentURL(attachmentId);
          if (attachmentUrl.isPresent()) {
            return attachmentUrl.get();
          }
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return "";
  }

  private Optional<String> getAttachmentURL(final String attachmentId) {
    if (StringUtil.isDefined(attachmentId)) {
      if (attachmentId.startsWith("/")) {
        // case of an image provided by a gallery
        return Optional.of(attachmentId);
      } else {
        SimpleDocument attachment = AttachmentService.get().searchDocumentById(
            new SimpleDocumentPK(attachmentId, getBackOfficeApp().getId()), null);
        if (attachment != null) {
          return Optional.of(URLUtil.getApplicationURL() + attachment.getAttachmentURL());
        }
      }
    }
    return Optional.empty();
  }

  private String getFieldWysiwygValue(String name) {
    if (getBackOfficeApp() == null) {
      return "";
    }
    try {
      String text =
          WysiwygFCKFieldDisplayer.getContentFromFile(getBackOfficeApp().getId(), "0", name);
      return WysiwygContentTransformer.on(text)
          .modifyImageUrlAccordingToHtmlSizeDirective()
          .resolveVariablesDirective()
          .applySilverpeasLinkCssDirective()
          .transform();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return "";
    }
  }

  private String getFieldValue(String name) {
    if (getBackOfficeApp() == null || data == null) {
      return "";
    }
    try {
      Field field = data.getField(name);
      if (field != null) {
        return field.getStringValue();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return "";
  }

  public boolean isAdmin() {
    return look.isSpaceAdmin(getSpace().getId());
  }

  public String getBackOfficeURL() {
    boolean active = look.getSettings("space.homepage.management.active", false);
    if (active && isAdmin()) {
      return URLUtil.getApplicationURL() + "/AuroraSpaceHomepageBackoffice?SpaceId=" + getSpace().getId();
    }
    return null;
  }

  private PublicationTemplate getTemplate(String appId, String paramName) {
    String xmlFormName = OrganizationController.get().getComponentParameterValue(appId, paramName);
    if (!xmlFormName.isEmpty()) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      try {
        return PublicationTemplateManager.getInstance()
            .getPublicationTemplate(appId + ":" + xmlFormShortName);
      } catch (Exception e) {
        SilverLogger.getLogger(this).info("On app #"+appId+", template "+xmlFormShortName+" not yet used !");
      }
    }
    return null;
  }

  private DataRecord getDataRecord(PublicationTemplate template) {
    if (template != null) {
      try {
        RecordSet recordSet = template.getRecordSet();
        DataRecord record = recordSet.getRecord("0", userLanguage);
        if (record == null) {
          record = recordSet.getEmptyRecord();
          record.setId("0");
          record.setLanguage(userLanguage);
        }
        return record;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  private void setCustomForm(String appId) {
    PublicationTemplate customTemplate = getTemplate(appId, "xmlTemplate2");
    if (customTemplate != null) {
      DataRecord dataRecord = getDataRecord(customTemplate);
      if (dataRecord != null) {
        try {
          Form form = customTemplate.getViewForm();
          form.setData(dataRecord);
          setCustomForm(form);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
  }

  public String getCustomFormContent() {
    if (getCustomForm() != null) {
      try {
        PagesContext formContext = new PagesContext();
        formContext.setComponentId(getBackOfficeApp().getId());
        formContext.setLanguage(userLanguage);

        return getCustomForm().toString(formContext);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return "";
  }

  private Form getCustomForm() {
    return customForm;
  }

  private void setCustomForm(final Form customForm) {
    this.customForm = customForm;
  }
}
