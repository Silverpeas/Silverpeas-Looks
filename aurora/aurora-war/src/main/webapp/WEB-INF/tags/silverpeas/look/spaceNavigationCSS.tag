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

<style type="text/css">
  .spaceHome .spaceDescription {
    display: block;
    margin: 1em 0px;
    padding: 0px;
    text-align: justify;
  }

  .spaceHome .spaceNavigation .browseSpace {
    font-weight: 100;
  }

  .spaceHome .spaceNavigation ul {
    list-style-type: none;
    margin: 0px;
    padding: 0px;
  }

  .spaceHome .spaceNavigation li {
    border-radius: 12px;
    float: left;
    line-height: 32px;
    margin-right: 6px;
    min-height: 32px;
    padding: 8px 12px 8px 8px;
  }

  .spaceHome .spaceNavigation li:hover {
    background-image: url("<c:url value="/util/icons/gradientSVG.jsp?from=e5e5e5&to=fff&vertical=0&horizontal=100"/>");
    cursor: pointer;
  }

  .spaceHome .spaceNavigation li div {
    display: inline-block;
    vertical-align: middle;
  }

  .spaceHome .spaceNavigation li a {
    font-size: 14px;
  }

  .spaceHome .spaceNavigation li p {
    font-size: 90%;
    margin: -13px 0 0 0;
    padding: 0px;
  }

  .spaceHome .spaceNavigation li.browse-component {
    padding-left: 36px;
    position: relative;
  }

  .spaceHome .spaceNavigation li img {
    left: 5px;
    position: absolute;
    top: 25%;
    width: 26px;
  }
</style>