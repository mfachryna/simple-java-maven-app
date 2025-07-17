pipeline {
    
    agent any

    environment {
        DOCKER_IMAGE_NAME = "croazt/simple-java-maven-app"

        APP_ENVIRONMENT = sh(returnStdout: true, script: 'echo $APP_ENVIRONMENT').trim()
        APP_API_URL = sh(returnStdout: true, script: 'echo $APP_API_URL').trim()
        APP_DB_HOST = sh(returnStdout: true, script: 'echo $APP_DB_HOST').trim()
        NGINX_EXTERNAL_PORT = sh(returnStdout: true, script: 'echo $NGINX_EXTERNAL_PORT').trim()
    }

    stages {
        stage('Initialize Pipeline') {
            steps {
                script {
                    def scmInfo = checkout scm
                    if (scmInfo && scmInfo.GIT_BRANCH) {
                        env.BRANCH_NAME = scmInfo.GIT_BRANCH.replace('origin/', '')
                        echo "Initialized BRANCH_NAME: ${env.BRANCH_NAME}"
                    } else {
                        env.BRANCH_NAME = 'master'
                        echo "WARNING: Could not determine branch from SCM, defaulting to ${env.BRANCH_NAME}"
                    }
                }
            }
        }

        stage('Verification of Environment Variables') {
            steps {
                script {
                    echo "--- VERIFYING ENV VARS ---"
                    echo "APP_ENVIRONMENT: ${env.APP_ENVIRONMENT}"
                    echo "APP_API_URL: ${env.APP_API_URL}"
                    echo "APP_DB_HOST: ${env.APP_DB_HOST}"
                    echo "NGINX_EXTERNAL_PORT: ${env.NGINX_EXTERNAL_PORT}"
                    echo "--------------------------"
                    echo "VERIFYING BRANCH_NAME: ${env.BRANCH_NAME}"
                }
            }
        }

        stage('Build Java App & Docker Image') {
            
            agent {
                docker {
                    image 'maven:3.9.10-eclipse-temurin-17-noble' 
                    args '-u root' 
                }
            }
            steps {
                script {
                    def imageNameWithTag = "${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}-${env.BRANCH_NAME.replace('/', '-')}"
                    echo "Building Java app and Docker image: ${imageNameWithTag}"

                    
                    sh "mvn clean package -DskipTests"
                    
                    
                    docker.build(imageNameWithTag)
                    echo "Docker image built successfully."
                }
            }
        }

        stage('Run Unit Tests (if any)') {
            
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-17-alpine'
                    args '-u root'
                }
            }
            steps {
                echo "Running unit tests for Java..."
                sh "mvn test" 
                echo "Unit tests completed."
            }
        }

        stage('Deploy Application') {
            
            agent any 
            steps {
                script {
                    def targetContainerName = 'java-app'
                    def targetNetwork = 'app-network'
                    def imageNameWithTag = "${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}-${env.BRANCH_NAME.replace('/', '-')}"

                    echo "Attempting to stop and remove existing application container: ${targetContainerName}"
                    sh "docker stop ${targetContainerName} || true"
                    sh "docker rm ${targetContainerName} || true"

                    echo "Deploying ${imageNameWithTag} to its local environment (Container: ${targetContainerName})"

                    def dockerRunCommand = "docker run -d " +
                                           "--name ${targetContainerName} " +
                                           "--network ${targetNetwork} " +
                                           "-e APP_ENV=${env.APP_ENVIRONMENT} " +
                                           "-e API_URL=${env.APP_API_URL} " +
                                           "-e DB_HOST=${env.APP_DB_HOST} " +
                                           "-p ${env.NGINX_EXTERNAL_PORT}:${env.NGINX_EXTERNAL_PORT} " +
                                           "${imageNameWithTag}"

                    sh dockerRunCommand
                    echo "Successfully deployed application to its local environment."
                    echo "Access your app via Nginx at: http://localhost:${env.NGINX_EXTERNAL_PORT}/"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully for branch ${env.BRANCH_NAME}!"
        }
        failure {
            echo "Pipeline failed for branch ${env.BRANCH_NAME}!"
        }
    }
}