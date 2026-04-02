package org.silverpeas.looks.aurora.matomo;

import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackableContentProvider {
    public static TrackableContent get(HttpServletRequest httpReq) {
        String path = httpReq.getRequestURI();
        TrackableContent content = new TrackableContent();
        content.setContent(false);
        if (path.contains("ViewPublication")) {
            content.setContentType("Publication");
            content.setContent(true);
            content.setContentId(httpReq.getParameter("PubId"));
            PublicationDetail pub = PublicationService.get().getDetail(new PublicationPK(content.getContentId()));
            content.setContentName(pub.getTitle());
            content.setPermalink(pub.getPermalink());
        } else if (path.contains("/Rquickinfo/") && path.contains("/View")) {
            content.setContentType("News");
            content.setContent(true);
            content.setContentId(httpReq.getParameter("Id"));
            News n = QuickInfoService.get().getNews(content.getContentId());
            content.setContentName(n.getTitle());
            content.setPermalink(n.getPermalink());
        } else if (path.contains("/CalendarEvent/") && !path.contains("possibledurations")) {
            content.setContentType("Event");
            content.setContent(true);
            Pattern pattern = Pattern.compile("CalendarEvent/([a-f0-9\\-]+)");
            Matcher matcher = pattern.matcher(path);
            String id = "";
            if (matcher.find()) {
                id =  matcher.group(1);
            }
            content.setContentId(id);
            content.setPermalink("/silverpeas/Contribution/" + id);
            content.setContentName(CalendarEvent.getById(id).getTitle());
        } else if (path.contains("Rdirectory")) {
            content.setContentType("Directory");
            content.setContent(true);
            content.setPermalink(path);
            content.setContentName("Users directory");
            content.setContentId("");
        } else if (path.contains("RjobManagerPeas/jsp/Main")) {
            content.setContentType("Administration");
            content.setContent(true);
            content.setPermalink(path);
            content.setContentName("Admin console");
            content.setContentId("");
        } else if (path.contains("ViewPost")) {
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
            Collection<PostDetail> posts = BlogService.get().getAllPosts(blogId);
            for (PostDetail post : posts) {
                if (post.getId().equals(id)) {
                    content.setContentName(post.getTitle());
                    content.setPermalink(post.getPermalink());
                    break;
                }
            }
        }
        return content;
    }
}
