package org.silverpeas.looks.aurora.servlets;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.looks.aurora.AuroraSpaceHomePage;
import org.silverpeas.looks.aurora.LookAuroraHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GoToSpaceHomepageBackOffice extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    try {
      HttpRequest request = HttpRequest.decorate(req);
      String spaceId = request.getParameter("SpaceId");
      LookAuroraHelper lookHelper =
          (LookAuroraHelper) LookHelper.getLookHelper(req.getSession(false));

      String destination = "admin/jsp/accessForbidden.jsp";
      if (lookHelper.isSpaceAdmin(spaceId)) {
        ComponentInstLight backOfficeApp = lookHelper.getConfigurationApp(spaceId);
        if (backOfficeApp == null) {
          createConfigurationApp(spaceId, lookHelper);
          backOfficeApp = lookHelper.getConfigurationApp(spaceId);
        }

        if (backOfficeApp != null) {
          // go to app
          destination =
              URLUtil.getApplicationURL() + URLUtil.getComponentInstanceURL(backOfficeApp.getId()) +
                  "Edit";
          res.sendRedirect(destination);
        } else {
          req.getRequestDispatcher("/admin/jsp/errorpageMain.jsp").forward(req, res);
        }
      } else {
        req.getRequestDispatcher(destination).forward(req, res);
      }
    } catch (ServletException | IOException e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String createConfigurationApp(String spaceId, LookAuroraHelper lookHelper) {
    String componentName = "webPages";
    Optional<WAComponent> existingComponent = WAComponent.getByName(componentName);
    if (existingComponent.isPresent()) {
      List<Parameter> parameters = existingComponent.get().getAllParameters();

      // set specific parameter values
      ComponentInst component = new ComponentInst();
      component.setCreatorUserId(lookHelper.getUserId());
      component.setInheritanceBlocked(false);
      component.setLabel("Page d'accueil");
      component.setName(componentName);
      component.setDomainFatherId(spaceId);
      component.setHidden(true);
      for (Parameter parameter : parameters) {
        if (parameter.getName().equals("useSubscription")) {
          parameter.setValue("no");
        } else if (parameter.getName().equals("xmlTemplate")) {
          parameter.setValue(AuroraSpaceHomePage.TEMPLATE_NAME);
        } else if (parameter.getName().equals("xmlTemplate2")) {
          // check if a custom template is defined for this space or a parent
          String template = lookHelper.getSpaceHomePageCustomTemplate(spaceId);
          if (StringUtil.isDefined(template)) {
            parameter.setValue(template);
          }
        }
      }
      component.setParameters(parameters);

      try {
        return Administration.get().addComponentInst(lookHelper.getUserId(), component);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

}
