// SafeStep backend continuous integration pipeline.
// Runs on the Jenkins controller described in ci/ (JDK 26 and Maven are baked into the image). Stages follow the
// course pipeline: compile, Checkstyle, unit tests, coverage, SonarQube and package; the BDD acceptance tests run
// together with the unit tests. The last two stages build the Docker image of the application and publish it to Docker Hub.
pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 60, unit: 'MINUTES')
  }

  environment {
    JAVA_HOME  = "${env.JAVA_HOME_26}"
    PATH       = "${env.JAVA_HOME_26}/bin:${env.PATH}"
    MAVEN_OPTS = '-Xmx1g'
    // Docker image of the application; each build is tagged with its Jenkins build number
    IMAGE_NAME    = 'safestep-backend'
    TAG           = "${env.BUILD_NUMBER}"
    REGISTRY_USER = 'melga135'   // Docker Hub account that owns the published image
  }

  stages {
    stage('Compile Project') {
      steps {
        sh 'java -version && mvn -version'
        sh 'mvn -B -ntp clean compile'
      }
    }

    // Stock Google checks, report only: the baseline has thousands of findings, so it must not break the build
    stage('Checkstyle Report') {
      steps {
        sh 'mvn -B -ntp checkstyle:checkstyle'
      }
      post {
        always {
          archiveArtifacts artifacts: 'target/checkstyle-result.xml', allowEmptyArchive: true
        }
      }
    }

    // Unit tests of the application layer plus the Cucumber acceptance tests (Spring context + H2)
    stage('Unit and BDD Tests') {
      steps {
        sh 'mvn -B -ntp test'
      }
      post {
        always {
          junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
          archiveArtifacts artifacts: 'target/cucumber-reports/**', allowEmptyArchive: true
        }
      }
    }

    // The 80% gate reads the jacoco.exec produced by the previous stage
    stage('Validate Test Coverage') {
      steps {
        sh 'mvn -B -ntp jacoco:check'
      }
      post {
        always {
          archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('MiSonarServer') {
          // Same exclusions as the JaCoCo gate so both tools measure the same code
          sh '''mvn -B -ntp sonar:sonar \
                -Dsonar.projectKey=safestep-backend \
                -Dsonar.projectName=SafeStep-Backend \
                -Dsonar.coverage.exclusions='**/domain/**,**/infrastructure/**,**/interfaces/rest/resources/**,**/interfaces/rest/*Controller*,**/*Application*' '''
        }
        script {
          timeout(time: 10, unit: 'MINUTES') {
            // Resumes when SonarQube calls the webhook http://jenkins-master:9089/sonarqube-webhook/
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
              error "The pipeline stopped because the code did not pass the SonarQube Quality Gate. Status: ${qg.status}"
            }
          }
        }
      }
    }

    stage('Package Project') {
      steps {
        sh 'mvn -B -ntp package -DskipTests'
      }
      post {
        success {
          archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
      }
    }

    // Builds the application image with the Dockerfile of the repository (it compiles and packages the project in
    // its own build stage). --load keeps the image in the local Docker engine; the next stage publishes it.
    stage('Build Docker Image') {
      steps {
        sh 'docker version --format "Docker engine: {{.Server.Version}}"'
        sh "docker buildx build --platform linux/amd64 -t ${IMAGE_NAME}:${TAG} -t ${IMAGE_NAME}:latest --load ."
        sh "docker image ls ${IMAGE_NAME}"
      }
    }

    // Publishes the image built above to Docker Hub under <account>/<image>:<build number> and :latest.
    // The credential is injected as environment variables and read by the shell, never interpolated by Groovy
    stage('Publish Docker Image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CREDENTIALS',
                                          usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker tag "$IMAGE_NAME:$TAG" "$REGISTRY_USER/$IMAGE_NAME:$TAG"
            docker tag "$IMAGE_NAME:latest" "$REGISTRY_USER/$IMAGE_NAME:latest"
            docker push "$REGISTRY_USER/$IMAGE_NAME:$TAG"
            docker push "$REGISTRY_USER/$IMAGE_NAME:latest"
          '''
        }
      }
      post {
        always {
          // Removes the stored login from the Jenkins home
          sh 'docker logout || true'
        }
      }
    }
  }

  post {
    success { echo 'Pipeline finished: the project compiles, passes its tests and coverage gate and was analyzed.' }
    failure { echo 'Pipeline failed: open the failed stage for the details.' }
  }
}
