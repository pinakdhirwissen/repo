#!/usr/bin/env groovy
pipeline {
    agent any

     options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    tools {
        gradle 'Gradle'
        jdk 'JDK17'
    }

    environment {

        DOCKER_IMAGE = 'pinakdhir/wt-ticketing-service'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
    }    
    
    stages {

        stage('Validate') {
    steps {
        script {
            echo "Workspace: ${WORKSPACE}"
            echo "Current directory: ${pwd()}"
            
            if (!fileExists('build.gradle')) {
                error 'build.gradle not found. Pipeline cannot continue.'
            }
            
            if (!fileExists('Dockerfile')) {
                error 'Dockerfile not found. Pipeline cannot continue.'
            }
        }
    }
}

        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/pinakdhirwissen/repo'
            }
        }

        stage('Build') {
            steps {
                    echo 'Building project with Gradle...'
                    bat 'gradlew clean build -x test'
            }
        }

        stage('Test') {
            steps {
                    echo 'Running unit tests...'
                    bat 'gradlew test'
            }
        }

        stage('Build Docker Image') {
            steps {
                    script {
                        bat 'docker version'
                        bat 'docker info'
                        bat "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                        bat "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                    }
            }        
            
        }
        
        stage('Push to DockerHub') {
            steps {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', 
                                                        usernameVariable: 'DOCKER_USERNAME', 
                                                        passwordVariable: 'DOCKER_PASSWORD')]) {
                            bat 'echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin'
                            bat "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                            bat "docker push ${DOCKER_IMAGE}:latest"
                            bat 'docker logout'
                        }
                    }
            }
        }
        
        stage('Verify Setup') {
    steps {
        script {
            echo "JDK Version:"
            bat 'java -version'
            echo "Gradle Version:"
            bat 'gradlew -v'
        }
    }
}

    
    
    }

    post {
        always {
            cleanWs()
            script {
                // Clean up Docker images
                bat "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
                bat "docker rmi ${DOCKER_IMAGE}:latest || true"
            }
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
