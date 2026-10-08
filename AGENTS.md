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

## Build

To build the project, use the devcontainer whenever possible. Otherwise, if a container
from the `silverpeas/silverdev:latest` Docker image is available on the host, starts it (if not
already done) and uses it.

Java 17 (`maven.compiler.release` inherited from the `org.silverpeas:silverpeas-project` parent POM), Maven.

```bash
mvn clean install                 # full build of both modules
mvn clean install -PskipMinify    # skip JS/CSS minification — much faster for local iteration
mvn clean install -pl aurora/aurora-war -am   # build a single module and its prerequisites
```

There is **no test source tree** in this repository (no `src/test` nor `src/integration-test`); `mvn test` runs
nothing. Validation happens through deployment into a Silverpeas instance and through SonarCloud in CI.

The parent POM is periodically bumped (see git history); dependencies resolve from the Silverpeas Nexus repository
declared in the root `pom.xml`.

A devcontainer based on `silverpeas/silverdev` is provided in `.devcontainer/`; it bind-mounts the host `~/.m2`,
`~/.ssh` and `~/.gitconfig`.

CI (`Jenkinsfile`) waits for any running build of `core` and `components` of the same version, then runs
`mvn clean install -Pdeployment -Dcontext=ci` against a WildFly started for the occasion. PR titles must start with
`Bug #<n>`, `Feature #<n>` or `Support #<n>` (or `[something]`) — the CI derives the snapshot version from it, and
commit messages follow the same convention.

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

## Conventions

- Jakarta EE 10 namespaces throughout (`jakarta.*`, `jakarta.tags.core` taglib URIs, `web-app` 6.0).
- Logging goes through `SilverLogger.getLogger(this)`; the look declares its own logger namespace in
  `properties/org/silverpeas/util/logging/auroraLogging.properties`.
- Every source file carries the Silverpeas AGPL v3 + FLOSS-exception header (see `license.txt` / `exceptions.txt`);
  copy it into new files, with the current year as upper bound.
