pipeline {
    agent any

    tools {
        maven 'Maven - Jenkins internal'
    }

    stages {
        stage('Init') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    def projectVersion = pom.version
                    echo "Project version: ${projectVersion}"
                    env.projectVersion = projectVersion
                    env.dockerId = "szegheomarci/carads:" + projectVersion + "-" + env.BUILD_NUMBER

                    def artifactId = pom.artifactId
                    def dropCount = sh(script: '''
                        #!/bin/bash
                        IFS="-" read -ra version <<< $(git describe --tags)
                        expr ${version[2]} + ${version[3]}
                        ''', returnStdout: true).trim()
                    def buildVersion = artifactId + "-v" + projectVersion + dropCount
                    echo "Build version: ${buildVersion}"
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
                sh "docker build -t ${env.dockerId} ."
            }
        }
    }
    post {
        always {
            script {
                cleanWs()
                // Check if the Docker container is running
                def isContainerRunning = sh(script: "docker inspect -f {{.State.Running}} ${env.dockerId}", returnStatus: true) == 0

                // Stop the Docker container if it's running
                if (isContainerRunning) {
                    echo "Stopping ${env.dockerId} container"
                    sh "docker ps -q --filter ancestor=${env.dockerId} | xargs docker stop"
                }
                /*// Remove the Docker container
                echo "Deleting ${env.dockerId} container"
                sh "docker ps -a | grep '${env.dockerId}' | awk '{print \$1}' | xargs docker rm"*/
                // Remove the Docker image
                echo "Deleting ${env.dockerId} image"
                sh "docker images | grep \$(echo '${env.dockerId}' | sed 's|:|\\\\\\s*|') | awk '{print \$3}' | xargs docker rmi -f"
            }
        }
    }
}
