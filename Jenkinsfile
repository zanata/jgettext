#!/usr/bin/env groovy

try {
  ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          printNodeInfo()
          notifyStarted()
          checkout scm
        }

        stage('Install build tools') {
          printNodeInfo()

          // Note: if next stage happens on another node, mvnw might have to download again
          sh "./mvnw --version"
        }

        stage('Build') {
          printNodeInfo()
          printEnvInfo()
          def testReports = '**/target/surefire-reports/TEST-*.xml'
          sh "shopt -s globstar && rm -f $testReports"
          sh """./mvnw clean verify \
                     --batch-mode \
                     --settings .travis-settings.xml \
                     --update-snapshots \
                     -Dmaven.test.failure.ignore \
                     -DstaticAnalysis \
          """
          junit testResults: testReports, testDataPublishers: [[$class: 'StabilityTestDataPublisher']]

          //sh "curl -s https://codecov.io/bash | bash"
        }
      }
    }

    // TODO in case of failure, notify culprits via IRC and/or email
    // https://wiki.jenkins-ci.org/display/JENKINS/Email-ext+plugin#Email-extplugin-PipelineExamples
    // http://stackoverflow.com/a/39535424/14379
    // IRC: https://issues.jenkins-ci.org/browse/JENKINS-33922
    notifySuccessful()
  }
} catch (e) {
  notifyFailed()
  throw e
}

// TODO factor these out into a shared pipeline library

@NonCPS
void ensureJobDescription() {
  if (env.CHANGE_ID) {
    try {
      def job = manager.build.project
      // we only want to do this once, to avoid hammering the github api
      if (!job.description || !job.description.contains(env.CHANGE_URL)) {
        def sourceBranchLabel = getSourceBranchLabel()
        def abbrTitle = truncateAtWord(env.CHANGE_TITLE, 50)
        def prDesc = """<a title=\"${env.CHANGE_TITLE}\" href=\"${env.CHANGE_URL}\">PR #${env.CHANGE_ID} by ${env.CHANGE_AUTHOR}</a>:
                       |$abbrTitle
                       |merging ${sourceBranchLabel} to ${env.CHANGE_TARGET}""".stripMargin()
        // ideally we would show eg sourceRepo/featureBranch ⭆ master
        // but there is no env var with that info

        println "description: " + prDesc
        //currentBuild.description = prDesc
        job.description = prDesc
        job.save()
        null // avoid returning non-Serializable Job
      }
    } catch (e) {
      // NB we don't want to fail the build just because of a problem in this method
      println e
      e.printStackTrace() // not sure how to log this to the build log
    }
  }
}

@NonCPS
String getSourceBranchLabel() {
  println "checking github api for pull request details"
  // TODO use github credentials to avoid rate limiting
  def prUrl = new URL("https://api.github.com/repos/zanata/zanata-platform/pulls/${env.CHANGE_ID}")
  def sourceBranchLabel = new groovy.json.JsonSlurper().parseText(prUrl.text).head.label
  return sourceBranchLabel
}

// Based on http://stackoverflow.com/a/37688740/14379
@NonCPS
static String truncateAtWord(String content, int maxLength) {
  def ellipsis = "…"
  // Is content > than the maxLength?
  if (content.size() > maxLength) {
    def bi = java.text.BreakIterator.getWordInstance()
    bi.setText(content);
    def cutoff = bi.preceding(maxLength - ellipsis.length())
    // if too short when cutting by words, ignore words
    if (cutoff < maxLength / 2) {
      cutoff = maxLength - ellipsis.length()
    }
    // Truncate
    return content.substring(0, cutoff) + ellipsis
  } else {
    return content
  }
}

// these wrappers don't seem to be built in yet
void ansicolor(Closure wrapped) {
  // NB this wrapper requires a node
  wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm', 'defaultFg': 1, 'defaultBg': 2]) {
    wrapped.call()
  }
}

void printNodeInfo() {
  println "running on node ${env.NODE_NAME}"
}

void printEnvInfo() {
  sh "env|sort"
}

void notifyStarted() {
  hipchatSend color: "GRAY", notify: true, message: "STARTED: Job " + jobLink()
}

void notifyTestResults(def testType) {
  // if tests have failed currentBuild.result will be 'UNSTABLE'
  if (currentBuild.result != null) {
    hipchatSend color: "YELLOW", notify: true, message: "TESTS FAILED ($testType): Job " + jobLink()
  } else {
    hipchatSend color: "GREEN", notify: true, message: "TESTS PASSED ($testType): Job " + jobLink()
  }
}

void notifySuccessful() {
  hipchatSend color: "GRAY", notify: true, message: "SUCCESSFUL: Job " + jobLink()
}

void notifyFailed() {
  hipchatSend color: "RED", notify: true, message: "FAILED: Job " + jobLink()
}

String jobLink() {
  "<a href=\"${env.BUILD_URL}\">${env.JOB_NAME} #${env.BUILD_NUMBER}</a>"
}
