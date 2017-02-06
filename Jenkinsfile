#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@master')

def dummyForLibrary = ""

try {
  pullRequests.ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        stage('Checkout') {
          info.printNode()
          notify.started()
          checkout scm
        }

        stage('Install build tools') {
          info.printNode()

          // Note: if next stage happens on another node, mvnw might have to download again
          sh "./mvnw --version"
        }

        stage('Build') {
          info.printNode()
          info.printEnv()
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
    notify.successful()
  }
} catch (e) {
  notify.failed()
  throw e
}
