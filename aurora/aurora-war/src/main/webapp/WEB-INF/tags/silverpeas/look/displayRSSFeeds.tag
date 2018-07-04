<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="rssFeeds"
              required="true"
              type="org.silverpeas.looks.aurora.RSSFeeds" %>

<fmt:message var="labelRSS" key="look.home.rssFeeds"/>

<c:set var="rssDisplayMode" value="aggregate"/>
<c:if test="${rssFeeds.tabs}">
  <c:set var="rssDisplayMode" value="rss_tab" />
</c:if>
<c:if test="${rssFeeds.carrousel}">
  <c:set var="rssDisplayMode" value="rss_carrousel" />
</c:if>

<c:if test="${not empty rssFeeds}">
  <c:set var="rssChannels" value="${rssFeeds.channels}"/>
  <c:set var="onceChannel" value="${fn:length(rssChannels) == 1}"/>
  <c:set var="rssItems" value="${rssFeeds.aggregatedItems}"/>

  <div id="rssNews" class="feed-home secteur-container ${rssDisplayMode} onceChannel-${onceChannel}">
    <h4>${labelRSS}</h4>
    <c:choose>
      <c:when test="${rssFeeds.carrousel}">
        <ul id="rss-slider">
          <!-- Display each RSS items -->
          <c:forEach var="rssItem" items="${rssItems}">
            <li class="item-channel channel_${rssItem.channelId}">
              <h3 class="title-item-rssNews">
                <a href="${rssItem.itemLink}" target="_blank">${rssItem.itemTitle}</a>
              </h3>
            </li>
          </c:forEach>
        </ul>
      </c:when>
      <c:otherwise>
        <div class="rss_type">
          <c:forEach var="rssChannel" items="${rssChannels}">
            <a href="#" id="channel_${rssChannel.PK.id}" class="link" onclick="return false;"><span>${rssChannel.feed.title}</span></a>
          </c:forEach>
        </div>
        <ul>
          <!-- Display each RSS items -->
          <c:forEach var="rssItem" items="${rssItems}">
            <li class="item-channel channel_${rssItem.channelId} deploy-item">
              <h3 class="title-item-rssNews">
                <span class="channelName-rssNews"><span>${rssItem.channelTitle}</span></span>
                <a href="${rssItem.itemLink}" target="_blank">${rssItem.itemTitle}</a>
              </h3>
              <div class="lastUpdate-item-rssNews"><view:formatDateTime value="${rssItem.itemDate}"/></div>
              <div class="itemDeploy" style="display:none;">
                <div class="description-item-rssNews">${rssItem.itemDescription}</div>
                <br class="clear"/>
              </div>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>

  </div>

  <script type="text/javascript">
    $(document).ready(function() {

      <c:if test="${rssFeeds.carrousel}">
        $("#rss-slider").responsiveSlides({
          auto: true,
          pager: false,
          nav: false,
          speed: 500,
          pause: true,
          timeout: 6000
        });
      </c:if>

      $('.description-item-rssNews a').attr("target","_blank");

      $('.deploy-item').click(function() {
        $(this).children('.itemDeploy').toggle();
        $(this).toggleClass('open');
      });
      $('.aggregate .rss_type a').click(function() {
        var channel = $(this).attr('id');
        $('.'+channel).toggle();
        $(this).toggleClass('off');
      });

      $('.rss_tab .rss_type > a').click(function() {
        $('.rss_tab .selected').removeClass('selected');
        $(this).addClass('selected');
        var channel = $(this).attr('id');
        $('.item-channel').hide();
        $('.'+channel).show();
      });

      var firstFeed = $('.rss_tab .rss_type > a:first');
      firstFeed.addClass('selected');
      var firstFeedId = firstFeed.attr('id');
      $('.rss_tab .item-channel').hide();
      $('.rss_tab .'+firstFeedId).show();
    });
  </script>
</c:if>