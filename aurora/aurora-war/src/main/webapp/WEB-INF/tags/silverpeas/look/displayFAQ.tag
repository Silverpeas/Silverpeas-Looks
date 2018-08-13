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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="questions"
              required="true"
              type="org.silverpeas.looks.aurora.Questions" %>

<fmt:message var="labelQuestions" key="look.home.faq.title"/>
<fmt:message var="labelQuestionsPost" key="look.home.faq.post"/>
<fmt:message var="labelQuestionsMore" key="look.home.faq.more"/>

<c:if test="${not empty questions.list}">
  <div class="secteur-container faq" id="faq-home">
    <h4>${labelQuestions}</h4>
    <div class="FAQ-entry-main-container">
      <div class="FAQ-entry">
        <c:forEach var="question" items="${questions.list}">
          <c:url var="questionURL" value="${question._getPermalink()}"/>
          <p><a class="sp-permalink" href="${questionURL}">${question.title}</a></p>
        </c:forEach>
      </div>
      <c:if test="${questions.canAskAQuestion}">
        <c:url var="newQuestionURL" value="${questions.requestURL}"/>
        <a title="${labelQuestionsPost}" href="${newQuestionURL}" class="link-add sp-link"><span>${labelQuestionsPost}</span> </a>
      </c:if>
    </div>
    <a title="${labelQuestionsMore}" href="${questions.appURL}" class="link-more sp-link"><span>${labelQuestionsMore}</span> </a>
  </div>
</c:if>