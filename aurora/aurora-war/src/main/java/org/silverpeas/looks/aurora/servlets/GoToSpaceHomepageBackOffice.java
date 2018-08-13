package org.silverpeas.looks.aurora.servlets;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
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

public class GoToSpaceHomepageBackOffice extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    HttpRequest request = HttpRequest.decorate(req);
    String spaceId = request.getParameter("SpaceId");
    LookAuroraHelper lookHelper = (LookAuroraHelper) LookHelper.getLookHelper(req.getSession(false));

    String destination = "admin/jsp/accessForbidden.jsp";
    if (lookHelper.isSpaceAdmin(spaceId)) {
      ComponentInstLight backOfficeApp = lookHelper.getConfigurationApp(spaceId);
      if (backOfficeApp == null) {
        createConfigurationApp(spaceId, lookHelper.getUserId());
        backOfficeApp = lookHelper.getConfigurationApp(spaceId);
      }

      if (backOfficeApp != null) {
        // go to app
        destination = URLUtil.getApplicationURL()+URLUtil.getComponentInstanceURL(backOfficeApp.getId()) + "Edit";
        res.sendRedirect(destination);
      } else {
        destination = "/admin/jsp/errorpageMain.jsp";
      }
    } else {
      req.getRequestDispatcher(destination).forward(req, res);
    }
	}

  private String createConfigurationApp(String spaceId, String userId) {
    String componentName = "webPages";
    ComponentInst component = new ComponentInst();
    component.setCreatorUserId(userId);
    component.setInheritanceBlocked(false);
    component.setLabel("Page d'accueil");
    component.setName(componentName);
    component.setDomainFatherId(spaceId);
    component.setHidden(true);

    WAComponent baseComponent = WAComponent.getByName(componentName).get();
    List<Parameter> parameters = baseComponent.getAllParameters();

    // set specific parameter values
    for (Parameter parameter : parameters) {
      if (parameter.getName().equals("useSubscription")) {
        parameter.setValue("no");
      } else if (parameter.getName().equals("xmlTemplate")) {
        parameter.setValue(AuroraSpaceHomePage.TEMPLATE_NAME);
      }
    }
    component.setParameters(parameters);

    try {
      return Administration.get().addComponentInst(userId, component);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return null;
  }

}