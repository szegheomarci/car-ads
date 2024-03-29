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
                    def artifactId = pom.artifactId
                    def projectVersion = pom.version
                    //            must use /bin/bash, set Manage Jenkins --> Configure System --> Shell executable
                    def dropCount = sh(script: '''
                        IFS="-" read -ra version <<< $(git describe --tags)
                        expr ${version[2]} + ${version[3]}
                        ''', returnStdout: true).trim()
                    env.buildVersion = artifactId + "-v" + projectVersion + "-" + dropCount
                    echo "Build version: ${env.buildVersion}"
                    env.dockerId = "szegheomarci/carads:" + projectVersion + "-" + dropCount
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
        stage('Push Docker image to repository') {
            steps {
                script {
                    docker.withRegistry('https://ghcr.io/', 'szegheomarci-github') {
                        docker.image("${env.dockerId}").push()
                    }
                }
            }
        }
        stage('Tag on Success') {
            when {
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
            steps {
                script {
                    sh("git config user.name 'Jenkins'")
                    sh("git config user.email 'jenkins@szegheomarci.com'")

                    // Tag the commit
                    sh "git tag -a ${env.buildVersion} -m 'Version ${env.buildVersion}'"

                    // Push the tag to the remote repository
                    sshagent(['gerrit_user']) {
                        sh("git push origin ${env.buildVersion}")
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                cleanWs()
                def isContainerValid = sh(script: "docker ps -a | grep '${env.dockerId}'", returnStatus: true) == 0
                if (isContainerValid) {
                    def isContainerRunning = sh(script: "docker inspect -f {{.State.Running}} ${env.dockerId}", returnStatus: true) == 0
                    // Stop the Docker container if it's running
                    if (isContainerRunning) {
                        echo "Stopping ${env.dockerId} container"
                        sh "docker ps -q --filter ancestor=${env.dockerId} | xargs docker stop"
                    }

                    // Remove the Docker container
                    echo "Deleting ${env.dockerId} container"
                    sh "docker ps -a | grep '${env.dockerId}' | awk '{print \$1}' | xargs docker rm -f"
                }
                // Remove the Docker image
                echo "Deleting ${env.dockerId} image"
                sh "docker images | grep \$(echo '${env.dockerId}' | sed 's|:|\\\\\\s*|') | awk '{print \$3}' | xargs docker rmi -f"
            }
        }
    }
}
