pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'JDK11'
        // CORRECT tool name for SonarQube Scanner
        sonarRunner 'SonarScanner'
    }
    
    environment {
        DOCKER_IMAGE = "devsecops-backend"
        DOCKER_TAG = "${env.BUILD_ID}"
        SONAR_HOST_URL = "http://192.168.56.10:9000"
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
                    // Generate JaCoCo report
                    sh 'mvn jacoco:report'
                }
            }
            post {
                success {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh "mvn sonar:sonar \
                            -Dsonar.projectKey=devsecops-backend \
                            -Dsonar.projectName='DevSecOps Backend' \
                            -Dsonar.host.url=${SONAR_HOST_URL}"
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                    echo "Testing deployed application..."
                    curl -f http://localhost:8088/api/health || exit 1
                    curl -f http://localhost:8088/api/hello || exit 1
                    curl -f "http://localhost:8088/api/greet?name=Jenkins" || exit 1
                    echo "All tests passed!"
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up Docker containers...'
            sh 'docker-compose down || true'
            
            // Archive test results
            junit 'backend/target/surefire-reports/*.xml'
        }
        success {
            echo '✅ Pipeline SUCCESS! Code quality analyzed with SonarQube.'
        }
        failure {
            echo '❌ Pipeline FAILED!'
        }
    }
}
