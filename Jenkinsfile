pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK11'
    }

    environment {
        DOCKER_IMAGE = "devsecops-backend"
        DOCKER_TAG = "${env.BUILD_ID}"
        SONAR_HOST_URL = "http://192.168.56.10:9000"
        SONAR_CREDS = credentials('sonarqube-creds')
        SONAR_PROJECT_KEY = "devsecops-backend"
        NEXUS_URL = "http://192.168.56.10:8081"
        PROJECT_VERSION = "1.0.0-${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout Git') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/yba-sudo/devsecops-project.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('Build & Test with Coverage') {
            steps {
                dir('backend') {
                    sh 'mvn clean compile test'
                    sh 'mvn jacoco:report'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.projectName="DevSecOps Backend" \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_CREDS_USR} \
                        -Dsonar.password=${SONAR_CREDS_PSW}
                    """
                }
            }
        }

        stage('Check Quality Gate') {
            steps {
                script {
                    sleep 10
                    def maxAttempts = 12
                    def qualityGateStatus = ""

                    for (def attempt = 1; attempt <= maxAttempts; attempt++) {
                        echo "Checking Quality Gate (Attempt ${attempt}/${maxAttempts})..."

                        try {
                            def response = sh(
                                script: """
                                    curl -s -u ${SONAR_CREDS_USR}:${SONAR_CREDS_PSW} \
                                    "${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}"
                                """,
                                returnStdout: true
                            ).trim()

                            qualityGateStatus = sh(
                                script: """
                                    echo '${response}' | jq -r '.projectStatus.status'
                                """,
                                returnStdout: true
                            ).trim()

                            if (qualityGateStatus == "OK") {
                                echo "✅ Quality Gate PASSED!"
                                break
                            } else if (qualityGateStatus == "ERROR") {
                                error "❌ Quality Gate FAILED! Check SonarQube for details."
                            }
                        } catch (Exception e) {
                            echo "Attempt ${attempt} failed: ${e.getMessage()}"
                        }
                        sleep 5
                    }

                    if (qualityGateStatus != "OK") {
                        error "❌ Quality Gate check failed"
                    }
                }
            }
        }

        stage('Package Application') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                dir('backend') {
                    script {
                        // First update version in pom.xml
                        sh "mvn versions:set -DnewVersion=${PROJECT_VERSION} -DgenerateBackupPoms=false"
                        
                        // Re-package with new version
                        sh 'mvn package -DskipTests'
                        
                        // Deploy main JAR (not javadoc or sources)
                        sh """
                            mvn deploy:deploy-file \
                            -Durl=http://admin:admin@192.168.56.10:8081/repository/maven-releases/ \
                            -DrepositoryId=nexus-releases \
                            -Dfile=target/devsecops-backend-${PROJECT_VERSION}.jar \
                            -DpomFile=pom.xml \
                            -DgroupId=com.devsecops \
                            -DartifactId=devsecops-backend \
                            -Dversion=${PROJECT_VERSION} \
                            -Dpackaging=jar
                        """
                        
                        echo "✅ Artifact ${PROJECT_VERSION} deployed to Nexus!"
                        echo "📦 Nexus URL: ${NEXUS_URL}/#browse/browse:maven-releases:com%2Fdevsecops%2Fdevsecops-backend%2F${PROJECT_VERSION}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${PROJECT_VERSION}")
                }
            }
        }

        stage('Run with Docker Compose') {
            steps {
                sh 'docker stop devsecops-backend devsecops-frontend 2>/dev/null || true'
                sh 'docker rm devsecops-backend devsecops-frontend 2>/dev/null || true'
                sh 'docker-compose down || true'
                sh 'docker-compose up -d --build'
                sleep 30
            }
        }

        stage('Integration Test') {
            steps {
                sh '''
                    curl -f http://localhost:8088/api/health
                    curl -f http://localhost:8088/api/hello
                    curl -f "http://localhost:8088/api/greet?name=Jenkins"
                '''
            }
        }
    }

    post {
        always {
            junit 'backend/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
        }
        success {
            echo '🎉 Pipeline completed successfully!'
            echo "✅ Code Quality: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}"
            echo "📦 Artifact: ${NEXUS_URL}/#browse/browse:maven-releases:com%2Fdevsecops%2Fdevsecops-backend"
            echo "🐳 Docker Image: ${DOCKER_IMAGE}:${PROJECT_VERSION}"
        }
        failure {
            echo '❌ Pipeline failed! Check logs above.'
        }
    }
}
