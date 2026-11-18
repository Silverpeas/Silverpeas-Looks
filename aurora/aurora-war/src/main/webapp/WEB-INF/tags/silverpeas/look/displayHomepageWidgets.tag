<%--
 * Copyright (C) 2000 - 2026 Silverpeas
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
 --%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>
<%@ attribute name="publications" required="true" type="java.util.Map" %>
<%@ attribute name="news" required="true" type="java.util.Map" %>
<%@ attribute name="widgets" required="true" type="java.util.List" %>
<%@ attribute name="zone" required="true" type="java.lang.String" %>
<%@ attribute name="questions" type="java.util.Map" required="false" %>
<%@ attribute name="users" type="java.util.Map" required="false" %>
<%@ attribute name="subscriptions" type="java.util.Map" required="false" %>
<c:forEach items="${widgets}" var="widget">
  <c:if test="${widget.enabled and widget.zone == zone}">
    <c:choose>
      <c:when test="${widget.type == 'news'}">
        <viewTags:displayNews listOfNews="${news[widget.id]}" id="homepage-widget-${widget.id}" title="${widget.title}" showMoreNews="${widget.newsConfiguration.showMoreNews}" showAllNews="${widget.newsConfiguration.showAllNews}"/>
      </c:when>
      <c:when test="${widget.type == 'publications'}">
        <viewTags:displayPublications lookHelper="${sessionScope['Silverpeas_LookHelper']}"
            publications="${publications[widget.id]}" id="homepage-widget-${widget.id}"
            title="${widget.title}" showMore="${widget.publicationsConfiguration.showMore}"/>
      </c:when>
      <c:when test="${widget.type == 'bookmarks'}">
        <c:url value="/AuroraHomepageContent" var="bookmarksUrl">
          <c:param name="action" value="renderBookmarks"/>
          <c:param name="kind" value="skins"/>
          <c:param name="owner" value="${homepageOwner}"/>
          <c:param name="widget" value="${widget.id}"/>
          <c:param name="language" value="${homepageLanguage}"/>
        </c:url>
        <div id="homepage-content-${widget.id}" class="homepage-bookmarks">
          <iframe class="homepage-search-frame homepage-bookmarks-frame" title="<c:out value='${empty widget.title ? "Favoris personnels" : widget.title}'/>"
              src="<c:out value='${bookmarksUrl}'/>"></iframe>
        </div>
      </c:when>
      <c:when test="${widget.type == 'search'}">
        <c:url value="/AuroraHomepageContent" var="searchUrl">
          <c:param name="action" value="renderSearch"/>
          <c:param name="kind" value="skins"/>
          <c:param name="owner" value="${homepageOwner}"/>
          <c:param name="widget" value="${widget.id}"/>
          <c:param name="language" value="${homepageLanguage}"/>
        </c:url>
        <section id="homepage-content-${widget.id}" class="secteur-container homepage-search">
          <c:if test="${not empty widget.title}"><h4><c:out value="${widget.title}"/></h4></c:if>
          <iframe class="homepage-search-frame" title="<c:out value='${empty widget.title ? "Recherche" : widget.title}'/>"
              src="<c:out value='${searchUrl}'/>"></iframe>
        </section>
      </c:when>
      <c:when test="${widget.type == 'shortcuts'}">
        <viewTags:displayShortcuts shortcuts="${widget.shortcutsConfiguration.entries}"
            id="homepage-content-${widget.id}" title="${widget.title}"/>
      </c:when>
      <c:when test="${widget.type == 'subscriptions'}">
        <viewTags:displayNewsSubscribed enabled="${true}" listOfNews="${subscriptions[widget.id]}"
            id="homepage-content-${widget.id}" title="${widget.title}"
            showMore="${widget.subscriptionsConfiguration.showMore}"/>
      </c:when>
      <c:when test="${widget.type == 'faq'}">
        <viewTags:displayFAQ questions="${questions[widget.id]}" id="homepage-content-${widget.id}" title="${widget.title}"/>
      </c:when>
      <c:when test="${widget.type == 'users'}">
        <viewTags:displayNewUsers newUsers="${users[widget.id]}" id="homepage-content-${widget.id}" title="${widget.title}"/>
      </c:when>
      <c:when test="${widget.type == 'events' or widget.type == 'rss'}">
        <c:url value="/AuroraHomepageContent" var="feedUrl">
          <c:param name="action" value="renderFeed"/>
          <c:param name="kind" value="skins"/>
          <c:param name="owner" value="${homepageOwner}"/>
          <c:param name="widget" value="${widget.id}"/>
          <c:param name="language" value="${homepageLanguage}"/>
        </c:url>
        <div id="homepage-widget-${widget.id}" class="homepage-feed-part" data-feed-url="<c:out value='${feedUrl}'/>"></div>
      </c:when>
      <c:when test="${widget.type == 'ephemeris'}">
        <viewTags:displayEphemeris id="homepage-widget-${widget.id}" title="${widget.title}"/>
      </c:when>
      <c:otherwise>
        <section id="homepage-widget-${widget.id}" class="secteur-container">
          <c:if test="${not empty widget.title}"><h4><c:out value="${widget.title}"/></h4></c:if>
          <div class="rich-content">${widget.html}</div>
        </section>
      </c:otherwise>
    </c:choose>
  </c:if>
</c:forEach>
