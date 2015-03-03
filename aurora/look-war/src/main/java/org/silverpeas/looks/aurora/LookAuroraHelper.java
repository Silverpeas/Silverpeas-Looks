package org.silverpeas.looks.aurora;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceFactory;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;
import org.silverpeas.wysiwyg.control.WysiwygController;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.look.LookSilverpeasV5Helper;
import com.silverpeas.look.PublicationHelper;
import com.silverpeas.look.Shortcut;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.PersonalSpaceController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class LookAuroraHelper extends LookSilverpeasV5Helper {

  List<Heading> headings = null;
  private DelegatedNewsService delegatedNewsService = null;
  private PublicationHelper kmeliaTransversal = null;
  
  public LookAuroraHelper(HttpSession session) {
    super(session);

    delegatedNewsService = ServicesFactory.getDelegatedNewsService();
  }
  
  public List<BannerMainItem> getBannerMainItems() {
    String param = getSettings("banner.spaces", null);
    if (param != null) {
      List<BannerMainItem> items = new ArrayList<BannerMainItem>();
      OrganisationController oc = getOrganisationController();
      String[] spaceIds = StringUtils.split(param);
      for (String spaceId : spaceIds) {
        if (oc.isSpaceAvailable(spaceId, getUserId())) {
          BannerMainItem item = new BannerMainItem(oc.getSpaceInstLightById(spaceId));
          String[] subspaceIds = oc.getAllSubSpaceIds(spaceId, getUserId());
          for (String subspaceId : subspaceIds) {
            item.addSubspace(oc.getSpaceInstLightById(subspaceId));
          }
          String[] appIds = oc.getAvailCompoIdsAtRoot(spaceId, getUserId());
          for (String appId : appIds) {
            item.addApp(oc.getComponentInstLight(appId));
          }
          items.add(item);
        }
      }
      return items;
    }
    return new ArrayList<BannerMainItem>();
  }

  public String getProjectsSpaceId() {
    return getSettings("projects.spaceId", "0");
  }

  public boolean areSomeProjectsAvailables() {
    String spaceIdProjects = getProjectsSpaceId();
    String[] spaceIds =
        getOrganizationController().getAllowedSubSpaceIds(getUserId(), spaceIdProjects);
    return spaceIds != null && spaceIds.length > 0;
  }
  
  public List<Project> getProjects() {
    List<Project> projects = new ArrayList<Project>();
    String[] spaceIds = getOrganizationController().getAllowedSubSpaceIds(getUserId(), getProjectsSpaceId());
    for (String spaceId : spaceIds) {
      SpaceInstLight space = getOrganizationController().getSpaceInstLightById(spaceId);
      if (space != null) {
        Project project = new Project(space);
        String[] componentIds = getOrganizationController().getAvailCompoIdsAtRoot(space.getShortId(), getUserId());
        for (String componentId : componentIds) {
          ComponentInst component = getOrganizationController().getComponentInst(componentId);
          if (component != null && !component.isHidden()) {
            project.addComponent(component);
          }
        }
        projects.add(project);
      }
    }
    return projects;
  }
  
  public String getLoginHomePage() {
    return getSettings("loginHomepage", URLManager.getApplicationURL()+"/sdis84/jsp/Main.jsp");
  }

  public Zoom getZoom() throws RemoteException {
    NodePK fatherPK = getZoomSource();
    Collection<PublicationDetail> publis =
        getPublicationBm().getDetailsByFatherPK(fatherPK, "P.pubUpdateDate desc", true);
    for (PublicationDetail publi : publis) {
      if (publi.getStatus().equalsIgnoreCase(PublicationDetail.VALID)) {
        String zoom = getPublicationWysiwygContent(publi);
        if (StringUtil.isDefined(zoom)) {
          return new Zoom(publi, zoom);
        }
      }
    }
    return null;
  }

  private NodePK getZoomSource() {
    return new NodePK(getSettings("zoom.topicId", "toBeDefined"), getSettings("zoom.componentId",
        "toBeDefined"));
  }

  private String getPublicationWysiwygContent(PublicationDetail publication) {
    return WysiwygController.load(publication.getPK().getInstanceId(), publication.getPK().getId(),
        getLanguage());
  }
  
  public boolean displaySearchOnHome() {
    return getSettings("home.search", true);
  }

  public List<Shortcut> getMainShortcuts() {
    return getShortcuts("home");
  }

  public List<City> getWeatherCities() {
    if (getSettings("home.weather", false) == false) {
      return null;
    }
    
    List<City> cities = new ArrayList<City>();
    
    String[] woeids = StringUtil.split(getSettings("home.weather.woeid", ""), ",");
    String[] labels = StringUtil.split(getSettings("home.weather.cities", ""), ",");
    
    for (int i=0; i<woeids.length; i++) {
      try {
        cities.add(new City(woeids[i], labels[i]));
      } catch (Exception e) {
        SilverTrace.error("lookSDIS84", "LookAuroraHelper.getWeatherCities", "ERROR", e);
      }
    }
    return cities;
  }
  
  public Heading getHeading(String spaceId) {
    if (!StringUtil.isDefined(spaceId)) {
      spaceId = getSubSpaceId();
    }
    SilverTrace.info("lookSDIS84", "LookSDIS84Helper.getHeading", "root.MSG_GEN_ENTER_METHOD",
        "spaceId = " + spaceId);
    SpaceInstLight space = getOrganizationController().getSpaceInstLightById(spaceId);
    String componentId = getComponentIdWebPage(spaceId);
    ComponentInst component = getOrganizationController().getComponentInst(componentId);
    Heading heading = new Heading(space, "");
    String[] profiles = getOrganisationController().getUserProfiles(getUserId(), componentId);
    heading.setAdmin(ArrayUtils.contains(profiles, SilverpeasRole.publisher.name()));
    heading.setBackOfficeAppId(componentId);
    if (component != null) {
      String param = component.getParameterValue("xmlTemplate");
      if (StringUtil.isDefined(param)) {
        DataRecord data = getDataRecord(componentId, param);
        if (data != null) {
          setHeadingEdito(heading, data, componentId);
          setHeadingImage(heading, data, componentId);
          setHeadingShortcuts(heading, data, componentId);
          setHeadingTitle(heading, data);
        }
      }
    }
    
    heading.setLastPublications(getLatestPublications(spaceId, 7));
    
    String[] almanachIds = getOrganisationController().getAllComponentIdsRecur(spaceId, getUserId(), "almanach", true, false);
    if (almanachIds != null && almanachIds.length > 0) {
      heading.setNextEventsDate(getNextEvents(false, almanachIds));
    }
    
    return heading;
  }
  
  private void setHeadingTitle(Heading heading, DataRecord data) {
    String fieldName = getSettings("headings.title.fieldname", "titre");
    // get description
    try {
      Field field = data.getField(fieldName);
      if (field != null) {
        heading.setTitle(field.getValue());
      }
    } catch (UtilException e) {
      SilverTrace.error("lookSDIS84", "LookSDIS84Helper.getProjectDescription", "root.MSG_ERROR",
          e);
    } catch (FormException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void setHeadingEdito(Heading heading, DataRecord data, String componentId) {
    String fieldName = getSettings("headings.about.fieldname", "edito");
    // get description
    try {
      heading.setEdito(WysiwygFCKFieldDisplayer.getContentFromFile(componentId, "0", fieldName));
    } catch (UtilException e) {
      SilverTrace.error("lookSDIS84", "LookSDIS84Helper.getProjectDescription", "root.MSG_ERROR",
          e);
    }
  }
  
  private void setHeadingImage(Heading heading, DataRecord data, String componentId) {
    // get image
    try {
      String fieldName = getSettings("headings.image.fieldname", "image");
      Field field = data.getField(fieldName);
      if (field != null) {
        if (field.getValue() != null) {
          heading.setImageURL(getImageURLFromField(field, componentId));
        }
      }
    } catch (FormException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private String getImageURLFromField(Field field, String componentId) {
    if (field != null) {
      String fieldValue = field.getValue();
      if (fieldValue != null) {
        String attachmentId =
            fieldValue.substring(fieldValue.indexOf("_") + 1, fieldValue.length());
        if (StringUtil.isDefined(attachmentId)) {
          if (attachmentId.startsWith("/")) {
            // case of an image provided by a gallery
            return attachmentId;
          } else {
            SimpleDocument attachment =
                AttachmentServiceFactory.getAttachmentService().searchDocumentById(
                    new SimpleDocumentPK(attachmentId, componentId), null);
            if (attachment != null) {
              return URLManager.getApplicationURL()+attachment.getAttachmentURL();
            }
          }
        }
      }
    }
    return "#";
  }
  
  private void setHeadingShortcuts(Heading heading, DataRecord data, String componentId) {
    String imageFieldName = getSettings("headings.shortcuts.image.fieldname", "shortcutImage");
    String urlFieldName = getSettings("headings.shortcuts.url.fieldname", "shortcutURL");
    String targetFieldName = getSettings("headings.shortcuts.target.fieldname", "shortcutTarget");
    String textFieldName = getSettings("headings.shortcuts.text.fieldname", "shortcutText");
    
    List<Shortcut> shortcuts = new ArrayList<Shortcut>();
    
    try {
      Field fieldURL = data.getField(urlFieldName+"1");
      for (int i=1; i<=6 && fieldURL != null && StringUtil.isDefined(fieldURL.getValue()); i++) {
        Field fieldImage = data.getField(imageFieldName+String.valueOf(i));
        Field fieldTarget = data.getField(targetFieldName+String.valueOf(i));
        Field fieldText = data.getField(textFieldName+String.valueOf(i));
        Shortcut shortcut =
            new Shortcut(getImageURLFromField(fieldImage, componentId), fieldTarget.getValue(),
                fieldURL.getValue(), fieldText.getValue());
        shortcuts.add(shortcut);
        fieldURL = data.getField(urlFieldName+String.valueOf(i+1));
      }
    } catch (FormException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    heading.setShortcuts(shortcuts);
  }

  /**
   * @param spaceId
   * @return
   */
  public String getComponentIdWebPage(String spaceId) {
    String[] asAvailCompoForCurUser =
        getOrganizationController().getAvailCompoIds(spaceId,
            getMainSessionController().getUserId());
    for (String componentId : asAvailCompoForCurUser) {
      ComponentInstLight componentInst =
          getOrganizationController().getComponentInstLight(componentId);

      if (getSettings("heading.about.ComponentDesc", "toBeDefined").equals(
          componentInst.getDescription())) {
        return componentId;
      }
    }
    return null;
  }

  public String getServiceDescription(String componentId, String param) {
    DataRecord data = getDataRecord(componentId, param);
    if (data != null) {
      String fieldName = getSettings("services.about.fieldName", "Missions");
      try {
        return WysiwygFCKFieldDisplayer.getContentFromFile(componentId, "0", fieldName);
      } catch (UtilException e) {
        SilverTrace.error("lookSDIS84", "LookSDIS84Helper.getServiceDescription",
            "root.MSG_ERROR", e);
      }
    }
    return "";
  }
    
  /**
   * @param numRub
   * @return
   */
  public List<PublicationDetail> getDernieresPublications(String numRub) {
    return getLatestPublications(getSettings("Rubrique" + numRub, "toBeDefined"), Integer
        .parseInt(getSettings("NbDernieresPublicationsRub", "6")));
  }

  /**
   * Dernières publications
   * @return
   */
  public List<PublicationDetail> getDernieresPublications() {
    return getLatestPublications(null, Integer
        .parseInt(getSettings("NbDernieresPublications", "3")));
  }

  /**
   * Dernières publications affichées au maximum
   * @return
   */
  public List<PublicationDetail> getDernieresPublicationsMax() {
    return getLatestPublications(null, Integer.parseInt(getSettings("NbDernieresPublicationsMax",
        "50")));
  }

  public List<ComponentInst> getPersonalComponents() {
    PersonalSpaceController psc = new PersonalSpaceController();
    SpaceInst space = psc.getPersonalSpace(getUserId());
    List<ComponentInst> components = null;
    if (space != null) {
      components = space.getAllComponentsInst();
    }
    List<WAComponent> allComponents = psc.getVisibleComponents(getOrganizationController());
    List<ComponentInst> result = new ArrayList<ComponentInst>();
    for (WAComponent oneComponent : allComponents) {
      ComponentInst componentUsed = getComponentInst(components, oneComponent);
      if (componentUsed == null) {
        componentUsed = new ComponentInst();
        componentUsed.setLabel(getComponentLabel(oneComponent.getName()));
        componentUsed.setName(oneComponent.getName());
      }
      result.add(componentUsed);
    }
    return result;
  }

  private ComponentInst getComponentInst(List<ComponentInst> components, WAComponent waComponent) {
    if (components != null) {
      for (ComponentInst component : components) {
        if (component.getName().equalsIgnoreCase(waComponent.getName())) {
          return component;
        }
      }
    }
    return null;
  }

  private String getComponentLabel(String componentName) {
    String label = getString("lookSilverpeasV5.personalSpace." + componentName);
    if (!StringUtil.isDefined(label)) {
      label = componentName;
    }
    return label;
  }

  /**
   * @return
   */
  public List<ComponentInst> getApplications() {
    List<ComponentInst> hyperLinks = new ArrayList<ComponentInst>();
    String[] asAvailCompoForCurUser =
        getOrganizationController().getAvailCompoIds(
            getSettings("applications.spaceId", "toBeDefined"),
            getMainSessionController().getUserId());
    for (String componentId : asAvailCompoForCurUser) {
      ComponentInst componentInst = getOrganizationController().getComponentInst(componentId);
      if ("hyperlink".equals(componentInst.getName())) {
        hyperLinks.add(componentInst);
      }
    }

    return hyperLinks;
  }

  public List<Shortcut> getHeadingShorcuts(String headingId) {
    return getShortcuts(headingId);
  }

  public OrganisationController getOrganizationController() {
    return super.getOrganisationController();
  }

  private PublicationTemplate getXMLTemplate(String componentId, String xmlTemplateName) {
    try {
      String shortName =
          xmlTemplateName.substring(xmlTemplateName.indexOf("/") + 1, xmlTemplateName.indexOf("."));
      PublicationTemplate pubTemplate =
          PublicationTemplateManager.getInstance().getPublicationTemplate(
              componentId + ":" + shortName);

      return pubTemplate;
    } catch (PublicationTemplateException e) {
      SilverTrace.error("lookSDIS84", "LookSDIS84Helper.getXMLTemplate()", "", e);
    }
    return null;
  }

  public DataRecord getDataRecord(String componentId, String xmlTemplateName) {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate(componentId, xmlTemplateName);
      if (pubTemplate != null) {

        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord("0", getLanguage());
        if (data == null) {
          data = recordSet.getEmptyRecord();
          data.setId("0");
          data.setLanguage(getLanguage());
        }

        return data;
      }
    } catch (Exception e) {
      SilverTrace.error("lookSDIS84", "LookSDIS84Helper.getDataRecord()", "", e);
    }
    return null;
  }
  
  @Override
  public List<PublicationDetail> getLatestPublications(String spaceId, int nbPublis) {
    List<PublicationDetail> publications = super.getLatestPublications(spaceId, nbPublis*2);
    List<PublicationDetail> result = new ArrayList<PublicationDetail>();
    for (PublicationDetail publication : publications) {
      if (!isComponentForNews(publication.getPK().getInstanceId())) {
        result.add(publication);
      }
      if (result.size() == nbPublis) {
        return result;
      }
    }
    return result;
  }
  
  private boolean isComponentForNews(String componentId) {
    return componentId.startsWith("quickinfo");
  }
  
  public List<News> getNews() {
    String newsType = getSettings("home.news", "");
    if (StringUtil.isDefined(newsType)) {
      if (newsType.equalsIgnoreCase("delegated")) {
        return getDelegatedNews();
      } else {
        return getNewsByComponentId(newsType);
      }
    }
    return null;
  }
  
  private List<News> getDelegatedNews() {
    List<News> news = new ArrayList<News>();
    List<DelegatedNews> delegatedNews = delegatedNewsService.getAllValidDelegatedNews();
    
    for (DelegatedNews delegated : delegatedNews) {
      News aNews = new News(delegated.getPublicationDetail());
      news.add(aNews);
    }

    return news;
  }
  
  private List<News> getNewsByComponentId(String appId) {    
    QuickInfoService service = QuickInfoServiceFactory.getQuickInfoService();
    return service.getVisibleNews(appId);
  }

  public List<Shortcut> getShortcuts(String id) {
    List<Shortcut> shortcuts = new ArrayList<Shortcut>();
    boolean end = false;
    int i = 1;
    while (!end) {
      String prefix = "Shortcut." + id + "." + i;
      String url = getSettings(prefix + ".Url", null);
      if (StringUtil.isDefined(url)) {
        String target = getSettings(prefix + ".Target", "toBeDefined");
        String altText = getSettings(prefix + ".AltText", "toBeDefined");
        String iconUrl = getSettings(prefix + ".IconUrl", "toBeDefined");
        Shortcut shortcut = new Shortcut(iconUrl, target, url, altText);
        shortcuts.add(shortcut);
        i++;
      } else {
        end = true;
      }
    }
    return shortcuts;
  }
  
  public String getCSSClassNames() {
    String currentSpaceId = getSubSpaceIdOrSpaceId();
  
    List<SpaceInst> spaces = getOrganizationController().getSpacePath(currentSpaceId);
    StringBuilder sb = new StringBuilder(10);
    for (SpaceInst spaceInst : spaces) {
      String spaceId = spaceInst.getId();
      if (!spaceId.startsWith("WA")) {
        spaceId = "WA" + spaceId;
      }
      sb.append(spaceId).append(" ");
    }
    return sb.toString();
  }
  
  private String getSubSpaceIdOrSpaceId() {
    String currentSpaceId = getSubSpaceId();
    if (!StringUtil.isDefined(currentSpaceId)) {
      currentSpaceId = getSpaceId();
    }
    return currentSpaceId;
  }
  
  public List<EventOccurrence> getTodayEvents() {   
    List<EventOccurrence> events =
        getAlmanachBm().getEventOccurrencesInPeriod(Period.from(new Date(), PeriodType.day, "fr"),
            getAlmanachIds());
    return events;
  }
  
  public List<NextEventsDate> getNextEvents() {
    return getNextEvents(true, getAlmanachIds());
  }
  
  private List<NextEventsDate> getNextEvents(boolean fetchOnlyImportant, String... almanachIds) {
    List<EventOccurrence> events = getAlmanachBm().getNextEventOccurrences(almanachIds);
    Date today = new Date();
    List<NextEventsDate> result = new ArrayList<NextEventsDate>();
    Date date = null;
    NextEventsDate nextEventsDate = null;
    for (EventOccurrence event : events) {
      if (!fetchOnlyImportant || (fetchOnlyImportant && event.isPriority())) {
        Date eventDate = event.getStartDate().asDate();
        if (DateUtil.compareTo(today, eventDate, true) != 0) {
          if (date == null || DateUtil.compareTo(date, eventDate, true) != 0) {
            nextEventsDate = new NextEventsDate(eventDate); 
            result.add(nextEventsDate);
            date = eventDate;
          }
          nextEventsDate.addEvent(event);
        }
      }
    }
    return result;
  }
  
  private AlmanachBm getAlmanachBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.ALMANACHBM_EJBHOME, AlmanachBm.class);
  }
  
  private String[] getAlmanachIds() {
    String[] almanachIds = {getSettings("home.events.appId", "")};
    return almanachIds;
  }
  
  private PublicationHelper getPublicationHelper() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    if (kmeliaTransversal == null) {
      Class<?> helperClass = Class.forName("com.stratelia.webactiv.kmelia.KmeliaTransversal");
      kmeliaTransversal = (PublicationHelper) helperClass.newInstance();
      kmeliaTransversal.setMainSessionController(this.getMainSessionController());
    }
    return kmeliaTransversal;
  }

  private List<PublicationDetail> getLastUpdatedPublicationsSince(String spaceId, int sinceNbDays,
      int nbPublis) {
    try {
      return getPublicationHelper().getUpdatedPublications(spaceId, sinceNbDays, nbPublis);
    } catch (ClassNotFoundException e) {
      SilverTrace.error("lookCG26", "LookCG26Helper.getLastUpdatedPublicationsSince", "", e);
    } catch (InstantiationException e) {
      SilverTrace.error("lookCG26", "LookCG26Helper.getLastUpdatedPublicationsSince", "", e);
    } catch (IllegalAccessException e) {
      SilverTrace.error("lookCG26", "LookCG26Helper.getLastUpdatedPublicationsSince", "", e);
    }
    return new ArrayList<PublicationDetail>();
  }

  public List<PublicationDetail> getLastUpdatedPublications(String spaceId) {
    return getLastUpdatedPublicationsSince(spaceId, Integer.parseInt(getSettings(
        "publications.page.since.days", "30")), 10000);
  }
  
  public List<LinkDetail> getBookmarks() {
    MyLinksBm myLinksBm = EJBUtilitaire.getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBm.class);
    List<LinkDetail> links = myLinksBm.getAllLinks(getUserId());
    List<LinkDetail> bookmarks = new ArrayList<LinkDetail>();
    for (LinkDetail link : links) {
      if (link.isVisible()) {
        bookmarks.add(link);
      }
    }
    return bookmarks;
  }
  
  public Question getAQuestion() {
    QuestionManager qm = QuestionManagerFactory.getQuestionManager();
    String appId = getSettings("home.faq.appId", "");
    if (StringUtil.isDefined(appId)) {
      try {
        List<Question> questions = (List<Question>) qm.getQuestions(appId);
        if (questions != null && !questions.isEmpty()) {
          if ("random".equalsIgnoreCase(getSettings("home.faq.display", "random")) &&
              questions.size() > 1) {
            Random random = new Random();
            int i = random.nextInt(questions.size()-1);
            return questions.get(i);
          } else {
            return questions.get(0);
          }
        }
      } catch (QuestionReplyException e) {
        SilverTrace.error("lookCG11", "LookCG11Helper.getDidYouKnow",
            "root.MSG_GEN_PARAM_VALUE", e);
      }
    }
    return null;
  }

}