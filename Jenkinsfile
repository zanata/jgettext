#!/usr/bin/env groovy
@Library('github.com/zanata/zanata-pipeline-library@0.1')

def dummyForLibrary = ""

try {
  pullRequests.ensureJobDescription()

  timestamps {
    node {
      ansicolor {
        // ensure the build can handle at-signs in paths:
        dir("@") {
          stage('Checkout') {
            info.printNode()
            notify.started()
            checkout scm
          }

          stage('Install build tools') {
            sh "./mvnw --version"
          }

          stage('Build') {
            info.printEnv()
            def testReports = '**/target/surefire-reports/TEST-*.xml'
            sh """./mvnw -e clean verify \
                       --batch-mode \
                       --settings .travis-settings.xml \
                       --update-snapshots \
                       -Dmaven.test.failure.ignore \
                       -DstaticAnalysis \
            """
            // step([$class: 'CheckStylePublisher', pattern: '**/target/checkstyle-result.xml', unstableTotalAll:'0'])
            // step([$class: 'PmdPublisher', pattern: '**/target/pmd.xml', unstableTotalAll:'0'])
            // step([$class: 'FindBugsPublisher', pattern: '**/findbugsXml.xml', unstableTotalAll:'0'])

            junit testResults: testReports

            // send test coverage data to codecov.io
            try {
              withCredentials(
                  [[$class: 'StringBinding',
                    credentialsId: 'codecov_jgettext',
                    variable: 'CODECOV_TOKEN']]) {
                // NB the codecov script uses CODECOV_TOKEN
                sh "curl -s https://codecov.io/bash | bash -s - -K"
              }
            } catch (InterruptedException e) {
              throw e
            } catch (hudson.AbortException e) {
              throw e
            } catch (e) {
              echo "[WARNING] Ignoring codecov error: $e"
            }

            // skip coverage report if unstable
            if (currentBuild.result == null) {
              step([ $class: 'JacocoPublisher' ])
            }
            notify.testResults("UNIT")
            //sh "curl -s https://codecov.io/bash | bash"
          }
        }
      }
    }
  }
} catch (e) {
  notify.failed()
  throw e
}
