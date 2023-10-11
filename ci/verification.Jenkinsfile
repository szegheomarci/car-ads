pipeline {
    agent any

    tools {
        maven 'Maven - Jenkins internal'
    }

    stages {
        stage('Read project version') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    def projectVersion = pom.version
                    echo "Project version: ${projectVersion}"
                    env.projectVersion = projectVersion
                }
            }
        }
        stage('Build jar') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Build docker image') {
            steps {
                echo "docker version: ${projectVersion}-${env.BUILD_NUMBER}"
                sh "docker build -t carads:0.3-${env.BUILD_NUMBER} ."
            }
        }
        stage('Push Docker image to Nexus') {
            steps {
                script {
                    docker.withRegistry('http://192.168.0.160:9081', 'nexus-user') {
                        docker.image("carads:0.3-${env.BUILD_NUMBER}").push()
                    }
                }
            }
        }
    }
}
