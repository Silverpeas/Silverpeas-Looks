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
  .spaceHome #spaceQuiskInfo ul.carousel {
    list-style: none outside none;
    margin: 0px;
    padding: 0px;
  }

  .spaceHome #spaceQuiskInfo li.slide {
    list-style: none outside none;
    padding: 0px;
    min-height: 180px;
    margin: 0px 0px 5px;
  }

  .spaceHome #spaceQuiskInfo li.slide > h4 {
    margin: 0 0 8px 0;
    padding: 0px;
  }

  .spaceHome #spaceQuiskInfo li.slide img {
    float: left;
    margin: 0.25em 0.5em 0 0;
    width: 100px;
  }

  .spaceHome #spaceQuiskInfo li p {
    color: #666666;
    text-align: justify;
    padding: 0;
    margin: 0;
  }

  .spaceHome #spaceQuiskInfo .slides-pagination {
    bottom: auto;
    left: auto;
    position: relative;
    margin: 0px;
    padding: 0px;
    z-index: 90;
  }

  .spaceHome #spaceQuiskInfo .slides-pagination li {
    display: inline-block;
  }

  .spaceHome #spaceQuiskInfo .slides-pagination a {
    color: #FFFFFF;
    text-align: center;
    margin-bottom: 5px;
  }

  .spaceHome #spaceQuiskInfo .slideshow {
    margin: 0px;
  }
</style>