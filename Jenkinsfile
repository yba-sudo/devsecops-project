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
        SONAR_CREDS = credentials('sonarqube-creds')  // Creates SONAR_CREDS_USR and SONAR_CREDS_PSW
        SONAR_PROJECT_KEY = "devsecops-backend"
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
                    // Wait a bit for SonarQube to process the analysis
                    sleep 10
                    
                    // Try multiple times (SonarQube might take time to process)
                    def maxAttempts = 12  // 12 attempts * 5 seconds = 1 minute max wait
                    def attempt = 1
                    def qualityGateStatus = ""
                    
                    while (attempt <= maxAttempts) {
                        echo "Checking Quality Gate status (Attempt ${attempt}/${maxAttempts})..."
                        
                        try {
                            // Call SonarQube API to get Quality Gate status
                            def response = sh(
                                script: """
                                    curl -s -u ${SONAR_CREDS_USR}:${SONAR_CREDS_PSW} \
                                    "${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}"
                                """,
                                returnStdout: true
                            ).trim()
                            
                            echo "API Response: ${response}"
                            
                            // Parse JSON response (requires jq installed)
                            qualityGateStatus = sh(
                                script: """
                                    echo '${response}' | jq -r '.projectStatus.status'
                                """,
                                returnStdout: true
                            ).trim()
                            
                            echo "Quality Gate Status: ${qualityGateStatus}"
                            
                            if (qualityGateStatus == "OK") {
                                echo "✅ Quality Gate PASSED!"
                                break
                            } else if (qualityGateStatus == "ERROR") {
                                error "❌ Quality Gate FAILED! Check SonarQube for details."
                            }
                            // If status is empty or not ready, continue waiting
                            
                        } catch (Exception e) {
                            echo "Attempt ${attempt} failed: ${e.getMessage()}"
                        }
                        
                        sleep 5  // Wait 5 seconds before next attempt
                        attempt++
                    }
                    
                    if (qualityGateStatus != "OK") {
                        if (qualityGateStatus == "ERROR") {
                            error "❌ Quality Gate check failed after ${maxAttempts} attempts"
                        } else {
                            error "❌ Could not determine Quality Gate status after ${maxAttempts} attempts"
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Run with Docker Compose') {
            steps {
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
            sh 'docker-compose down || true'
            junit 'backend/target/surefire-reports/*.xml'
        }
        success {
            echo '🎉 Pipeline completed successfully! All tests passed and Quality Gate approved.'
        }
        failure {
            echo '❌ Pipeline failed! Check the logs above.'
        }
    }
}
