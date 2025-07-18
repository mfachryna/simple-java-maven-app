// Jenkinsfile for Java application CI/CD on a single Ubuntu VM with Nginx

pipeline {
    // Specifies where the pipeline will run. 'any' means on any available agent (including the master).
    agent any

    // Define environment variables used throughout the pipeline.
    // These values are accessible in all stages and steps.
    environment {
        // Maven Tool configuration (set up in Jenkins: Manage Jenkins -> Global Tool Configuration)
        // Ensure 'Maven 3.8.7' matches the name you configured for Maven.
        MVN_HOME = tool 'Maven 3.8.7'

        // JDK Tool configuration (set up in Jenkins: Manage Jenkins -> Global Tool Configuration)
        // Ensure 'JDK 17' matches the name you configured for your Java Development Kit.
        JAVA_HOME = tool 'JDK 17'

        // Application-specific variables
        APP_NAME = 'my-java-app.jar'  // The expected name of your compiled JAR file.
                                      // Adjust if your build output is different.
        DEPLOY_PATH = '/opt/java-app' // The absolute path on the Ubuntu VM where the JAR will be deployed.
        JAVA_APP_PORT = '8080'        // The port your Java application will listen on internally.
                                      // Nginx will proxy requests from port 80 to this port.
    }

    // Define tools required by the pipeline. Jenkins will ensure they are available.
    tools {
        // Links the environment variable names (MVN_HOME, JAVA_HOME) to the tool configurations.
        maven MVN_HOME
        jdk JAVA_HOME
    }

    // Define the sequence of stages in the CI/CD pipeline.
    stages {
        // Stage 1: Checkout Source Code
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out source code from SCM..."
                    // This 'git' step assumes your Jenkins job is configured with Git SCM.
                    // If not configured in the job, you might need to specify repository URL and credentials here.
                    // Example with URL and branch (replace with your repo details):
                    git branch: 'main', url: 'https://github.com/your-username/your-java-app-repo.git'
                }
            }
        }

        // Stage 2: Build the Java Application
        stage('Build') {
            steps {
                script {
                    echo "Building Java application with Maven..."
                    // Execute Maven clean and package command.
                    // '-DskipTests' is used to skip tests during the build phase for faster compilation,
                    // as tests are run in a dedicated 'Test' stage.
                    sh "${MVN_HOME}/bin/mvn clean package -DskipTests"
                }
            }
        }

        // Stage 3: Run Tests
        stage('Test') {
            steps {
                script {
                    echo "Running unit and integration tests..."
                    // Execute Maven test command.
                    sh "${MVN_HOME}/bin/mvn test"
                    // Archive JUnit test results for Jenkins UI reporting (optional but recommended).
                    // This path might vary (e.g., 'build/test-results/test/*.xml' for Gradle).
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // Stage 4: Package Artifact
        stage('Package Artifact') {
            steps {
                script {
                    echo "Packaging application JAR..."
                    // Copy the built JAR to the workspace root with a consistent name.
                    // Adjust 'target/*.jar' if your build output path/pattern is different.
                    sh "cp target/*.jar ${APP_NAME}"
                    // Archive the packaged JAR. This makes it downloadable from Jenkins.
                    archiveArtifacts artifacts: "${APP_NAME}", fingerprint: true
                }
            }
        }

        // Stage 5: Deploy Application Locally
        stage('Deploy Application Locally') {
            steps {
                script {
                    echo "Deploying application to local path: ${DEPLOY_PATH}..."

                    // Step 5.1: Stop the currently running Java application
                    echo 'Stopping existing Java application...'
                    // Use 'sudo lsof' to find the process ID (PID) listening on JAVA_APP_PORT.
                    // '|| true' prevents the step from failing if no process is found.
                    sh """
                        PID=\$(sudo lsof -t -i :${JAVA_APP_PORT} || true)
                        if [ -n \"\$PID\" ]; then
                            sudo kill \"\$PID\" # Kill the process
                            echo 'Waiting for process to terminate...'
                            sleep 5 # Give it a few seconds to shut down gracefully
                        fi
                    """

                    // Step 5.2: Remove old JAR files from the deployment directory
                    echo "Removing old JARs from ${DEPLOY_PATH}..."
                    sh "sudo rm -f ${DEPLOY_PATH}/*.jar"

                    // Step 5.3: Copy the new JAR file to the deployment directory
                    echo "Copying new JAR to ${DEPLOY_PATH}/${APP_NAME}..."
                    sh "sudo cp ${APP_NAME} ${DEPLOY_PATH}/${APP_NAME}"

                    // Step 5.4: Start the new Java application in the background
                    echo 'Starting new Java application...'
                    // 'sudo nohup java -jar ... &':
                    //   - 'sudo': Execute as root (needed for stopping/starting if not jenkins owner)
                    //   - 'nohup': Ensures the process continues running after the Jenkins job completes.
                    //   - 'java -jar': Standard command to run a Java executable JAR.
                    //   - '>${DEPLOY_PATH}/application.log 2>&1': Redirects standard output and standard error
                    //     to a log file, so you can inspect application logs later.
                    //   - '&': Runs the command in the background immediately.
                    sh """
                        sudo nohup java -jar ${DEPLOY_PATH}/${APP_NAME} > ${DEPLOY_PATH}/application.log 2>&1 &
                        echo 'Application started. Check logs at ${DEPLOY_PATH}/application.log'
                    """
                }
            }
        }

        // Stage 6: Smoke Test / Health Check (Optional but highly recommended)
        stage('Smoke Test') {
            steps {
                script {
                    echo "Performing smoke test on deployed application..."
                    // Accesses the application via Nginx (localhost), checking for HTTP 200 status.
                    // Adjust '/health' if your application has a specific health endpoint.
                    // If just the root path is sufficient, use "http://127.0.0.1/"
                    def appUrl = "http://127.0.0.1/"
                    // 'curl -f -s -o /dev/null -w '%{http_code}'': Fetches URL,
                    // suppresses output, writes only HTTP code. '-f' fails silently on error.
                    // '| grep 200': Checks if the HTTP status code 200 was returned.
                    sh "curl -f -s -o /dev/null -w '%{http_code}' ${appUrl} | grep 200"
                    echo "Smoke test successful! Application is reachable."
                }
            }
        }
    }

    // Post-build actions, executed regardless of stage success or failure.
    post {
        always {
            echo "Pipeline finished. Overall status: ${currentBuild.result}"
            cleanWs() // Cleans up the Jenkins workspace to free up disk space.
        }
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed! Please check the console output for errors.'
        }
    }
}