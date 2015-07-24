package hclu.hreg.api

import hclu.hreg.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}