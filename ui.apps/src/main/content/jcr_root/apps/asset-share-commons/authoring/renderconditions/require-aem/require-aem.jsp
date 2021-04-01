<%--
  ==============================================================================
  Require AEM Rendercondition
  A condition toggles Granite components on and off based on what AEM distribution
  is being used.

  - A distribution of `cloud-ready` only renders the component on AEM as a Cloud Service.
  - A distribution of `classic` only renders the component on AEM 6.x

  /**
   * The AEM distribution on which to render the distribution
   */
  - distribution (String) = cloud-ready | classic


   Example node definition:

   <jcr:root ...
             xmlns:granite="http://www.adobe.com/jcr/granite/1.0">

   <granite:rendercondition
       jcr:primaryType="nt:unstructured"
       sling:resourceType="asset-share-commons/authoring/renderconditions/require-aem"
       distribution="cloud-ready"/>

  ==============================================================================
--%><%
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

    if (distribution == null) {
        vote = false;
    } else if (RequireAem.Distribution.CLOUD_READY.equals(requireAem.getDistribution())) {
        vote = RequireAem.Distribution.CLOUD_READY.getValue().equals(distribution);
    } else if (RequireAem.Distribution.CLASSIC.equals(requireAem.getDistribution())) {
        vote = RequireAem.Distribution.CLASSIC.getValue().equals(distribution);
    }

    request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(vote));
%>