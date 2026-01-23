pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'JDK11'
    }
    
    environment {
        DOCKER_IMAGE = "devsecops-backend"
        DOCKER_TAG = "${env.BUILD_ID}"
    }
    
    stages {
        stage('Checkout Git') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/yba-sudo/devsecops-project.git',
                    credentialsId: 'github-creds'
            }
        }
        
        stage('Build & Test') {
            steps {
                dir('backend') {
                    sh 'mvn clean compile test'
                }
            }
        }
        
        stage('Package') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                    sh 'ls -la target/*.jar'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    // Build Docker image
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    
                    // List images to verify
                    sh 'docker images | grep ${DOCKER_IMAGE}'
                }
            }
        }
        
        stage('Run with Docker Compose') {
            steps {
                sh 'docker-compose down || true'
                sh 'docker-compose up -d --build'
                sleep 30  // Wait for containers to start
            }
        }
        
        stage('Integration Test') {
            steps {
                sh '''
                    echo "Testing deployed application..."
                    curl -f http://localhost:8088/api/health || exit 1
                    curl -f http://localhost:8088/api/hello || exit 1
                    echo "All tests passed!"
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up Docker containers...'
            sh 'docker-compose down || true'
            sh 'docker image prune -f || true'
        }
        success {
            echo '✅ Pipeline SUCCESS! Docker image built and deployed.'
            sh 'docker images | grep ${DOCKER_IMAGE}'
        }
        failure {
            echo '❌ Pipeline FAILED!'
        }
    }
}
