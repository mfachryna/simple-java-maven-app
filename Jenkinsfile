# my-java-app-repo/Jenkinsfile
pipeline {
    agent any

    environment {
        # Your Docker Hub username/org for image tagging
        DOCKER_IMAGE_NAME = "your-github-username/my-java-app" # REPLACE THIS

        # These environment variables will be injected into THIS Jenkins container
        # by its parent docker-compose.yml file.
        # They define the context (Dev or Staging) for this particular Jenkins instance.
        APP_ENVIRONMENT = "" 
        APP_API_URL = ""     
        APP_DB_HOST = ""     

        # The Nginx port to access the app from your local machine, also injected from docker-compose.yml
        NGINX_EXTERNAL_PORT = ""
    }

    stages {
        stage('Checkout') {
            steps {
                # Ensure credentialsId and repository URL are correct for your GitHub repo
                git credentialsId: 'github-pat', branch: env.BRANCH_NAME, url: 'https:
            }
        }

        stage('Build Java App & Docker Image') {
            steps {
                script {
                    def imageNameWithTag = "${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}-${env.BRANCH_NAME.replace('/', '-')}"
                    echo "Building Java app and Docker image: ${imageNameWithTag}"
                    sh "mvn clean package -DskipTests" # Build the Java JAR
                    docker.build(imageNameWithTag) # Build the Docker image
                    echo "Docker image built successfully."
                }
            }
        }

        stage('Run Unit Tests (if any)') {
            steps {
                echo "Running unit tests (simulated for Java)..."
                # For real Java unit tests, you'd run Maven test here:
                # sh "mvn test"
                echo "Unit tests completed."
            }
        }

        stage('Deploy Application') {
            steps {
                script {
                    def targetContainerName = 'java-app' # Always target the 'java-app' service within THIS Docker Compose
                    def targetNetwork = 'app-network' # The network defined in THIS Docker Compose
                    def imageNameWithTag = "${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}-${env.BRANCH_NAME.replace('/', '-')}"

                    echo "Attempting to stop and remove existing application container: ${targetContainerName}"
                    sh "docker stop ${targetContainerName} || true" # Stop if running
                    sh "docker rm ${targetContainerName} || true" # Remove if exists

                    echo "Deploying ${imageNameWithTag} to its local environment (Container: ${targetContainerName})"

                    # The docker run command now uses environment variables injected into THIS Jenkins instance
                    def dockerRunCommand = "docker run -d " +
                                           "--name ${targetContainerName} " +
                                           "--network ${targetNetwork} " + # Connect to the correct network defined in THIS docker-compose.yml
                                           "-e APP_ENV=${APP_ENVIRONMENT} " +
                                           "-e API_URL=${APP_API_URL} " +
                                           "-e DB_HOST=${APP_DB_HOST} " +
                                           "${imageNameWithTag}"

                    sh dockerRunCommand
                    echo "Successfully deployed application to its local environment."
                    echo "Access your app via Nginx at: http:
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