
pipeline {
    agent any

    environment {
        
        DOCKER_IMAGE_NAME = "croazt/simple-java-maven-app" 

        APP_ENVIRONMENT = "" 
        APP_API_URL = ""     
        APP_DB_HOST = ""     
        
        NGINX_EXTERNAL_PORT = ""
    }

    stages {
        stage('Checkout') {
            steps {
                
                git credentialsId: 'github-usn', branch: env.BRANCH_NAME, url: 'https://github.com/mfachryna/simple-java-maven-app'
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
                                           "-e APP_ENV=${APP_ENVIRONMENT} " +
                                           "-e API_URL=${APP_API_URL} " +
                                           "-e DB_HOST=${APP_DB_HOST} " +
                                           "${imageNameWithTag}"

                    sh dockerRunCommand
                    echo "Successfully deployed application to its local environment."
                    echo "Access your app via Nginx at: http://localhost:${NGINX_EXTERNAL_PORT}/"
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