# Migration to Maven Central Publishing Portal - Summary

This document summarizes the changes made to migrate from the old OSSRH Nexus Staging deployment to the new Maven Central Publishing Portal.

## Date
February 4, 2026

## Changes Made

### 1. Root POM Configuration (`pom.xml`)

#### Plugin Replacement
- **Removed**: `nexus-staging-maven-plugin` v1.7.0
- **Added**: `central-publishing-maven-plugin` v0.10.0
- **Configuration**:
  - `publishingServerId: central`
  - `autoPublish: true` (automatic publishing after validation)
  - `waitUntil: published` (waits for full publication)
  - `extensions: true` (replaces default deploy plugin)

**Lines Changed**: 96-107 (plugin configuration), 539-544 (plugin management)

#### Distribution Management
- **Old Server ID**: `ossrh`
- **New Server ID**: `central`
- **Old URLs**: 
  - Snapshots: `https://s01.oss.sonatype.org/content/repositories/snapshots/`
  - Releases: `https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/`
- **New URLs**: 
  - Both: `https://central.sonatype.com/`

**Lines Changed**: 1025-1034

### 2. GitHub Actions Workflows

#### Release Workflow (`.github/workflows/maven-release.yml`)

**Changes**:
- Updated `server-id` from `ossrh` to `central`
- Simplified deployment to single `mvn deploy` command
- Removed separate `nexus-staging:deploy-staged` step
- Removed `DskipRemoteStaging=true` flag (no longer needed)

**Before**:
```yaml
- name: Build
  run: mvn clean deploy -DskipRemoteStaging=true -Pcloud,release

- name: Deploy to Central
  run: mvn nexus-staging:deploy-staged -DautoReleaseAfterClose=true
```

**After**:
```yaml
- name: Deploy to Maven Central
  run: mvn clean deploy -Pcloud,release
```

#### Snapshot Workflow (`.github/workflows/snapshot-deploy.yaml`)

**Changes**:
- Updated `server-id` from `ossrh` to `central`
- Simplified deployment to single `mvn deploy` command
- Removed separate `nexus-staging:deploy-staged` step
- Removed `DskipRemoteStaging=true` flag (no longer needed)

**Before**:
```yaml
- name: Build 
  run: mvn clean deploy -DskipRemoteStaging=true -Pcloud,release,jacoco-report

- name: Deploy to Central
  run: mvn nexus-staging:deploy-staged -DautoReleaseAfterClose=true
```

**After**:
```yaml
- name: Deploy Snapshot to Maven Central
  run: mvn clean deploy -Pcloud,release,jacoco-report
```

### 3. New Files Created

#### `settings.xml.example`
- Template Maven settings file for local development
- Documents how to configure Central Portal credentials
- Includes placeholder for GPG passphrase
- To be copied to `~/.m2/settings.xml` by developers

#### `MAVEN_CENTRAL_PUBLISHING.md`
- Comprehensive documentation for the new publishing process
- Includes:
  - Overview of changes
  - GitHub Actions configuration
  - Local testing instructions
  - Troubleshooting guide
  - Comparison of old vs new methods
  - Links to official documentation

#### `MIGRATION_SUMMARY.md` (this file)
- Summary of all changes made during migration

### 4. Updated Files

#### `.gitignore`
- Added `settings.xml` to prevent accidentally committing local credentials
- Keeps `settings.xml.example` in version control as a template

## GitHub Secrets (No Changes Required)

The following secrets remain the same and work with the new system:
- `SONATYPE_USERNAME`: Now contains Central Portal token username
- `SONATYPE_PASSWORD`: Now contains Central Portal token password
- `MAVEN_GPG_PRIVATE_KEY`: (unchanged)
- `GPG_PASSPHRASE`: (unchanged)
- `GPG_SECRET_KEYS`: (unchanged)
- `GPG_OWNERTRUST`: (unchanged)

**Important**: The values in `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` need to be updated to the new Central Portal token credentials. These can be generated at https://central.sonatype.com/account

## Benefits of New Publishing Method

1. **Simpler Process**: Single `mvn deploy` command instead of two-step process
2. **Faster Publishing**: Direct publishing to Central Portal
3. **Better Snapshot Support**: Native snapshot support (added in plugin v0.7.0)
4. **Automatic Publishing**: No manual intervention required with `autoPublish: true`
5. **Modern API**: Uses newer Central Portal API instead of legacy OSSRH

## Testing Checklist

### Local Testing
- [ ] Copy `settings.xml.example` to `~/.m2/settings.xml`
- [ ] Replace placeholders with Central Portal token credentials
- [ ] Test snapshot deployment: `mvn clean deploy -Pcloud,release`
- [ ] Verify deployment appears in Central Portal

### GitHub Actions Testing
- [ ] Update `SONATYPE_USERNAME` secret with Central Portal token username
- [ ] Update `SONATYPE_PASSWORD` secret with Central Portal token password
- [ ] Test snapshot deployment workflow (push to `develop` branch)
- [ ] Verify snapshot appears in Central Portal
- [ ] Test release deployment workflow (manual trigger)
- [ ] Verify release is published to Maven Central

## Migration Steps for Team Members

1. **Update Local Settings**:
   ```bash
   cd asset-share-commons
   cp settings.xml.example ~/.m2/settings.xml
   # Edit ~/.m2/settings.xml and add your Central Portal credentials
   ```

2. **Generate Portal Token**:
   - Visit https://central.sonatype.com/account
   - Generate a new user token
   - Copy token username and password to `~/.m2/settings.xml`

3. **Test Local Deployment**:
   ```bash
   mvn clean deploy -Pcloud,release
   ```

## Rollback Plan

If issues arise, rollback is possible by:

1. Revert changes to `pom.xml` (restore `nexus-staging-maven-plugin`)
2. Revert changes to GitHub Actions workflows
3. Revert `distributionManagement` URLs
4. Update GitHub secrets back to OSSRH credentials

**Note**: Keep a backup of the old configuration until the new system is fully validated.

## Documentation References

- [Central Portal Maven Plugin Documentation](https://central.sonatype.org/publish/publish-portal-maven/)
- [Generating Portal Token](https://central.sonatype.org/publish/generate-portal-token/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
- [Plugin Release Notes](https://central.sonatype.org/publish/publish-portal-maven/#release-notes)

## Support

For issues or questions:
- Check `MAVEN_CENTRAL_PUBLISHING.md` for troubleshooting
- Review Central Portal documentation
- Contact Sonatype support at https://central.sonatype.org/support/
