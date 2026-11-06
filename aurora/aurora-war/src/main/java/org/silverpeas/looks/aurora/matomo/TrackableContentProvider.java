package org.silverpeas.looks.aurora.matomo;

import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Provider
class TrackableContentProvider {

  private static final Pattern QUICKINFO_INSTANCE_PATTERN =
      Pattern.compile("/Rquickinfo/([^/]+)/");

  @Inject
  private BlogService blogService;
  @Inject
  private QuickInfoService quickInfoService;
  @Inject
  private PublicationService publicationService;

  private TrackableContentProvider() {
  }

  public TrackableContent get(HttpServletRequest httpReq) {
    String path = httpReq.getRequestURI();
    TrackableContent content = new TrackableContent();
    content.setContent(false);
    if (path.contains("ViewPublication")) {
      setContentFromPublication(httpReq, content);
    } else if (path.contains("/Rquickinfo/") && path.contains("/View")) {
      setContentFromNews(httpReq, path, content);
    } else if (path.contains("/CalendarEvent/") && !path.contains("possibledurations")) {
      setContentFromCalendarEvent(content, path);
    } else if (path.contains("Rdirectory")) {
      setContentFromResourcePath(content, path, "Directory", "Users directory");
    } else if (path.contains("RjobManagerPeas/jsp/Main")) {
      setContentFromResourcePath(content, path, "Administration", "Admin console");
    } else if (path.contains("ViewPost")) {
      setContentFromPost(httpReq, path, content);
    }
    return content;
  }

  private void setContentFromPost(HttpServletRequest httpReq, String path, TrackableContent content) {
    String id = httpReq.getParameter("PostId");
    String blogId = "";
    Pattern pattern = Pattern.compile("Rblog/([^/]+)");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      blogId = matcher.group(1);
    }
    content.setContentType("Post");
    content.setContent(true);
    content.setContentId(id);
    Collection<PostDetail> posts = blogService.getAllPosts(blogId);
    for (PostDetail post : posts) {
      if (post.getId().equals(id)) {
        content.setContentName(post.getTitle());
        content.setPermalink(post.getPermalink());
        break;
      }
    }
  }

  private void setContentFromResourcePath(TrackableContent content, String path,
      String contentType, String contentName) {
    content.setContentType(contentType);
    content.setContent(true);
    content.setPermalink(path);
    content.setContentName(contentName);
    content.setContentId("");
  }

  private void setContentFromCalendarEvent(TrackableContent content, String path) {
    content.setContentType("Event");
    content.setContent(true);
    Pattern pattern = Pattern.compile("CalendarEvent/([a-f0-9\\-]+)");
    Matcher matcher = pattern.matcher(path);
    String id = "";
    if (matcher.find()) {
      id = matcher.group(1);
    }
    content.setContentId(id);
    content.setPermalink("/silverpeas/Contribution/" + id);
    content.setContentName(CalendarEvent.getById(id).getTitle());
  }

  private void setContentFromNews(HttpServletRequest httpReq, String path,
      TrackableContent content) {
    Matcher matcher = QUICKINFO_INSTANCE_PATTERN.matcher(path);
    if (!matcher.find()) {
      return;
    }
    String newsId = httpReq.getParameter("Id");
    News news = quickInfoService.getNews(new ResourceReference(newsId, matcher.group(1)));
    if (news != null) {
      content.setContentType("News");
      content.setContent(true);
      content.setContentId(newsId);
      content.setContentName(news.getTitle());
      content.setPermalink(news.getPermalink());
    }
  }

  private void setContentFromPublication(HttpServletRequest httpReq,
      TrackableContent content) {
    content.setContentType("Publication");
    content.setContent(true);
    content.setContentId(httpReq.getParameter("PubId"));
    PublicationDetail pub = publicationService.getDetail(new PublicationPK(content.getContentId()));
    content.setContentName(pub.getTitle());
    content.setPermalink(pub.getPermalink());
  }
}
