# Snapshot Deployment Fix

## Issue
GitHub Actions snapshot deployment was failing with:
```
Failed to deploy artifacts: Could not find artifact com.adobe.aem.commons:assetshare:pom:3.13.3-20260204.160725-1 in central
```

## Root Cause
1. **Wrong Repository URL**: Snapshots require a different endpoint than releases
2. **Missing Namespace Configuration**: Snapshot support must be explicitly enabled in the Maven Central Portal

## Changes Made

### 1. Fixed Snapshot Repository URL

**File**: `pom.xml`

Changed distribution management to use the correct URLs:

```xml
<snapshotRepository>
    <id>central</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</snapshotRepository>
<repository>
    <id>central</id>
    <url>https://central.sonatype.com</url>
</repository>
```

### 2. Adjusted Plugin Configuration

**File**: `pom.xml`

Changed the default plugin behavior to be snapshot-friendly:

```xml
<!-- Default: optimized for snapshots -->
<configuration>
    <publishingServerId>central</publishingServerId>
    <autoPublish>false</autoPublish>          <!-- Just upload, don't publish -->
    <waitUntil>uploaded</waitUntil>           <!-- Don't wait for publishing -->
    <ignorePublishedComponents>true</ignorePublishedComponents>
</configuration>
```

Added release profile override:

```xml
<!-- Release profile: override for releases -->
<plugin>
    <groupId>org.sonatype.central</groupId>
    <artifactId>central-publishing-maven-plugin</artifactId>
    <configuration>
        <autoPublish>true</autoPublish>        <!-- Auto-publish releases -->
        <waitUntil>published</waitUntil>       <!-- Wait for publication -->
    </configuration>
</plugin>
```

### 3. Updated Documentation

**File**: `MAVEN_CENTRAL_PUBLISHING.md`

- Added prerequisites section explaining snapshot enablement
- Updated configuration documentation
- Clarified snapshot vs release behavior

## Required Action: Enable Snapshots in Portal

⚠️ **YOU MUST DO THIS BEFORE SNAPSHOTS WILL WORK**:

1. Go to https://central.sonatype.com/publishing/namespaces
2. Find your namespace: `com.adobe.aem.commons`
3. Click the dropdown menu (three dots) on the right
4. Select **"Enable SNAPSHOTs"**
5. Confirm in the popup
6. Verify you see a "SNAPSHOTs enabled" badge

## How It Works Now

### Snapshot Deployments (develop branch)
```bash
mvn clean deploy -Pcloud,release
```
- Uploads to: `https://central.sonatype.com/repository/maven-snapshots/`
- No validation performed (`autoPublish=false`)
- Returns immediately after upload (`waitUntil=uploaded`)
- Snapshots are cleaned up after 90 days

### Release Deployments
```bash
mvn clean deploy -Pcloud,release
```
- Uploads to: `https://central.sonatype.com/`
- Full validation performed
- Auto-publishes to Maven Central
- Waits for publication to complete

## Testing

After enabling snapshots in the Portal:

1. **Push to develop branch** - This will trigger the snapshot workflow
2. **Monitor GitHub Actions** - Watch the deployment succeed
3. **Verify snapshot published** - Check https://central.sonatype.com/repository/maven-snapshots/com/adobe/aem/commons/

## Key Differences Between Old and New System

| Aspect | Old (OSSRH) | New (Central Portal) |
|--------|-------------|---------------------|
| Snapshot URL | `https://oss.sonatype.org/content/repositories/snapshots/` | `https://central.sonatype.com/repository/maven-snapshots/` |
| Release URL | `https://oss.sonatype.org/service/local/staging/deploy/maven2/` | `https://central.sonatype.com` |
| Snapshot Setup | Automatic | Requires namespace enablement |
| Snapshot Validation | None | None |
| Snapshot Cleanup | None | 90 days |
| Release Validation | Manual staging | Automatic |

## Summary

The configuration is now correct. Once you enable snapshots for your namespace in the Portal, both snapshot and release deployments should work seamlessly in GitHub Actions!
