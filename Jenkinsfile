// SafeStep backend CI/CD pipeline.
// Runs on the Jenkins controller described in ci/ (JDK 26 and Maven are baked into the image, the Docker CLI talks to
// the host daemon). Stages follow the course pipeline: compile, Checkstyle, unit tests, coverage, SonarQube, package,
// Docker image; on top of it the BDD acceptance tests run with the unit tests and the Karate API tests are executed
// against the freshly built container.
pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 60, unit: 'MINUTES')
  }

  parameters {
    booleanParam(name: 'PUSH_IMAGE', defaultValue: false,
        description: 'Publish the Docker image to Docker Hub (needs the DOCKER_HUB_CREDENTIALS credential)')
  }

  environment {
    JAVA_HOME     = "${env.JAVA_HOME_26}"
    PATH          = "${env.JAVA_HOME_26}/bin:${env.PATH}"
    MAVEN_OPTS    = '-Xmx1g'
    REGISTRY_USER = 'safestep'
    IMAGE_NAME    = 'safestep-backend'
    TAG           = "${env.BUILD_NUMBER}"
    CI_NETWORK    = 'spring-postgres-net'
    CI_DB         = "safestep-ci-db-${env.BUILD_NUMBER}"
    CI_APP        = "safestep-ci-app-${env.BUILD_NUMBER}"
    CI_ADMIN_USER = 'ci-admin'
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

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t ${IMAGE_NAME}:${TAG} -t ${IMAGE_NAME}:latest .'
      }
    }

    // Black-box API tests (Karate) against the image that was just built, backed by a throw-away PostgreSQL
    stage('API Tests (Karate)') {
      steps {
        script {
          def adminPassword = 'Ci-' + UUID.randomUUID().toString().replace('-', '').substring(0, 16)
          sh '''
            set +x
            docker network inspect ${CI_NETWORK} > /dev/null 2>&1 || docker network create ${CI_NETWORK}
            docker run -d --name ${CI_DB} --network ${CI_NETWORK} \
              -e POSTGRES_DB=safestep -e POSTGRES_USER=safestep -e POSTGRES_PASSWORD=ci-password \
              postgres:18-alpine
            until docker exec ${CI_DB} pg_isready -U safestep > /dev/null 2>&1; do sleep 2; done
          '''
          withEnv(["CI_ADMIN_PASSWORD=${adminPassword}"]) {
            sh '''
              set +x
              docker run -d --name ${CI_APP} --network ${CI_NETWORK} \
                -e SPRING_PROFILES_ACTIVE=prod \
                -e DATABASE_URL=${CI_DB} -e DATABASE_PORT=5432 -e DATABASE_NAME=safestep \
                -e DATABASE_USER=safestep -e DATABASE_PASSWORD=ci-password \
                -e JWT_SECRET="$(head -c 48 /dev/urandom | base64 | tr -d '\\n')" \
                -e SAFESTEP_ADMIN_USERNAME=${CI_ADMIN_USER} -e SAFESTEP_ADMIN_PASSWORD=${CI_ADMIN_PASSWORD} \
                ${IMAGE_NAME}:${TAG}
              for i in $(seq 1 60); do
                code=$(curl -s -o /dev/null -w '%{http_code}' http://${CI_APP}:8092/api/v1/commerce/products || true)
                [ "$code" = "401" ] && break
                sleep 3
              done
              [ "$code" = "401" ] || { docker logs ${CI_APP} | tail -60; echo "The API did not start"; exit 1; }
            '''
            dir('api-tests') {
              sh '''mvn -B -ntp test -Dapi.baseUrl=http://${CI_APP}:8092 \
                    -Dapi.admin.username=${CI_ADMIN_USER} -Dapi.admin.password=${CI_ADMIN_PASSWORD}'''
            }
          }
        }
      }
      post {
        always {
          sh 'docker logs ${CI_APP} > api-container.log 2>&1 || true'
          archiveArtifacts artifacts: 'api-tests/target/karate-reports/**, api-container.log', allowEmptyArchive: true
          sh 'docker rm -f ${CI_APP} ${CI_DB} > /dev/null 2>&1 || true'
        }
      }
    }

    stage('Publish Docker Image') {
      when { expression { params.PUSH_IMAGE } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CREDENTIALS',
            usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
            docker tag ${IMAGE_NAME}:${TAG} ${REGISTRY_USER}/${IMAGE_NAME}:${TAG}
            docker tag ${IMAGE_NAME}:${TAG} ${REGISTRY_USER}/${IMAGE_NAME}:latest
            docker push ${REGISTRY_USER}/${IMAGE_NAME}:${TAG}
            docker push ${REGISTRY_USER}/${IMAGE_NAME}:latest
          '''
        }
      }
    }
  }

  post {
    success { echo "Pipeline finished: ${IMAGE_NAME}:${TAG} is built and verified." }
    failure { echo 'Pipeline failed: open the failed stage for the details.' }
  }
}
