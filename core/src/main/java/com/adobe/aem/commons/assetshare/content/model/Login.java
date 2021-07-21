package com.adobe.aem.commons.assetshare.content.model;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class})
public class Login {

    @Inject
    @Via("resource")
    @Optional
    private String loginTitle;

    @Inject
    @Via("resource")
    @Optional
    private String loginLabel;

    @Inject
    @Via("resource")
    @Optional
    private String passwordLabel;

    @Inject
    @Via("resource")
    @Optional
    private String loginButtonLabel;

    @Inject
    @Via("resource")
    @Optional
    private String invalidLoginLabel;

    @Inject
    @Via("resource")
    @Optional
    private String sessionTimedOutLabel;

    @Self
    protected SlingHttpServletRequest request;

    @Inject
    private Page currentPage;

    private String reason;

    @PostConstruct
    public void init() {
        String jReason = request.getParameter("j_reason");
        if (StringUtils.isNotEmpty(jReason)) {
            this.reason = jReason;
        }
    }

    public String getLoginTitle() {
        return loginTitle;
    }

    public String getLoginLabel() {
        return loginLabel;
    }

    public String getPasswordLabel() {
        return passwordLabel;
    }

    public String getLoginButtonLabel() {
        return loginButtonLabel;
    }

    public String getReason() {
        return reason;
    }

    public String getInvalidLoginLabel() {
        return invalidLoginLabel;
    }

    public String getSessionTimedOutLabel() {
        return sessionTimedOutLabel;
    }

    public String getHomePage() {
        return currentPage.getAbsoluteParent(1).getPath();
    }

}
