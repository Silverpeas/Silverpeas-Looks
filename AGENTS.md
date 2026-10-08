# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, Cursor, Gemini CLI, Copilot…)
when working with code in this repository. Claude Code loads it through `CLAUDE.md`, which only
imports this file.

## What this project is

Silverpeas Looks provides the *looks* (graphical themes / UX layouts) of the [Silverpeas](https://www.silverpeas.org)
collaborative platform. A look is not a standalone application: it is a set of JSP pages, JSP tag files, CSS/JS
assets and configuration files that Silverpeas Core loads at runtime to render the user's workspace (banner, menu,
home page, space home pages).

Currently a single look is delivered: **Aurora** (`aurora/`).

The project depends on Silverpeas Core and on several Silverpeas Components (quickinfo, delegatednews, almanach,
questionreply, rssaggregator, gallery, blog, webpages) — all `provided`, since they are supplied by the Silverpeas
runtime (WildFly). Their version is bound to `${silverpeas.version}` in the root `pom.xml`, which defaults to the
project's own version.

## Common rules

These rules are shared, word for word, by the `AGENTS.md` of Silverpeas-Core, Silverpeas-Components
and Silverpeas-Looks. Change them in the three repositories at once.

### Toolchain & build environment

- The build inherits almost everything (Java release, dependency versions, surefire/failsafe wiring,
  integration-test source dirs, profiles) from the external parent POM
  `org.silverpeas:silverpeas-project`, not from this repository. Read it (in `~/.m2`) when a build
  behaviour is not explained by the POMs in this repository.
- Java 21 (`maven.compiler.release` of the parent POM) and Maven 3.9.x. The platform is Jakarta EE 10
  (`jakarta.*` namespaces everywhere) deployed on WildFly.
- Build and test in the devcontainer (`.devcontainer/`, built on the `silverpeas/silverdev:latest`
  image) whenever possible. Otherwise, if a container of that image is available on the host, start
  it if needed and run the Maven commands inside it. The image provides Java, Maven, a WildFly under
  `/opt/wildfly-for-tests/` with a `wildfly start|stop|status` helper, and the native tools some
  tests need (ffmpeg, imagemagick, ghostscript, libreoffice, swftools, pdf2json). Don't expect the
  tests to run in a bare checkout.
- Profiles and switches from the parent POM: `-DskipTests`, `-PskipMinify` (skips the JS/CSS
  minification, much faster when iterating on web assets), `-Pcoverage` (JaCoCo), `-Pdeployment`
  (attaches sources and javadoc jars), `-Plicense` (rewrites the license header of every source file).

### Tests come with any code change

Any code that is modified or added has to be covered by unit or integration tests, written
preferably **before** the code itself: either to guard the modified code against regressions, or to
validate the new code and to help to design it (its call must be simple; any new code follows the
clean code principles). This is true even for a module or a repository without any test yet: set up
its test resources instead of skipping the tests. Code that is hard to test is a design signal: fix
the design rather than giving up the test.

**Unit tests** (surefire, `src/test/`, `**/*Test.java`): JUnit 5 with
`@EnableSilverTestEnv(context = JEETestContext.class)`.
- A bean under test declared with `@TestedBean` gets its `@Inject` dependencies resolved from the
  test bean container, the missing ones being automatically mocked; declare with `@TestManagedMock`
  only the collaborators to stub.
- A module without tests yet needs `silverpeas-core-test` as a test dependency and the
  `src/test/resources/META-INF/services/org.silverpeas.kernel.BeanContainer` file (plus
  `org.silverpeas.kernel.util.SystemWrapper` when system properties are read, and
  `org/silverpeas/util/stringtemplate.properties` when templates are involved); otherwise the CDI
  bean container is loaded instead of the test one.
- To check the user notifications asked by a service without rendering any template, send them
  through `UserNotificationHelper.buildAndSend(...)`, capture the builders with
  `mockStatic(UserNotificationHelper.class)` and put the test in the package of the builders so
  that their protected properties are reachable.
- The parent POM forces the `fr`/`FR` locale and the `Europe/Paris` timezone: date and number
  assertions are locale-sensitive.

**Integration tests** (failsafe, `src/integration-test/`, `**/*IT.java`): JUnit **4** with Arquillian.
- They run only with the `integration-test` profile, activated by `-Dcontext=ci`, against an
  **already running** WildFly started with `standalone-full.xml` (Arquillian uses the
  `wildfly-remote` container). The full CI command is
  `mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci`.
- Each test deploys a purpose-built WAR assembled by a `WarBuilder*` class that declares exactly
  which classes and resources go into the archive. Any type in the signature of a managed bean
  (fields, parameters, returned and thrown types) has to be embedded: otherwise Weld silently
  ignores the bean instead of failing the deployment.
- Inside an integration test, beans are looked up with `ServiceProvider.getService(...)`, not
  injected.

### Dependency injection

Silverpeas deliberately wraps the CDI/Jakarta-EE container behind its own annotations so the IoC
implementation could be swapped without touching business code. **Prefer these over raw CDI
annotations** when writing beans (they are defined in `org.silverpeas.core.annotation`):

- `@Service` — a transactional, `@ApplicationScoped` business service (a CDI stereotype).
- `@Repository` — a persistence/data-access bean.
- `@Provider`, `@Bean`, `@WebService` — other managed-bean stereotypes.

Managed beans get their collaborators via injection points. **Unmanaged objects** (e.g. entities
loaded from a datasource, JSP-side code) cannot inject, so they obtain services through
`org.silverpeas.core.util.ServiceProvider` (`ServiceProvider.getService(Type.class)` /
`getService("name")`), a thin delegator over the kernel's `ManagedBeanProvider`. For generic
(parameterized) service types, `ServiceProvider` won't resolve them — use
`jakarta.enterprise.inject.Instance` in a managed bean instead.

**In a managed bean, never get another managed bean through `ServiceProvider`**, neither directly
nor through a static accessor delegating to it (`PdcManager.get()`, `OrganizationController.get()`,
…): `ServiceProvider` is first intended for the objects that aren't managed by CDI, and a
programmatic lookup costs more than an injection. When a dependency has to be resolved lazily
(it is used only in some cases, or it isn't always deployed), inject it with
`jakarta.enterprise.inject.Instance<T>` and call `get()` where it is needed, as
`ICalendarEventSynchronization` does with its `Scheduler` in Silverpeas Core.

Beans needing startup logic implement `org.silverpeas.core.initialization.Initialization`.

### Code conventions

- Follow the clean code principles. A constructor that would take more than four parameters is
  replaced by a builder.
- Every source file carries the AGPL v3 + Silverpeas FLOSS-exception header (`license.txt` and
  `exceptions.txt` at the repository root); copy it into new files with the current year as upper
  bound, or run `mvn generate-sources -Plicense`.
- Logging goes through `SilverLogger.getLogger(this)`; each module declares its own logger
  namespace in `properties/org/silverpeas/util/logging/<name>Logging.properties`.
- Javadoc must satisfy the Java 21 doclint.
- LF line endings for all text and source files (enforced by `.gitattributes`).

### Git, CI & versioning

- Commit messages reference the Redmine tracker: `Feature #<n> ...`, `Fix bug #<n> ...`,
  `Fix vulnerability #<n> ...`. PR titles must start with `Bug #<n>`, `Feature #<n>`, `Support #<n>`
  or `[<label>]`: the CI derives the snapshot version from it.
- CI is Jenkins (`Jenkinsfile`) in the `silverpeas/silverbuild` image. It rewrites the project
  version (`versions:set`) and the parent-POM version per branch/PR before building, then runs a
  SonarCloud quality gate on PRs. Don't hand-edit versions to match the CI behaviour.

## Build

```bash
mvn clean install                 # full build of both modules
mvn clean install -PskipMinify    # skip JS/CSS minification
mvn clean install -pl aurora/aurora-war -am   # build a single module and its prerequisites
```

There is **no test source tree yet** in this repository (neither `src/test` nor `src/integration-test`), so
`mvn test` currently runs nothing. The common rule on tests applies all the same: the first change to Java code
sets up `src/test` of the module (test dependency on `silverpeas-core-test`, `META-INF/services` files, see above).
Deployment into a Silverpeas instance and SonarCloud in CI complete the tests, they don't replace them.

The parent POM is periodically bumped (see git history); dependencies resolve from the Silverpeas Nexus repository
declared in the root `pom.xml`.

The devcontainer bind-mounts the host `~/.m2`, `~/.ssh` and `~/.gitconfig`.

CI (`Jenkinsfile`) waits for any running build of `core` and `components` of the same version before building.

## Module layout

- **`aurora/aurora-configuration`** (jar) — packages everything that must be *installed into the Silverpeas
  configuration directories* rather than into the web app. Its resources root is `src/main/config`:
  - `properties/…/viewGenerator/settings/lookSettings.properties` — registers the available looks. `Initial` names
    the default one; the other entries (`Sobre`, `Prima`, `Waves`) are alternative skins users can pick.
  - `properties/…/viewGenerator/settings/Aurora.properties` (and `Sobre`/`prima`/`waves`) — **the settings bundle of
    a look**. Every `getSettings("some.key", default)` call in the Java code and every `LookSettings` getter reads
    from here. This file is the main extension point of the look.
  - `properties/…/looks/aurora/multilang/lookBundle*.properties` — i18n (fr, en, de, ru).
  - `properties/…/weather/settings/weather.properties`, `…/statistics/settings/matomo.properties`.
  - `data/web/weblib.war/**` — skin CSS and images served from `/weblib/…`.
  - `data/templateRepository/**` — XML publication templates, notably `auroraspacehomepage` (see below).
- **`aurora/aurora-war`** (war) — the look itself: Java helpers, JSPs, tag files, look-specific CSS/JS.

Both modules run `silverpeas-ui-compressor-maven-plugin:compress`, which minifies JS and CSS at build time
(`*.min.*` / `*-min-*` files are excluded).

## Architecture of the Aurora look

### The look helper is the entry point

`LookAuroraHelper` extends Silverpeas Core's `LookSilverpeasV5Helper` and is instantiated per HTTP session (stored
under the session attribute `Silverpeas_LookHelper`). It is the single façade the JSPs talk to:

```jsp
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>
```

`initLayoutConfiguration()` is what wires the look into Core's layout: it declares `TopBar.jsp` as the header,
`bodyPartAurora.jsp` as the body and `DomainsBar.jsp` as the navigation frame.

Nearly every behaviour of the helper is driven by a key of the look's settings bundle (`home.news`,
`banner.spaces`, `home.events.appId`, `space.homepage.*`, …). When adding a feature, the convention is: read it via
`getSettings(key, default)` (or add a typed getter to `LookSettings`), document the key in `Aurora.properties`, and
consume it from a JSP/tag.

The helper aggregates content from the Silverpeas components: news (`quickinfo` + `delegatednews`), events
(`almanach` + personal `userCalendar`), FAQ (`questionreply`), RSS (`rssaggregator`), media (`gallery`), free zones
(`webPages` WYSIWYG content), publications, bookmarks (`mylinks`) and directory/users.

### Rendering: JSP + tag files

`aurora-war/src/main/webapp/look/jsp/` holds the pages (`Main.jsp` — the platform home page, `spaceHomePage.jsp`,
`TopBar.jsp`, `DomainsBar.jsp`, `bodyPartAurora.jsp`, list pages). Reusable fragments live as JSP **tag files** in
`WEB-INF/tags/silverpeas/look/` (`displayNews.tag`, `displayNextEvents.tag`, `spaceNavigation.tag`, …) and are used
with the `viewTags` prefix. Pages combine them with Silverpeas Core's own `view:` / `silfn:` taglibs.

Presentation model classes (`NewsList`, `AuroraNews`, `Space`, `App`, `NextEvents`, `FreeZone`, `Questions`,
`NewUsersList`, `Project`, …) are thin view-model wrappers built by the helper and consumed via EL from the JSPs.
Two enums drive placement and rendering: `AuroraSpaceHomePageZone` (`MAIN` / `RIGHT` / `THIRD`) and
`NewsList.RenderingType` (carousel vs list).

### Space home pages are configurable per space

`AuroraSpaceHomePage` renders a space's home page. Its content comes from **either** the look settings
(`space.homepage.*` keys, the fallback) **or**, when the space contains a `webPages` application whose `xmlTemplate`
parameter is `auroraspacehomepage.xml`, from the data record of that XML publication template. `isEnabled(lookKey,
templateField)` encodes exactly that precedence: the template wins as soon as it exists.

That template lives in `aurora-configuration/src/main/config/data/templateRepository/auroraspacehomepage/`. Adding a
configurable item to a space home page means touching `data.xml` / `update.html` / `view.xml` there *and* reading
the new field in `AuroraSpaceHomePage`. A second template (`xmlTemplate2` parameter of the same app) can inject a
free custom form rendered by `getCustomFormContent()`.

Space admins reach the back-office through the `GoToSpaceHomepageBackOffice` servlet
(`/AuroraSpaceHomepageBackoffice?SpaceId=…`), enabled by `space.homepage.management.active`. A per-space override of
the whole rendering JSP is possible via `space.homepage.management.customtemplate.<spaceId>`, resolved by walking up
the space path.

### Weather widget

`service/weather/` implements a small pluggable weather client: `WeatherServiceRequester` with three
implementations (`OpenWeatherMapRequester`, `AccuWeatherRequester`, `YahooWeatherRequester`) selected by
`weather.service` in `weather.properties`, results cached by `WeatherCache` (`weather.cache.timeToLive`). The
browser calls the `WeatherServiceAdapter` servlet at `/RWeatherService/*`, which proxies the third-party API (their
keys must never leak to the client). Matching JS lives in `look/jsp/js/silverpeas-weather*.js`.

### Matomo tracking

`matomo/MatomoInjectionFilter` is mapped on `/*` and, when `matomo.enable` is true, wraps the response, builds the
tracking script from the StringTemplate `resources/StringTemplates/core/statistics/matomo.st` and injects it into
the HTML. It only processes `bodyPartAurora.jsp` and pages that `TrackableContentProvider` recognises as content.
