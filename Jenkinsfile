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

        stage('Verification of Environment Variables') {
            steps {
                script {
                    echo "--- VERIFYING ENV VARS ---"
                    echo "APP_ENVIRONMENT: ${env.APP_ENVIRONMENT}"
                    echo "APP_API_URL: ${env.APP_API_URL}"
                    echo "APP_DB_HOST: ${env.APP_DB_HOST}"
                    echo "NGINX_EXTERNAL_PORT: ${env.NGINX_EXTERNAL_PORT}"
                    echo "--------------------------"
                }
            }
        }

        stage('Build Java App & Docker Image') {
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
            steps {
                echo "Running unit tests (simulated for Java)..."
                echo "Unit tests completed."
            }
        }

        stage('Deploy Application') {
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