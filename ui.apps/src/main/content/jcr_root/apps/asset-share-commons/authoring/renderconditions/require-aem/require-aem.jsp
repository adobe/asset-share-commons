<%
%><%@include file="/libs/granite/ui/global.jsp" %><%
%><%@page session="false"
          import="com.adobe.granite.ui.components.Config,
                  com.adobe.granite.ui.components.rendercondition.RenderCondition,
                  com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition,
                  com.adobe.aem.commons.assetshare.util.RequireAem" %>
<%
    RequireAem requireAem = sling.getService(RequireAem.class);

    Config cfg = cmp.getConfig();
    String distribution = cfg.get("distribution", String.class);

    boolean vote = false;

    if (distributionStr == null) {
        vote = false;
    } else {
        vote = RequireAem.Distribution.CLOUD_READY.equals(new RequireAem.Distribution(distribution));
    }

    request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(vote));
%>