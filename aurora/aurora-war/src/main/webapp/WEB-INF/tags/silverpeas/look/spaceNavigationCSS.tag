<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
  .spaceHome .spaceDescription {
    display: block;
    margin: 1em 0;
    padding: 0;
    text-align: justify;
  }

  .spaceHome .spaceNavigation .browseSpace {
    font-weight: 500;
  }

  .spaceHome .spaceNavigation ul {
    list-style-type: none;
    display: flex;
    flex-direction: row;
    flex-flow: wrap;
    margin: 0;
    padding: 0;
  }

  .spaceHome .spaceNavigation li {
    border-radius: 12px;
    line-height: 3em;
    margin-right: 1em;
    min-height: 3em;
    padding: 1em;
    display: flex;
  }

  .spaceHome .spaceNavigation li:hover {
    background: linear-gradient(90deg, rgb(0 0 0 / 0.3) 0%, rgb(255 255 255 / 1) 100%);
    cursor: pointer;
  }

  .spaceHome .spaceNavigation li div {
    align-self: center;
  }

  .spaceHome .spaceNavigation li a {
    font-size: 110%;
  }

  .spaceHome .spaceNavigation li p {
    font-size: 90%;
    margin: 0;
    padding: 0;
  }

  .spaceHome .spaceNavigation li.browse-component {
    padding-left: 36px;
    position: relative
  }

  .spaceHome .spaceNavigation li img {
    left: 5px;
    position: absolute;
    top: calc(50% - 13px);
    width: 26px;
    height: auto;
  }
</style>