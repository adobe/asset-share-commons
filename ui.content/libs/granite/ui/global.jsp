<%@page session="false"
          pageEncoding="utf-8"
          contentType="text/html"
          import="org.apache.sling.api.resource.Resource,
                  org.apache.sling.api.resource.ValueMap,
                  com.adobe.granite.ui.components.ComponentHelper,
                  com.adobe.granite.xss.XSSAPI,
                  com.day.cq.i18n.I18n" %><%
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling" %><%
%><%@taglib prefix="ui" uri="http://www.adobe.com/taglibs/granite/ui/1.0" %><%
%><sling:defineObjects  /><%

final ComponentHelper cmp = new ComponentHelper(pageContext);
final I18n i18n = cmp.getI18n();
final XSSAPI xssAPI = cmp.getXss();

%><%!
/**
 * A shortcut for <code>xssAPI.encodeForHTML(i18n.getVar(text))</code>.
 */
private final String outVar(XSSAPI xssAPI, I18n i18n, String text) {
    return xssAPI.encodeForHTML(i18n.getVar(text));
}

/**
 * A shortcut for <code>xssAPI.encodeForHTMLAttr(i18n.getVar(text))</code>.
 */
private final String outAttrVar(XSSAPI xssAPI, I18n i18n, String text) {
    return xssAPI.encodeForHTMLAttr(i18n.getVar(text));
}
%>