package org.silverpeas.looks.aurora.matomo;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;

import javax.servlet.http.HttpServletRequest;

public class TrackableContentProvider {
    public static TrackableContent get(HttpServletRequest httpReq) {
        String path = httpReq.getRequestURI();
        TrackableContent content = new TrackableContent();
        content.setContent(false);
        //TODO : extract content with ApplicationServiceProvider
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
            content.setPermalink(path);
        }

        return content;
    }
}
