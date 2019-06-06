---
layout: doc-page
title: Permissions
---

Asset Share Commons does not provide any permission scheme of it's own, rather it relies on AEM's native permissions to protect Assets and Asset Share pages.

## Protecting Asset Share pages

Asset Share pages, which are simply AEM pages, should be protected using [AEM Pages' Closed User Groups (CUGs)](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/cug.html).

## Protecting Assets

Assets can be protected at the Asset Folder level using [Asset Folders' Closed User Groups (CUGs)](https://helpx.adobe.com/experience-manager/6-5/assets/using/managing-assets-touch-ui.html#ClosedUserGroup).
For more information, a [video illustrating using CUGs on Asset Folders](https://helpx.adobe.com/experience-manager/kt/assets/using/closed-user-groups-feature-video-use.html) is available.

## Using JCR ACLs

If Closed User Groups are inadequate, JCR ACLs can be used to protect (allow/deny) any content in AEM.
Prefer Closed User Groups (CUGs) when protected Pages or Assets, and fall back on ACLs only when CUGs are inadequate.

Remember that JCR ACLs can be applied using more [complex restrictions](https://jackrabbit.apache.org/oak/docs/security/authorization/restriction.html#Built-in_Restrictions) allowing ACLs rules to be set at root folder and match sub-resources based on glob patterns, node types, or node/property names. This can be very helpful in avoiding peppering the JCR with discrete ACE's which in turn require maintenance.

Common use cases for ACLs over CUGs:

* Permissioning at the Asset level (versus the Asset Folder level, which is what CUGs supports).
* Permissioning at the rendition level (ie. only certain users have access to specific static renditions).