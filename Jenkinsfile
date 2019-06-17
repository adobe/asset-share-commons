

// Tools
def jdkTool = 'JDK8'
def mavenTool = 'Maven 3.5'

def releaseOptions = ['Major', 'Minor', 'Bug Fix']

def currentVersion
String releaseVersion
String snapshotVersion

def versionChangelog
String releaseArtifact

properties([
    buildDiscarder(
        logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '1')
    ),
    disableConcurrentBuilds()
])

pipeline {
  environment {
    MVN_SETTTINGS_ID = 'acsbuild-settings'
    GITHUB_TOKEN_CREDENTIALS_ID = 'adobe-acsbuild-github-token-creds'
    GITHUB_CREDENTIALS_FILE = '.github-creds'
    GIT_USER = 'Adobe ACSBuild'
    GIT_EMAIL = 'acsbuild@adobe.com'
    GITHUB_API = "https://api.github.com/repos"
    CHANGELOG = 'CHANGELOG.md'
    CHANGELOG_REGEX = /(?ms)(##\s+\[Unreleased\].*?)(##\s+\[v\d+\.\d+\.\d+\]|\Z)/
    JAVADOC_STASH = 'javadocs'
    APPS_PACKAGE_NAME = 'asset-share-commons.ui.apps'
    CONTENT_PACKAGE_NAME = 'asset-share-commons.ui.content'
  }
  agent any
  stages {
    stage('Build') {
      tools {
        maven "${mavenTool}"
        jdk "${jdkTool}"
      }
      agent any
      steps {
        script {
          sh 'mvn clean package'
        }
      }
    }
    stage('Metadata') {
      tools {
        maven "${mavenTool}"
        jdk "${jdkTool}"
      }
      agent any
      steps {
        script {
          currentVersion = sh(
              returnStdout: true,
              script: 'mvn -q help:evaluate -Dexpression=project.version -DforceStdout=true'
          )
        }
      }
    }
    stage('Version Prompt') {
      agent any
      steps {
        script {

          def releaseType = input(message: "Release Type:",
              parameters: [
                  choice(defaultValue: '',
                      choices: releaseOptions.join('\n'),
                      description: "Select the type of release that is being performed..",
                      name: 'Release Type')
              ]
          )
          releaseVersion = getReleaseVersion(currentVersion, releaseType)
          snapshotVersion = getSnapshotVersion(releaseVersion)
          input "Version to create: ${releaseVersion} Is this correct?"
          echo "Release Version: ${releaseVersion}"
          echo "Snapshot Version: ${snapshotVersion}"
        }
      }
    }
    stage('Update Changelog') {
      agent any
      steps {
        script {
          writeGithubCredentials(this)
          versionChangelog = getVersionChangelog(this, releaseVersion)
          prepareReleaseChangelog(this, versionChangelog)
        }
      }
      post {
        always {
          script {
            deleteGithubCredentials(this)
          }
        }
      }
    }
    stage('Merge to Master') {
      agent any
      steps {
        script {
          writeGithubCredentials(this)
          mergeBranches(this, 'origin/develop','master', 'Merge from Develop', "v${releaseVersion} Release")
        }
      }
      post {
        always {
          script {
            deleteGithubCredentials(this)
          }
        }
      }
    }
    stage('Release') {
      tools {
        maven "${mavenTool}"
        jdk "${jdkTool}"
      }
      agent any
      steps {
        script {
          releaseArtifact = mvnRelease(this, releaseVersion, snapshotVersion)
          stashJavadocs(this)
          stashContentPackage(this)
        }
      }
    }
    stage('Backmerge Dev') {
      agent any
      steps {
        script {
          writeGithubCredentials(this)
          mergeBranches(this, 'origin/master','develop', 'Merge from Master', "v${snapshotVersion}")
          prepareDevelopChangelog(this)
        }
      }
      post {
        always {
          script {
            deleteGithubCredentials(this)
          }
        }
      }
    }
    stage('Update Docs') {
      agent any
      steps {
        script {
          writeGithubCredentials(this)
          updateGhPages(this, releaseVersion, versionChangelog)
        }
      }
      post {
        always {
          script {
            deleteGithubCredentials(this)
          }
        }
      }
    }
    stage('Github Release') {
      agent any
      steps {
        script {
          def relUplUrl = createGithubRelease(this, releaseVersion)
          uploadReleaseArtifacts(this, releaseVersion, relUplUrl)
        }
      }
    }
    stage ("Adobe Nexus Update") {
      agent any
      steps {
        script {
          echo 'Artifacts are published. ' +
              'Contact the Adobe release contact and request an artifact ' +
              'deployment to repo.adobe.com (Clone and edit INFRA-5605).'
        }
      }
    }
  }
}

@NonCPS
static def determineScmInfo(scm) {

  def info = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/')
  def org = info[info.size() - 2]
  def name = info[info.size() - 1].split("\\.")[0]

  return [org, name]
}

@NonCPS
static def slurpJson(data) {
  def slurper = new groovy.json.JsonSlurperClassic()
  return slurper.parseText(data)
}

@NonCPS
static def getReleaseVersion(version, type) {

  def versionArr = version.tokenize('.')
  def major = versionArr[0]
  def minor = versionArr[1]
  def bugfix = versionArr[2].replace('-SNAPSHOT', '')

  switch (type) {
    case 'Major':
      major = Integer.parseInt(major) + 1
      minor = bugfix = 0
      break
    case 'Minor':
      minor = Integer.parseInt(minor) + 1
      bugfix = 0
      break
    case 'Bug Fix':
      bugfix = Integer.parseInt(bugfix) + 1
      break
  }

  def release = [major, minor, bugfix].join('.')
  return release
}

@NonCPS
static def getSnapshotVersion(version) {
  def versionArr = version.tokenize('.')
  def major = versionArr[0]
  def minor = versionArr[1]
  def bugfix = versionArr[2]
  bugfix = Integer.parseInt(bugfix) + 1

  def snapshot = [major, minor, bugfix].join('.')
  return "${snapshot}-SNAPSHOT"
}

static def getVersionChangelog(script, version) {

  String changelog = readChangelog(script)
  def matcher = (changelog =~ script.env.CHANGELOG_REGEX)
  def versionChangelog = matcher[0][1]
  versionChangelog = versionChangelog.replaceFirst(/\[Unreleased\]/, "[v${version}]")
  return versionChangelog
}

static def writeGithubCredentials(script) {

  def tmpDir = script.pwd tmp: true
  script.withCredentials([
      script.usernameColonPassword(credentialsId: script.env.GITHUB_TOKEN_CREDENTIALS_ID, variable: 'CREDENTIALS')
  ]) {

    def scmUrl = script.scm.getUserRemoteConfigs()[0].getUrl()
    scmUrl = scmUrl.replaceAll('https://', 'https://\\$CREDENTIALS@')
    script.sh "git config --local credential.helper 'store --file ${tmpDir}/${script.env.GITHUB_CREDENTIALS_FILE}'"
    script.sh "echo ${scmUrl} > ${tmpDir}/${script.env.GITHUB_CREDENTIALS_FILE}"
  }
}

static def deleteGithubCredentials(script) {
  def tmpDir = script.pwd tmp: true
  script.sh "rm -f ${tmpDir}/${script.env.GITHUB_CREDENTIALS_FILE}"
}

static def gitCheckoutBranch(script, branch) {

  def scmUrl = script.scm.getUserRemoteConfigs()[0].getUrl()
  script.git(
      url: scmUrl,
      branch: branch,
      credentialsId: script.env.GITHUB_TOKEN_CREDENTIALS_ID
  )

  script.sh "git branch -u origin/${branch}"

}

// Changelog processing methods.

static def prepareReleaseChangelog(script, changelog) {
  gitCheckoutBranch(script, 'develop')
  updateChangeLog(script,  script.env.CHANGELOG_REGEX , "${changelog}\$2")
  commitChangelog(script, 'Preparing Changelog for release.')
}

static def prepareDevelopChangelog(script) {

  def template = '''\
    ## [Unreleased]
    
    ### Added
    
    ### Changed
    
    ### Fixed


    ##'''.stripIndent()

  gitCheckoutBranch(script, 'develop')
  updateChangeLog(script, '##', template)
  commitChangelog(script, 'Preparing Changelog new development.')
}

static def updateChangeLog(script, search, replace) {
  def changelog = readChangelog(script)
  changelog = changelog.replaceFirst(search, replace)
  writeChangelog(script, changelog)
}

static def readChangelog(script) {
  def changelog = script.readFile script.env.CHANGELOG
  return changelog
}

static def writeChangelog(script, content) {
  script.writeFile file: script.env.CHANGELOG, text: content
}

static def commitChangelog(script, msg) {
  script.sh "git add ${script.env.CHANGELOG}"
  gitCommit(script, msg)
  gitPush(script)
}


// Git Processing Methods

static def createMsgFlags(String... msgs) {
  def msg = msgs.join("' -m '")
  msg = "-m '${msg}'"
  return msg
}

static def gitCommit(script, String... msgs) {
  script.sh "git config user.name '${script.env.GIT_USER}' --replace-all"
  script.sh "git config user.email ${script.env.GIT_EMAIL} --replace-all"
  script.sh "git commit ${createMsgFlags(msgs)}"
}

static def gitPush(script, branch = '') {

  script.sh 'git config push.default simple'
  script.sh "git push origin ${branch}"
}

static def mergeBranches(script, from, to, String... msgs) {
  gitCheckoutBranch(script, to)
  gitMerge(script, from, msgs)
  gitPush(script, to)
}

static def gitMerge(script, from, String... msgs) {
  script.sh "git config user.name '${script.env.GIT_USER}' --replace-all"
  script.sh "git config user.email ${script.env.GIT_EMAIL} --replace-all"
  script.sh "git merge --no-ff ${createMsgFlags(msgs)} ${from}"
}



// Release Methods

static def mvnRelease(script, version, snapshot) {
  def (org, repo) = determineScmInfo(script.scm)

  gitCheckoutBranch(script, 'master')
  script.configFileProvider(
      [script.configFile(fileId: 'acsbuild-settings', variable: 'MAVEN_SETTINGS')]) {

    script.sh 'mvn release:clean'
    script.sh 'mvn -s $MAVEN_SETTINGS -q -B ' +
        '-Prelease,adobe-public ' +
        'release:prepare ' +
        "-Dtag=${repo}-${version} " +
        "-DreleaseVersion=${version} " +
        "-DdevelopmentVersion=${snapshot}"
    script.sh 'mvn -s $MAVEN_SETTINGS -q -Pbintray-release,adobe-public release:perform'
  }
}

static def stashJavadocs(script) {
  stashFiles(script, script.env.JAVADOC_STASH, 'core/target/apidocs', '**/*')
}

static def stashContentPackage(script) {
  stashFiles(script, script.env.CONTENT_PACKAGE_NAME, 'ui.content/target', "${script.env.CONTENT_PACKAGE_NAME}-*.zip")
  stashFiles(script, script.env.APPS_PACKAGE_NAME, 'ui.apps/target', "${script.env.APPS_PACKAGE_NAME}-*.zip")
}

static def stashFiles(script, name, dir, files) {
  script.dir(dir) {
    script.stash name: name, includes: files
  }
}

static def createGithubRelease(script, version) {

  def (org, repo) = determineScmInfo(script.scm)

  def release = "${repo}-${version}"
  def response = script.httpRequest(
      authentication: script.env.GITHUB_TOKEN_CREDENTIALS_ID,
      httpMode: 'POST',
      contentType: 'APPLICATION_JSON',
      requestBody: """{ 
        "tag_name": "${release}",
        "name": "${release}",
        "body": "See [Changelog](/CHANGELOG.md) for details.",
        "draft": false,
        "prerelease": false
      }""",
      url: "${script.env.GITHUB_API}/${org}/${repo}/releases",
  )
  def data = slurpJson(response.content)
  def uploadUrl = data.upload_url.replaceAll(/\{\?name,label}/, '')
  return uploadUrl
}

static def uploadReleaseArtifacts(script, version, url) {
  script.unstash name: script.env.CONTENT_PACKAGE_NAME
  script.unstash name: script.env.APPS_PACKAGE_NAME
  script.withCredentials([
      script.usernameColonPassword(credentialsId: script.env.GITHUB_TOKEN_CREDENTIALS_ID, variable: 'CREDENTIALS')
  ]) {

    script.sh """
      curl -u "\$CREDENTIALS" \
        -H "Content-Type: application/zip" \
        --data-binary @${script.env.CONTENT_PACKAGE_NAME}-${version}.zip \
        ${url}?name=${script.env.CONTENT_PACKAGE_NAME}-${version}.zip
    """
    script.sh """
      curl -u "\$CREDENTIALS" \
        -H "Content-Type: application/zip" \
        --data-binary @${script.env.APPS_PACKAGE_NAME}-${version}.zip \
        ${url}?name=${script.env.APPS_PACKAGE_NAME}-${version}.zip
    """
  }
}

// Doc Pages Methods.

static def updateGhPages(script, version, changelog) {
  gitCheckoutBranch(script, 'gh-pages')
  createVersionFile(script, version, changelog)
  updateJavadocs(script)
  commitDocs(script, version)
}

static def createVersionFile(script, version, content) {

  String tpl = """\
    ---
    layout: content-page
    title: v${version}
    ---
    
    """.stripIndent()

  content = content.replaceFirst("## v${version}", '')
  String fileContent = "${tpl}${content}"
  String path = "v${buildVersionFolder(version)}"

  script.dir('_pages/releases') {
    script.sh "mkdir -p ${path}"
    script.dir(path) {
      script.writeFile file: "index.md", text: fileContent
    }
  }

}

static def buildVersionFolder(version) {
  return version.tokenize('.').join('-')
}

static def updateJavadocs(script) {
  script.dir('_apidocs') {
    script.unstash name: script.env.JAVADOC_STASH
  }
}

static def commitDocs(script, version) {
  script.sh 'git add apidocs _pages/releases'
  String msg = "Updating docs for release v${version}"
  gitCommit(script, msg)
  gitPush(script)
}
