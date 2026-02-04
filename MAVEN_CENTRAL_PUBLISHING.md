# Maven Central Publishing Guide

This project has been updated to use the new [Maven Central Publishing Portal](https://central.sonatype.org/) with the `central-publishing-maven-plugin`.

## Overview

The project now uses:
- **Plugin**: `central-publishing-maven-plugin` version 0.10.0
- **Publishing URL**: `https://central.sonatype.com` (plugin automatically routes snapshots vs releases)
- **Server ID**: `central` (in settings.xml)
- **Auto-publish**: Enabled for releases, upload-only for snapshots

## GitHub Actions Deployment

### Secrets Required

The following GitHub secrets are configured for automated deployments:

- `SONATYPE_USERNAME`: Your Central Portal token username
- `SONATYPE_PASSWORD`: Your Central Portal token password
- `MAVEN_GPG_PRIVATE_KEY`: GPG private key for signing artifacts
- `GPG_PASSPHRASE`: Passphrase for the GPG key
- `GPG_SECRET_KEYS`: Base64-encoded GPG secret keys
- `GPG_OWNERTRUST`: Base64-encoded GPG owner trust

### Workflows

1. **Release Workflow** (`maven-release.yml`): Triggered manually for releases
2. **Snapshot Workflow** (`snapshot-deploy.yaml`): Triggered automatically on push to `develop` branch

Both workflows now use a single `mvn deploy` command with the plugin configured for automatic publishing.

## Local Testing

### Prerequisites

1. **Generate a Portal Token**: Go to https://central.sonatype.com/account and generate a user token
2. **Configure Maven Settings**: Copy the provided `settings.xml.example` to `~/.m2/settings.xml`
3. **GPG Key**: Ensure you have a GPG key set up for signing artifacts

### Setup Steps

1. Copy the example settings file:
   ```bash
   cp settings.xml.example ~/.m2/settings.xml
   ```

2. Edit `~/.m2/settings.xml` and replace the placeholders:
   ```xml
   <server>
     <id>central</id>
     <username>YOUR_TOKEN_USERNAME</username>
     <password>YOUR_TOKEN_PASSWORD</password>
   </server>
   ```

3. (Optional) If you need to encrypt your password, use Maven password encryption:
   ```bash
   mvn --encrypt-password YOUR_TOKEN_PASSWORD
   ```

### Testing Snapshot Deployment

**⚠️ Known Limitation**: Local snapshot deployment has issues with multi-module reactor builds due to dependency resolution timing in the `central-publishing-maven-plugin`. The plugin tries to verify that parent POMs and dependencies exist in the remote repository immediately after upload, before they're fully indexed.

**Recommendation**: Test snapshot deployments using GitHub Actions first, as the CI environment handles this better.

To test locally without deploying:

```bash
# Build and verify (recommended for local testing)
mvn clean verify -Pcloud,release
```

**Local deployment attempts will likely fail** with errors like:
```
Could not find artifact com.adobe.aem.commons:assetshare:pom:3.13.3-SNAPSHOT in central
```

This is expected and does not indicate a problem with your configuration. The GitHub Actions workflows are properly configured and should work correctly.

### Testing Release Deployment

For release versions (even version numbers per the odd/even version policy):

```bash
# Full release process (prepare and deploy)
mvn clean deploy -Pcloud,release
```

## Plugin Configuration

The `central-publishing-maven-plugin` is configured in the root `pom.xml` with:

```xml
<plugin>
  <groupId>org.sonatype.central</groupId>
  <artifactId>central-publishing-maven-plugin</artifactId>
  <version>0.10.0</version>
  <extensions>true</extensions>
  <configuration>
    <publishingServerId>central</publishingServerId>
    <autoPublish>true</autoPublish>
    <waitUntil>published</waitUntil>
  </configuration>
</plugin>
```

### Configuration Options

- **`autoPublish: true`**: Automatically publishes after validation (no manual approval needed)
- **`waitUntil: published`**: Waits until the deployment is fully published to Maven Central
- **`publishingServerId: central`**: Matches the server ID in settings.xml

## Snapshot Support

The new plugin supports snapshot deployments out of the box (as of version 0.7.0). Snapshots are:
- Automatically detected by the `-SNAPSHOT` suffix in the version
- Deployed to the snapshot repository
- Published automatically if `autoPublish` is enabled

## Differences from Old Publishing Method

### Old Method (nexus-staging-maven-plugin)
- Required two steps: `mvn deploy` + `mvn nexus-staging:deploy-staged`
- Used OSSRH staging repository: `https://s01.oss.sonatype.org/`
- Server ID: `ossrh`
- Required manual intervention for publishing (unless auto-release was configured)

### New Method (central-publishing-maven-plugin)
- Single step: `mvn deploy`
- Uses Central Portal: `https://central.sonatype.com/`
- Server ID: `central`
- Automatic publishing with `autoPublish: true`
- Simpler configuration and faster publishing

## Troubleshooting

### Authentication Failures

If you get 401 authentication errors:
1. Verify your token is valid at https://central.sonatype.com/account
2. Ensure the token is correctly set in `~/.m2/settings.xml`
3. Check that the server ID is `central` (not `ossrh`)

### GPG Signing Issues

If GPG signing fails:
1. Ensure GPG is installed: `gpg --version`
2. List your keys: `gpg --list-keys`
3. Configure the passphrase in settings.xml or use `gpg.passphrase` property

### Validation Failures

If the bundle fails validation:
1. Check that all required files are present (POM, JAR, sources, javadoc, signatures)
2. Verify POM metadata meets Maven Central requirements
3. Check the validation report in the Central Portal UI

## BND Baseline Plugin

The `bnd-baseline-maven-plugin` is configured to:
- **Automatically detect** the newest available version in Maven Central to baseline against
- **Skip by default** during migration (due to compatibility issues with old OSSRH artifacts)
- **Can be enabled** with a command-line property once new artifacts are published

### How Auto-Detection Works

The plugin automatically finds the highest version below your current version using the pattern `(,${project.version})`. For example, if you're building `3.13.3-SNAPSHOT`, it will automatically find and use `3.8.12` (or whatever the latest release is).

### Current Status

During migration, baseline checking is skipped by default because:
- Old artifacts are in OSSRH repository structure  
- New artifacts will be in Maven Central structure
- Comparison fails due to repository incompatibility

### To Enable Baseline Checking

Once new artifacts are successfully published to Maven Central:

```bash
# Enable baseline checking for this build
mvn verify -Dbnd.baseline.skip=false

# Or make it permanent by changing the property in pom.xml:
<bnd.baseline.skip>false</bnd.baseline.skip>
```

After your first successful deployment, the plugin will automatically use the newest deployed version for baseline comparison.

## Additional Resources

- [Central Portal Documentation](https://central.sonatype.org/publish/publish-portal-maven/)
- [Generating a Portal Token](https://central.sonatype.org/publish/generate-portal-token/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
- [central-publishing-maven-plugin Release Notes](https://central.sonatype.org/publish/publish-portal-maven/#release-notes)
