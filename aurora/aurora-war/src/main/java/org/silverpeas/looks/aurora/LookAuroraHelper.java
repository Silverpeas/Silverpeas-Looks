package org.silverpeas.looks.aurora;

import org.silverpeas.components.almanach.AlmanachSettings;
import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.components.questionreply.service.QuestionManagerProvider;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.components.quickinfo.service.QuickInfoDateComparatorDesc;
import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.service.RSSService;
import org.silverpeas.components.rssaggregator.service.RSSServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.DefaultMyLinksService;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.DefaultLayoutConfiguration;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.look.LookSilverpeasV5Helper;
import org.silverpeas.core.web.look.Shortcut;
import org.silverpeas.core.webapi.calendar.CalendarResourceURIs;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;
import org.silverpeas.looks.aurora.service.almanach.CalendarEventOccurrenceEntity;
import org.silverpeas.looks.aurora.service.weather.City;
import org.silverpeas.looks.aurora.service.weather.WeatherSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.silverpeas.looks.aurora.AuroraSpaceHomePage.TEMPLATE_NAME;

public class LookAuroraHelper extends LookSilverpeasV5Helper {

  private static final String DEFAULT_VALUE = "toBeDefined";
  private static final Random RANDOM = new Random();
  private static final String BANNER_ALL_SPACES = "*";
  private DelegatedNewsService delegatedNewsService;
  private LocalizationBundle messages;
  private LookSettings settings;
  private List<Domain> directoryDomains = null;
  private List<Group> directoryGroups = null;

  // Main search form session objects
  private PublicationTemplate mainSearchTemplate = null;
  private boolean mainSearchTemplateLoaded = false;

  public static LookHelper newLookHelper(HttpSession session) {
    LookHelper lookHelper = new LookAuroraHelper(session);
    session.setAttribute(LookHelper.SESSION_ATT, lookHelper);
    return lookHelper;
  }

  private LookAuroraHelper(HttpSession session) {
    super(session);

    delegatedNewsService = DelegatedNewsService.get();

    String language = getMainSessionController().getFavoriteLanguage();

    messages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.looks.aurora.multilang.lookBundle",
            language);

    final WeatherSettings weatherSettings = WeatherSettings.get();
    final String basePath = "org.silverpeas.weather.settings.";
    weatherSettings.setSettingsFilePath(
        basePath + getSettings("home.weather.settings", basePath + "weather.properties"));
  }

  @Override
  public void initLayoutConfiguration() {
    super.initLayoutConfiguration();
    DefaultLayoutConfiguration layout = (DefaultLayoutConfiguration) super.getLayoutConfiguration();
    layout.setHeaderURL("/look/jsp/TopBar.jsp");
    layout.setBodyURL("/look/jsp/bodyPartAurora.jsp");
    layout.setBodyNavigationURL("/look/jsp/DomainsBar.jsp");

    settings = new LookSettings(getSettingsBundle());
  }

  public List<BannerMainItem> getBannerMainItems() {
    String param = getSettings("banner.spaces", "*");
    if (StringUtil.isDefined(param)) {
      List<String> definedSpaceIds = new ArrayList<>();
      if (param.contains("*")) {
        definedSpaceIds = getExplicitlyDefinedSpaceIds();
      }
      String[] spaceIds = StringUtil.split(param);
      return getBannerMainItemOfSpaces(spaceIds, definedSpaceIds);
    }
    return new ArrayList<>();
  }

  private List<BannerMainItem> getBannerMainItemOfSpaces(final String[] spaceIds,
      final List<String> definedSpaceIds) {
    List<BannerMainItem> items = new ArrayList<>();
    OrganizationController oc = getOrganisationController();
    for (String spaceId : spaceIds) {
      if (BANNER_ALL_SPACES.equals(spaceId)) {
        List<String> allRootSpaceIds =
            new ArrayList<>(Arrays.asList(oc.getAllRootSpaceIds(getUserId())));
        allRootSpaceIds.removeAll(definedSpaceIds);
        for (String rootSpaceId : allRootSpaceIds) {
          items.add(getBannerMainItem(rootSpaceId));
        }
      } else {
        if (oc.isSpaceAvailable(spaceId, getUserId())) {
          items.add(getBannerMainItem(spaceId));
        }
      }
    }
    return items;
  }

  private BannerMainItem getBannerMainItem(String spaceId) {
    OrganizationController oc = getOrganisationController();
    return new BannerMainItem(oc.getSpaceInstLightById(spaceId));
  }

  private List<String> getExplicitlyDefinedSpaceIds() {
    List<String> spaceIds = new ArrayList<>();
    spaceIds.add(getProjectsSpaceId());
    spaceIds.add(getSettings("applications.spaceId", ""));
    String bannerSpaceIds = getSettings("banner.spaces", "*");
    for (String bannerSpaceId : StringUtil.split(bannerSpaceIds)) {
      if (!BANNER_ALL_SPACES.equals(bannerSpaceId)) {
        spaceIds.add(bannerSpaceId);
      }
    }
    return spaceIds;
  }

  public String getProjectsSpaceId() {
    return getSettings("projects.spaceId", "0");
  }

  public List<Project> getProjects() {
    return getProjects(false);
  }

  public List<Project> getProjects(boolean includeApps) {
    List<Project> projects = new ArrayList<>();
    OrganizationController controller = getOrganisationController();
    String[] spaceIds = controller.getAllowedSubSpaceIds(getUserId(), getProjectsSpaceId());
    for (String spaceId : spaceIds) {
      SpaceInstLight space = controller.getSpaceInstLightById(spaceId);
      if (space != null) {
        Project project = new Project(space);
        if (includeApps) {
          project.setComponents(getComponents(space.getId()));
        }
        projects.add(project);
      }
    }
    return projects;
  }

  private List<SilverpeasComponentInstance> getComponents(String spaceId) {
    List<SilverpeasComponentInstance> components = new ArrayList<>();
    OrganizationController controller = getOrganisationController();
    String[] componentIds = controller.getAvailCompoIdsAtRoot(spaceId, getUserId());
    for (String componentId : componentIds) {
      SilverpeasComponentInstance component = controller.getComponentInstLight(componentId);
      if (component != null && !component.isHidden()) {
        components.add(component);
      }
    }
    return components;
  }

  public String getLoginHomePage() {
    return getSettings("loginHomepage", URLUtil.getApplicationURL()+"/look/jsp/Main.jsp");
  }

  public List<Shortcut> getMainShortcuts() {
    return getShortcuts("home");
  }

  public List<Shortcut> getShortcuts(String id) {
    List<Shortcut> shortcuts = new ArrayList<>();
    boolean end = false;
    int i = 1;
    while (!end) {
      String prefix = "Shortcut." + id + "." + i;
      String url = getSettings(prefix + ".Url", null);
      if (StringUtil.isDefined(url)) {
        String target = getSettings(prefix + ".Target", DEFAULT_VALUE);
        String altText = getSettings(prefix + ".AltText", DEFAULT_VALUE);
        String iconUrl = getSettings(prefix + ".IconUrl", DEFAULT_VALUE);
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

    return WeatherSettings.get().getCities();
  }

  public List<PublicationDetail> getLatestPublications() {
    String spaceId = getSettings("home.publications.spaceid", "");
    if (!StringUtil.isDefined(spaceId)) {
      spaceId = null;
    }
    return getLatestPublications(spaceId,
        Integer.parseInt(getSettings("home.publications.nb", "3")));
  }

  public List<ComponentInst> getApplications() {
    List<ComponentInst> hyperLinks = new ArrayList<>();
    String[] asAvailCompoForCurUser = getOrganisationController().getAvailCompoIds(
        getSettings("applications.spaceId", DEFAULT_VALUE), getMainSessionController().getUserId());
    for (String componentId : asAvailCompoForCurUser) {
      ComponentInst componentInst = getOrganisationController().getComponentInst(componentId);
      if ("hyperlink".equals(componentInst.getName()) && !componentInst.isHidden()) {
        hyperLinks.add(componentInst);
      }
    }

    return hyperLinks;
  }

  @Override
  public List<PublicationDetail> getLatestPublications(String spaceId, int nbPublis) {
    String[] excludedComponentIds =
        StringUtil.split(getSettings("home.publications.components.excluded", ""));
    List<PublicationDetail> publications =
        super.getLatestPublications(spaceId, Arrays.asList(excludedComponentIds), nbPublis * 2);
    List<PublicationDetail> result = new ArrayList<>();
    for (PublicationDetail publication : publications) {
      String componentId = publication.getPK().getInstanceId();
      boolean excluded = ArrayUtil.contains(excludedComponentIds, componentId);
      if (!isComponentForNews(componentId) && !excluded) {
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
        List<String> allowedComponentIds =
            getAllowedComponents("quickinfo", StringUtil.split(newsType, ' '));
        if (allowedComponentIds.size() == 1) {
          uniqueAppId = allowedComponentIds.get(0);
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
    List<News> news = new ArrayList<>();
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

  private List<News> getNewsByComponentIds(List<String> allowedComponentIds) {
    if (CollectionUtil.isEmpty(allowedComponentIds)) {
      return Collections.emptyList();
    }

    // getting news from allowed components
    List<News> allNews = new ArrayList<>();
    for (String appId : allowedComponentIds) {
      allNews.addAll(getNewsByComponentId(appId));
    }

    // sorting news
    Collections.sort(allNews, QuickInfoDateComparatorDesc.comparator);

    return allNews;
  }

  private List<News> getNewsByComponentId(String allowedComponentId) {
    // getting news from allowed component
    boolean importantOnly = getSettings("home.news.importantOnly", false);
    List<News> allNews = QuickInfoService.get().getVisibleNews(allowedComponentId);
    if (!importantOnly) {
      return allNews;
    }
    List<News> importantNews = new ArrayList<>();
    for (News news : allNews) {
      if (news.isImportant()) {
        importantNews.add(news);
      }
    }
    return importantNews;
  }

  public NextEvents getNextEvents() {
    final String[] appIds = StringUtil.split(getSettings("home.events.appId", ""), ' ');
    final List<String> allowedComponentIds = getAllowedComponents("almanach", appIds);
    PersonalComponent.getByName("userCalendar").ifPresent(c -> allowedComponentIds
        .add(PersonalComponentInstance.from(User.getCurrentRequester(), c).getId()));
    return getNextEvents(allowedComponentIds);
  }

  private NextEvents getNextEvents(List<String> allowedComponentIds) {
    boolean includeToday = getSettings("home.events.today.include", true);
    int nbDays = getSettings("home.events.maxDays", 2);
    boolean fetchOnlyImportant = getSettings("home.events.importantOnly", true);
    return getNextEvents(allowedComponentIds, includeToday, nbDays, fetchOnlyImportant);
  }

  protected NextEvents getNextEvents(List<String> allowedComponentIds, boolean includeToday,
      int nbDays, boolean onlyImportant) {
    List<NextEventsDate> result = new ArrayList<>();
    if (!allowedComponentIds.isEmpty()) {
      final CalendarResourceURIs uri = CalendarResourceURIs.get();
      List<CalendarEventOccurrenceEntity> events =
          CalendarWebManager.get(allowedComponentIds.get(0))
              .getNextEventOccurrences(allowedComponentIds, emptySet(), emptySet(), emptySet(),
                  AlmanachSettings.getZoneId(),
                  AlmanachSettings.getNbOccurrenceLimitOfShortNextEventView())
          .map(o -> {
            final CalendarEventOccurrenceEntity entity = new CalendarEventOccurrenceEntity(o);
            entity.withOccurrencePermalinkUrl(uri.ofOccurrencePermalink(o));
            return entity;
          })
          .collect(Collectors.toList());

      result = filterNextEvents(events, nbDays, includeToday, onlyImportant);
    }
    NextEvents nextEvents = new NextEvents(result);
    if (allowedComponentIds.size() == 1) {
      nextEvents.setUniqueAppId(allowedComponentIds.get(0));
    }
    return nextEvents;
  }

  private List<NextEventsDate> filterNextEvents(final List<CalendarEventOccurrenceEntity> events,
      final int nbDays, final boolean includeToday, final boolean onlyImportant) {
    Date date = null;
    Date today = new Date();
    NextEventsDate nextEventsDate = null;
    List<NextEventsDate> filteredEventDates = new ArrayList<>();
    for (CalendarEventOccurrenceEntity event : events) {
      Date eventDate = event.getStartDateAsDate();
      if ((!onlyImportant || Priority.HIGH == event.getPriority()) &&
          (includeToday || DateUtil.compareTo(today, eventDate, true) != 0)) {
        if (date == null || DateUtil.compareTo(date, eventDate, true) != 0) {
          nextEventsDate = new NextEventsDate(eventDate);
          if (filteredEventDates.size() == nbDays) {
            break;
          }
          filteredEventDates.add(nextEventsDate);
          date = eventDate;
        }
        nextEventsDate.addEvent(event);
      }
    }
    return filteredEventDates;
  }

  private List<String> getAllowedComponents(String componentName, String... componentIds) {
    if (ArrayUtil.contains(componentIds, "*")) {
      return Arrays
          .asList(getOrganisationController().getComponentIdsForUser(getUserId(), componentName));
    }
    List<String> allowedComponentIds = new ArrayList<>();
    for (String componentId : componentIds) {
      if (isComponentAvailable(componentId)) {
        allowedComponentIds.add(componentId);
      }
    }
    return allowedComponentIds;
  }

  public List<PublicationDetail> getLastUpdatedPublicationsSince(String spaceId, int sinceNbDays,
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
    List<LinkDetail> bookmarks = new ArrayList<>();
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
    if (StringUtil.isDefined(appId) && isComponentAvailable(appId)) {
      faqs.setAppId(appId);
      try {
        String[] profiles = getOrganisationController().getUserProfiles(getUserId(), appId);
        SilverpeasRole role = SilverpeasRole.getHighestFrom(SilverpeasRole.from(profiles));
        if (role != null) {
          faqs.setCanAskAQuestion(role.isGreaterThanOrEquals(SilverpeasRole.writer));
        }
        List<Question> questions = qm.getQuestions(appId);
        if (CollectionUtil.isNotEmpty(questions)) {
          return filterFAQsMatchingQuestions(faqs, questions);
        }
      } catch (QuestionReplyException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  private Questions filterFAQsMatchingQuestions(final Questions faqs,
      final List<Question> questions) {
    int nb = getSettings("home.faq.nb", 1);
    if (nb > questions.size()) {
      nb = questions.size();
    }
    if ("random".equalsIgnoreCase(getSettings("home.faq.display", "random")) &&
        questions.size() > 1 && questions.size() > nb) {
      Predicate<Question> shouldTryAgain =
          q -> faqs.getList().stream().anyMatch(f -> q.getPK().getId().equals(f.getPK().getId()));
      int j = 0;
      while (j < nb) {
        int i = RANDOM.nextInt(questions.size() - 1);
        Question question = questions.get(i);
        boolean tryAgain = shouldTryAgain.test(question);
        if (!tryAgain) {
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
    buildDomainsBar(paramsForDomainsBar, spaceId, subSpaceId, fromTopBar, componentId, fromMySpace);

    String frameURL = getFrameURL(bodyPartSettings, strGoToNew, spaceId, componentId,
        displayLoginHomepage, loginHomepage);

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

  private String getFrameURL(final BodyPartSettings bodyPartSettings,
      final String strGoToNew, final String spaceId, final String componentId,
      final boolean displayLoginHomepage, final String loginHomepage) {
    String webContext = URLUtil.getApplicationURL();
    String frameURL;
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
    return frameURL;
  }

  private void buildDomainsBar(final StringBuilder paramsForDomainsBar, final String spaceId,
      final String subSpaceId, final String fromTopBar, final String componentId,
      final String fromMySpace) {
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
  }

  public boolean isUserCanDisplayMainHomePage() {
    if (getSettings("home.displayedWhenNoNews", true) || getUserDetail().isAccessAdmin()) {
      return true;
    }
    NewsList news = getNews();
    return !news.isEmpty();
  }

  public List<Domain> getDirectoryDomains() {
    if (directoryDomains == null) {
      directoryDomains = Arrays.stream(getSettings("directory.domains", "").split(","))
          .filter(StringUtil::isDefined)
          .map(i -> getOrganisationController().getDomain(i.trim()))
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    }
    return directoryDomains;
  }

  public List<Group> getDirectoryGroups() {
    if (directoryGroups == null) {
      directoryGroups = Arrays.stream(getSettings("directory.groups", "").split(","))
          .filter(StringUtil::isDefined)
          .map(groupId -> Group.getById(groupId.trim()))
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    }
    return directoryGroups;
  }

  public String getDirectoryURL() {
    if (!getSettings("directory.enabled", true)) {
      return null;
    }
    String url = "/Rdirectory/jsp/Main";
    url += "?DomainIds="+getDirectoryDomains().stream().map(Domain::getId).collect(Collectors.joining(","));
    url += "&GroupIds="+getDirectoryGroups().stream().map(Group::getId).collect(Collectors.joining(","));
    url += "&Sort="+getSettings("directory.sort", "ALPHA");
    return url;
  }

  public RSSFeeds getRSSFeeds() {
    String componentId = getSettings("home.rss.appId", "");
    if (StringUtil.isDefined(componentId)) {
      RSSService rssService = RSSServiceProvider.getRSSService();
      try {
        List<RSSItem> allItems = rssService.getApplicationItems(componentId, true);
        List<SPChannel> channels = rssService.getAllChannels(componentId);
        RSSFeeds rssFeeds = new RSSFeeds(channels, allItems);
        rssFeeds.setDisplayer(getSettings("home.rss.displayer", "aggregate"));
        return rssFeeds;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  public FreeZone getFreeZone() {
    String componentId = getSettings("home.freezone.appId", "");
    if (StringUtil.isDefined(componentId)) {
      WysiwygContent content = WysiwygController.get(componentId, componentId, getLanguage());
      if (content != null) {
        FreeZone freeZone = new FreeZone(content.getData());
        if (getSettings("home.freezone.app.useLabel", true)) {
          ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
          if (component != null) {
            freeZone.setTitle(component.getLabel(getLanguage()));
          }
        } else {
          freeZone.setTitle(getString("look.home.freezone"));
        }
        return freeZone;
      }
    }
    return null;
  }

  public AuroraSpaceHomePage getHomePage(String spaceId) {
    setSpaceIdAndSubSpaceId(spaceId);
    String currentSpaceId = getSubSpaceId();

    // get main information of space
    SpaceInstLight space = getOrganisationController().getSpaceInstLightById(currentSpaceId);
    return new AuroraSpaceHomePage(this, space);
  }

  public ComponentInstLight getConfigurationApp(String spaceId) {
    OrganizationController oc = OrganizationController.get();
    List<ComponentInstLight> apps = getAppsByName(spaceId, "webPages");
    for (ComponentInstLight app : apps) {
      String xmlFormName = oc.getComponentParameterValue(app.getId(), "xmlTemplate");
      if (StringUtil.isDefined(xmlFormName) && xmlFormName.equals(TEMPLATE_NAME)) {
        return app;
      }
    }
    return null;
  }

  private List<ComponentInstLight> getAppsByName(String spaceId, String name) {
    List<ComponentInstLight> apps = new ArrayList();
    OrganizationController oc = OrganizationController.get();
    String[] appsIds = oc.getAvailCompoIdsAtRoot(spaceId, getUserId());
    for (String appId : appsIds) {
      ComponentInstLight app = oc.getComponentInstLight(appId);
      if (app.getName().equals(name)) {
        apps.add(app);
      }
    }
    return apps;
  }

  public boolean isSpaceAdmin(String spaceId) {
    if (getUserDetail().isAccessAdmin()) {
      return true;
    }
    return getSpaceAdmins(spaceId).contains(getUserDetail());
  }

  public Form getMainSearchForm() {
    PublicationTemplate template = getMainSearchTemplate();
    if (template != null) {
      try {
        Form form = template.getSearchForm();
        form.setData(template.getRecordSet().getEmptyRecord());
        return form;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  private PublicationTemplate getMainSearchTemplate() {
    if (mainSearchTemplate == null && !mainSearchTemplateLoaded) {
      String templateFilename = getSettings("home.search.template", "");
      if (StringUtil.isDefined(templateFilename)) {
        PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
        try {
          mainSearchTemplate = templateManager.loadPublicationTemplate(templateFilename);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
      mainSearchTemplateLoaded = true;
    }
    return mainSearchTemplate;
  }
}