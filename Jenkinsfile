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
                    withSonarQubeEnv('SonarQube') {
                        // withSonarQubeEnv injects sonar.login automatically
                        sh '''
                            mvn sonar:sonar \
                            -Dsonar.projectKey=devsecops-backend \
                            -Dsonar.projectName="DevSecOps Backend" \
                            -Dsonar.host.url=${SONAR_HOST_URL}
                        '''
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
    }
}
