
pipeline {
    agent any

    environment {
        MVN_HOME = '/opt/maven'
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-arm64'

        APP_NAME = 'spring-hello-world-app-0.0.1-SNAPSHOT.jar'

        DEPLOY_PATH = '/opt/java-app'
        JAVA_APP_PORT = '8080'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from SCM defined in job configuration..."
                    checkout scm 
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo 'Building Java application with Maven...'

                    sh "${MVN_HOME}/bin/mvn clean package -DskipTests"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo 'Running unit and integration tests...'

                    sh "${MVN_HOME}/bin/mvn test"

                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package Artifact') {
            steps {
                script {
                    echo 'Packaging application JAR...'

                    sh "cp target/*.jar ${APP_NAME}"

                    archiveArtifacts artifacts: "${APP_NAME}", fingerprint: true
                }
            }
        }

        stage('Deploy Application Locally') {
            steps {
                script {
                    echo "Deploying application to local path: ${DEPLOY_PATH}..."

                    echo 'Stopping existing Java application...'

                    sh """
                        PID=\$(sudo lsof -t -i :${JAVA_APP_PORT} || true)
                        if [ -n \"\$PID\" ]; then
                            sudo kill \"\$PID\" # Kill the process
                            echo 'Waiting for process to terminate...'
                            sleep 5 # Give it a few seconds to shut down gracefully
                        fi
                    """

                    echo "Removing old JARs from ${DEPLOY_PATH}..."
                    sh "sudo rm -f ${DEPLOY_PATH}/*.jar"

                    echo "Copying new JAR to ${DEPLOY_PATH}/${APP_NAME}..."
                    sh "sudo cp ${APP_NAME} ${DEPLOY_PATH}/${APP_NAME}"

                    echo 'Starting new Java application...'

                    sh """
                        sudo nohup "${JAVA_HOME}/bin/java" -jar ${DEPLOY_PATH}/${APP_NAME} > ${DEPLOY_PATH}/application.log 2>&1 &
                        echo 'Application started. Check logs at ${DEPLOY_PATH}/application.log'
                    """
                }
            }
        }

        stage('Smoke Test') {
            steps {
                script {
                    echo 'Performing smoke test on deployed application...'

                    def appUrl = 'http://127.0.0.1/'

                    sh "curl -f -s -o /dev/null -w '%{http_code}' ${appUrl} | grep 200"
                    echo 'Smoke test successful! Application is reachable.'
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Overall status: ${currentBuild.result}"
            cleanWs()
        }
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed! Please check the console output for errors.'
        }
    }
}
