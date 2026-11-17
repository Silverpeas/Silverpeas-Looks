/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.looks.aurora.homepage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomepageJspCompatibilityTest {
  @TempDir
  Path temporary;

  @Test
  void homepageScriptletCompilesWithTheLegacyJspSourceLevel() throws Exception {
    String jsp = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String imports = Pattern.compile("<%@ page import=\"([^\"]+)\" %>")
        .matcher(jsp).results().map(match -> "import " + match.group(1) + ";")
        .collect(Collectors.joining("\n"));
    String scriptlets = Pattern.compile("<%(?![@!=-])([\\s\\S]*?)%>")
        .matcher(jsp).results().map(match -> match.group(1))
        .collect(Collectors.joining("\n"));
    Path source = temporary.resolve("HomepageScriptlet.java");
    Files.writeString(source, imports + "\nclass HomepageScriptlet {\n" +
        "void render(jakarta.servlet.http.HttpSession session, " +
        "jakarta.servlet.http.HttpServletRequest request) throws Exception {\n" +
        scriptlets + "\n}\n}");
    var compiler = ToolProvider.getSystemJavaCompiler();
    var diagnostics = new DiagnosticCollector<JavaFileObject>();
    try (var files = compiler.getStandardFileManager(diagnostics, null, null)) {
      // Keep JDK APIs available, but enforce the source syntax used by the JSP compiler.
      var options = List.of("-source", "8", "-target", "8", "-proc:none",
          "-classpath", System.getProperty("java.class.path"), "-d", temporary.toString());
      boolean compiled = compiler.getTask(null, files, diagnostics, options, null,
          files.getJavaFileObjects(source)).call();
      assertTrue(compiled, () -> diagnostics.getDiagnostics().toString());
    }
  }
}
