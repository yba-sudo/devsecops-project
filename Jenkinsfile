pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'JDK11'
    }
    
    stages {
        // Stage 1: Get code from GitHub
        stage('Checkout Git') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/yba-sudo/devsecops-project.git',
                    credentialsId: 'github-creds'
            }
        }
        
        // Stage 2: Build and test
        stage('Build & Test') {
            steps {
                dir('backend') {
                    sh 'mvn clean compile test'
                }
            }
        }
        
        // Stage 3: Package
        stage('Package') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        // Stage 4: Simple echo (we'll add Docker later)
        stage('Docker Ready') {
            steps {
                echo 'Application packaged. Ready for Docker build in next phase.'
                sh 'ls -la backend/target/*.jar'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
        }
        success {
            echo 'Success!'
        }
        failure {
            echo 'Failed!'
        }
    }
}
