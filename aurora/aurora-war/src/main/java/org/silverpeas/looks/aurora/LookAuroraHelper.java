package org.silverpeas.looks.aurora;

import org.silverpeas.components.almanach.model.EventOccurrence;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.components.questionreply.service.QuestionManagerProvider;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.PersonalSpaceController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.DefaultMyLinksService;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.look.DefaultLayoutConfiguration;
import org.silverpeas.core.web.look.LookSilverpeasV5Helper;
import org.silverpeas.core.web.look.PublicationHelper;
import org.silverpeas.core.web.look.Shortcut;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class LookAuroraHelper extends LookSilverpeasV5Helper {

  private DelegatedNewsService delegatedNewsService = null;
  private PublicationHelper kmeliaTransversal = null;
  private LocalizationBundle messages;
  private LookSettings settings;
  
  public LookAuroraHelper(HttpSession session) {
    super(session);

    delegatedNewsService = DelegatedNewsService.get();

    messages = ResourceLocator
        .getLocalizationBundle("org.silverpeas.looks.aurora.multilang.lookBundle",
            getMainSessionController().getFavoriteLanguage());
  }

  public void initLayoutConfiguration() {
    super.initLayoutConfiguration();
    DefaultLayoutConfiguration layout = (DefaultLayoutConfiguration) super.getLayoutConfiguration();
    layout.setHeaderURL("/look/jsp/TopBar.jsp");
    layout.setBodyURL("/look/jsp/bodyPartAurora.jsp");
    layout.setBodyNavigationURL("/look/jsp/DomainsBar.jsp");

    settings = new LookSettings(getSettingsBundle());
  }

  public List<BannerMainItem> getBannerMainItems() {
    String param = getSettings("banner.spaces", null);
    if (param != null) {
      List<BannerMainItem> items = new ArrayList<BannerMainItem>();
      OrganizationController oc = getOrganisationController();
      String[] spaceIds = StringUtil.split(param);
      for (String spaceId : spaceIds) {
        if (oc.isSpaceAvailable(spaceId, getUserId())) {
          BannerMainItem item = new BannerMainItem(oc.getSpaceInstLightById(spaceId));
          String[] subspaceIds = oc.getAllSubSpaceIds(spaceId, getUserId());
          for (String subspaceId : subspaceIds) {
            item.addSubspace(oc.getSpaceInstLightById(subspaceId));
          }
          String[] appIds = oc.getAvailCompoIdsAtRoot(spaceId, getUserId());
          for (String appId : appIds) {
            ComponentInstLight app = oc.getComponentInstLight(appId);
            if (!app.isHidden()) {
              item.addApp(app);
            }
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
        String[] componentIds = getOrganizationController().getAvailCompoIdsAtRoot(space.getId(), getUserId());
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
    return getSettings("loginHomepage", URLUtil.getApplicationURL()+"/look/jsp/Main.jsp");
  }

  public List<Shortcut> getMainShortcuts() {
    return getShortcuts("home");
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

  /**
   * Dernières publications
   * @return
   */
  public List<PublicationDetail> getDernieresPublications() {
    String spaceId = getSettings("home.publications.spaceid", "");
    if (!StringUtil.isDefined(spaceId)) {
      spaceId = null;
    }
    return getLatestPublications(spaceId,
        Integer.parseInt(getSettings("home.publications.nb", "3")));
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

  public OrganizationController getOrganizationController() {
    return super.getOrganisationController();
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
      aNews.setPublicationId(delegated.getPublicationDetail().getId());
      news.add(aNews);
    }

    return news;
  }
  
  private List<News> getNewsByComponentId(String appId) {    
    QuickInfoService service = QuickInfoService.get();
    return service.getVisibleNews(appId);
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
  
  private AlmanachService getAlmanachBm() {
    return AlmanachService.get();
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
      SilverTrace.error("lookAurora", "LookAuroraHelper.getLastUpdatedPublicationsSince", "", e);
    } catch (InstantiationException e) {
      SilverTrace.error("lookAurora", "LookAuroraHelper.getLastUpdatedPublicationsSince", "", e);
    } catch (IllegalAccessException e) {
      SilverTrace.error("lookAurora", "LookAuroraHelper.getLastUpdatedPublicationsSince", "", e);
    }
    return new ArrayList<PublicationDetail>();
  }

  public List<PublicationDetail> getLastUpdatedPublications(String spaceId) {
    return getLastUpdatedPublicationsSince(spaceId, Integer.parseInt(getSettings(
        "publications.page.since.days", "30")), 10000);
  }
  
  public List<LinkDetail> getBookmarks() {
    MyLinksService myLinksService = new DefaultMyLinksService();
    List<LinkDetail> links = myLinksService.getAllLinks(getUserId());
    List<LinkDetail> bookmarks = new ArrayList<LinkDetail>();
    for (LinkDetail link : links) {
      if (link.isVisible()) {
        bookmarks.add(link);
      }
    }
    return bookmarks;
  }
  
  public Questions getQuestions() {
    Questions faqs = new Questions();
    QuestionManager qm = QuestionManagerProvider.getQuestionManager();
    String appId = getSettings("home.faq.appId", "");
    int nb = getSettings("home.faq.nb", 1);
    if (StringUtil.isDefined(appId)) {
      faqs.setAppId(appId);
      try {
        String[] profiles = getOrganisationController().getUserProfiles(getUserId(), appId);
        SilverpeasRole role = SilverpeasRole.getGreaterFrom(SilverpeasRole.from(profiles));
        faqs.setCanAskAQuestion(role.isGreaterThanOrEquals(SilverpeasRole.writer));
        List<Question> questions = (List<Question>) qm.getQuestions(appId);
        if (questions != null && !questions.isEmpty()) {
          if (nb > questions.size()) nb = questions.size();
          if ("random".equalsIgnoreCase(getSettings("home.faq.display", "random")) &&
              questions.size() > 1 && questions.size() > nb) {
            Random random = new Random();
            int j = 0;
            while (j < nb) {
              int i = random.nextInt(questions.size()-1);
              Question question = questions.get(i);
              boolean tryagain = false;
              for (Question q : faqs.getList()) {
                if (question.getPK().getId().equals(q.getPK().getId())) {
                  tryagain = true;
                  break;
                }
              }
              if (!tryagain) {
                faqs.add(question);
                j++;
              }
            }
          } else {
            for (int i = 0; i < nb; i++) {
              faqs.add(questions.get(i));
            }
          }
          return faqs;
        }
      } catch (QuestionReplyException e) {
        SilverTrace
            .error("lookAurora", "LookAuroraHelper.LookAuroraHelper", "root.MSG_GEN_PARAM_VALUE",
                e);
      }
    }
    return null;
  }

  public LocalizationBundle getLocalizedBundle() {
    return messages;
  }

  public LookSettings getLookSettings() {
    return settings;
  }
}