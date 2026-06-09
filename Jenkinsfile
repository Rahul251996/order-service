pipeline {

    agent any

    environment {
        IMAGE_NAME = "order-service"
        IMAGE_TAG = "latest"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker rm -f order-service || true

                docker run -d \
                  --name order-service \
                  --network quickshop_app-net \
                  -p 8081:8081 \
                  ${IMAGE_NAME}:${IMAGE_TAG}
                '''
            }
        }
    }

    post {

        success {
            echo 'Order Service deployed successfully'
        }

        failure {
            echo 'Deployment failed'
        }
    }
}