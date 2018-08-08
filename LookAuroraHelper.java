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
import org.silverpeas.components.quickinfo.service.QuickInfoDateComparatorDesc;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.DefaultMyLinksService;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.DefaultLayoutConfiguration;
import org.silverpeas.core.web.look.LookSilverpeasV5Helper;
import org.silverpeas.core.web.look.PublicationHelper;
import org.silverpeas.core.web.look.Shortcut;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
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
      List<BannerMainItem> items = new ArrayList<>();
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
  
  public List<Project> getProjects() {
    List<Project> projects = new ArrayList<Project>();
    String[] spaceIds = getOrganisationController().getAllowedSubSpaceIds(getUserId(), getProjectsSpaceId());
    for (String spaceId : spaceIds) {
      SpaceInstLight space = getOrganisationController().getSpaceInstLightById(spaceId);
      if (space != null) {
        Project project = new Project(space);
        String[] componentIds = getOrganisationController().getAvailCompoIdsAtRoot(space.getId(), getUserId());
        for (String componentId : componentIds) {
          ComponentInst component = getOrganisationController().getComponentInst(componentId);
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
    if (!getSettings("home.weather", false)) {
      return Collections.emptyList();
    }
    
    List<City> cities = new ArrayList<City>();
    
    String[] woeids = StringUtil.split(getSettings("home.weather.woeid", ""), ",");
    String[] labels = StringUtil.split(getSettings("home.weather.cities", ""), ",");
    
    for (int i=0; i<woeids.length; i++) {
      try {
        cities.add(new City(woeids[i], labels[i]));
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
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
        getOrganisationController().getAvailCompoIds(
            getSettings("applications.spaceId", "toBeDefined"),
            getMainSessionController().getUserId());
    for (String componentId : asAvailCompoForCurUser) {
      ComponentInst componentInst = getOrganisationController().getComponentInst(componentId);
      if ("hyperlink".equals(componentInst.getName())) {
        hyperLinks.add(componentInst);
      }
    }

    return hyperLinks;
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
  
  public NewsList getNews() {
    String newsType = getSettings("home.news", "");
    List<News> news = new ArrayList<>();
    String uniqueAppId = "";
    if (StringUtil.isDefined(newsType)) {
      if ("delegated".equalsIgnoreCase(newsType)) {
        news = getDelegatedNews();
      } else {
        String[] allowedComponentIds =
            getAllowedComponents(StringUtil.split(newsType, ' ')).toArray(new String[0]);
        if (allowedComponentIds.length == 1) {
          uniqueAppId = allowedComponentIds[0];
        }
        news = getNewsByComponentIds(allowedComponentIds);
      }
      int nbNews = getSettings("home.news.size", -1);
      if (nbNews != -1 && news.size() > nbNews) {
        return new NewsList(news.subList(0, nbNews), uniqueAppId);
      }
    }
    return new NewsList(news, uniqueAppId);
  }

  private List<News> getDelegatedNews() {
    List<News> news = new ArrayList<News>();
    List<DelegatedNews> delegatedNews = delegatedNewsService.getAllValidDelegatedNews();
    for (DelegatedNews delegated : delegatedNews) {
      if (delegated != null && isComponentAvailable(delegated.getInstanceId())) {
        News aNews = new News(delegated.getPublicationDetail());
        if (delegated.getPublicationDetail() != null) {
          aNews.setPublicationId(delegated.getPublicationDetail().getId());
          aNews.setComponentInstanceId(delegated.getPublicationDetail().getComponentInstanceId());
          news.add(aNews);
        }
      }
    }
    return news;
  }
  
  private List<News> getNewsByComponentIds(String[] allowedComponentIds) {
    if (allowedComponentIds.length == 0) {
      return Collections.emptyList();
    }

    // getting news from allowed components
    QuickInfoService service = QuickInfoService.get();
    List<News> allNews = new ArrayList<>();
    for (String appId : allowedComponentIds) {
      allNews.addAll(service.getVisibleNews(appId));
    }

    // sorting news
    Collections.sort(allNews, QuickInfoDateComparatorDesc.comparator);

    return allNews;
  }

  private String[] getAllowedComponentIds(String param) {
    String[] appIds = StringUtil.split(getSettings(param, ""), ' ');

    return getAllowedComponents(appIds).toArray(new String[0]);
  }

  public NextEvents getNextEvents() {
    String[] allowedComponentIds = getAllowedComponentIds("home.events.appId");

    List<NextEventsDate> result = new ArrayList<NextEventsDate>();

    if (allowedComponentIds.length > 0) {
      List<EventOccurrence> events = getAlmanachBm().getNextEventOccurrences(allowedComponentIds);
      Date today = new Date();
      Date date = null;
      NextEventsDate nextEventsDate = null;
      int nbDays = getSettings("home.events.maxDays", 2);
      boolean fetchOnlyImportant = getSettings("home.events.importantOnly", true);
      for (EventOccurrence event : events) {
        if (!fetchOnlyImportant || (fetchOnlyImportant && event.isPriority())) {
          Date eventDate = event.getStartDate().asDate();
          if (DateUtil.compareTo(today, eventDate, true) != 0) {
            if (date == null || DateUtil.compareTo(date, eventDate, true) != 0) {
              nextEventsDate = new NextEventsDate(eventDate);
              if (result.size() == nbDays) {
                break;
              }
              result.add(nextEventsDate);
              date = eventDate;
            }
            nextEventsDate.addEvent(event.getEventDetail());
          }
        }
      }
    }
    NextEvents nextEvents = new NextEvents(result);
    if (allowedComponentIds.length == 1) {
      nextEvents.setUniqueAppId(allowedComponentIds[0]);
    }
    return nextEvents;
  }

  private List<String> getAllowedComponents(String... componentIds) {
    List<String> allowedComponentIds = new ArrayList<>();
    for (String componentId : componentIds) {
      if (isComponentAvailable(componentId)) {
        allowedComponentIds.add(componentId);
      }
    }
    return allowedComponentIds;
  }
  
  private AlmanachService getAlmanachBm() {
    return AlmanachService.get();
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return Collections.emptyList();
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
    if (StringUtil.isDefined(appId) && isComponentAvailable(appId)) {
      faqs.setAppId(appId);
      try {
        String[] profiles = getOrganisationController().getUserProfiles(getUserId(), appId);
        SilverpeasRole role = SilverpeasRole.getHighestFrom(SilverpeasRole.from(profiles));
        faqs.setCanAskAQuestion(role.isGreaterThanOrEquals(SilverpeasRole.writer));
        List<Question> questions = (List<Question>) qm.getQuestions(appId);
        if (CollectionUtil.isNotEmpty(questions)) {
          if (nb > questions.size()) {
            nb = questions.size();
          }
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
        SilverLogger.getLogger(this).error(e);
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

  private boolean isComponentAvailable(String componentId) {
    return getOrganisationController().isComponentAvailable(componentId, getUserId());
  }

  public BodyPartSettings getBodyPartSettings(HttpServletRequest request) {
    BodyPartSettings bodyPartSettings = new BodyPartSettings();

    HttpSession session = request.getSession();
    String strGoToNew = (String) session.getAttribute("gotoNew");
    String spaceId = request.getParameter("SpaceId");
    String subSpaceId = request.getParameter("SubSpaceId");
    String fromTopBar = request.getParameter("FromTopBar");
    String componentId = request.getParameter("ComponentId");
    String login = request.getParameter("Login");
    String fromMySpace 	= request.getParameter("FromMySpace");

    if (StringUtil.isDefined(strGoToNew) || StringUtil.isDefined(spaceId) ||
        StringUtil.isDefined(subSpaceId) || StringUtil.isDefined(componentId)) {
      // ignore login page when try to access a direct resource
      login = null;
    }

    //Allow to force a page only on login and when user clicks on logo
    boolean displayLoginHomepage = false;
    String loginHomepage = getSettings("loginHomepage", "");
    if (StringUtil.isDefined(loginHomepage) && StringUtil.isDefined(login) &&
        !StringUtil.isDefined(spaceId) && !StringUtil.isDefined(subSpaceId) &&
        !StringUtil.isDefined(componentId) && !StringUtil.isDefined(strGoToNew)) {
      displayLoginHomepage = true;

      if (!isUserCanDisplayMainHomePage()) {
        spaceId = getBannerMainItems().get(0).getSpace().getId();
        displayLoginHomepage = false;
        login = null;
      }
    }

    StringBuilder paramsForDomainsBar = new StringBuilder().append("{");
    if ("1".equals(fromTopBar)) {
      if (spaceId != null) {
        paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("', privateSubDomain:'")
            .append(subSpaceId).append("', FromTopBar:'1'");
      }
    } else if (componentId != null) {
      paramsForDomainsBar.append("privateDomain:'', component_id:'").append(componentId).append("'");
    } else {
      paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("'");
    }
    if ("1".equals(fromMySpace)) {
      paramsForDomainsBar.append(",FromMySpace:'1'");
    }
    paramsForDomainsBar.append('}');

    String webContext = URLUtil.getApplicationURL();
    String frameURL = "";
    if (displayLoginHomepage) {
      frameURL = loginHomepage;
      bodyPartSettings.setHideMenu(true);
    } else if (strGoToNew == null) {
      if (StringUtil.isDefined(componentId)) {
        frameURL = webContext + URLUtil.getURL(null, componentId)+"Main";
      } else {
        String homePage = getSettings("defaultHomepage", "/dt");
        String param = "";
        if (StringUtil.isDefined(spaceId)) {
          param = "?SpaceId=" + spaceId;
        }
        frameURL = webContext+homePage+param;
      }
    } else {
      frameURL = webContext+strGoToNew;
      if(strGoToNew.startsWith(webContext)) {
        frameURL = strGoToNew;
      }
    }

    session.removeAttribute("goto");
    session.removeAttribute("gotoNew");
    session.removeAttribute("RedirectToComponentId");
    session.removeAttribute("RedirectToSpaceId");

    boolean hideMenu = "1".equals(fromTopBar) || "1".equals(login);
    if (hideMenu) {
      bodyPartSettings.setHideMenu(true);
    }

    bodyPartSettings.setDomainsBarParams(paramsForDomainsBar.toString());
    bodyPartSettings.setMainPartURL(frameURL);
    return bodyPartSettings;
  }

  public boolean isUserCanDisplayMainHomePage() {
    if (getSettings("home.displayedWhenNoNews", true) || getUserDetail().isAccessAdmin()) {
      return true;
    }
    NewsList news = getNews();
    return !news.isEmpty();
  }
}