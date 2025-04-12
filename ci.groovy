podTemplate(containers: [
    containerTemplate(
        name: 'jnlp', 
        image: 'adil22/jenkins-agent-groovy:18125-v11'
        )
    ],
    volumes: [
        hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
    ]
    ) 
    {

    node(POD_LABEL) {
        //set github webhook before setting this
        // properties([pipelineTriggers([[$class: 'GitHubPushTrigger'], pollSCM('H/15 * * * *')])])

        def dockerimagename = "adil22/python-app:${currentBuild.number}"
        def PAT = credentials('github')
        def registryCredential = 'dockerhub'
        def scannerName = 'sonarqube'
        def scannerURL = 'http://quality-sonarqube.ci.svc.cluster.local:9000'

        container('jnlp') {
            stage('Checkout Source') {
                checkoutSource(PAT)
            }

            stage('SonarQube Code Analysis') {
                dir("${WORKSPACE}"){
                    sonarqubeCheck(scannerName, scannerURL)
                }
            }

            stage('Build image') {
                buildDockerImage(dockerimagename)
            }

            stage('Trivy Scan Image') {
                trivyScanImage(dockerimagename)
            }

            stage('Pushing Image') {
                pushDockerImage(registryCredential)
            }
            
            stage('Updating the Helm Chart') {
                setImageTagInHelmChart(PAT,"${currentBuild.number}")
            }
        }

    }
}

// Function to checkout source code
def checkoutSource(PAT) {
    withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
        git branch: 'main', credentialsId: 'github', url: "http://${PAT}@github.com/AdilNehal/Python-CI-CD.git"
    }
}

// Function to build the Docker image
def buildDockerImage(dockerimagename) {
    dockerImage = docker.build(dockerimagename)
}

// Function to push the Docker image to DockerHub
def pushDockerImage(registryCredential) {
    docker.withRegistry('https://registry.hub.docker.com', registryCredential) {
        dockerImage.push("${currentBuild.number}")
    }
}

// Function to run SonarQube code analysis
def sonarqubeCheck(scannerName, scannerURL) {
    def scannerHome = tool name: scannerName, type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    withSonarQubeEnv('sonarqube') {
        sh "${scannerHome}/bin/sonar-scanner \
            -D sonar.projectVersion=1.0-SNAPSHOT \
            -D sonar.qualityProfile='Sonar way' \
            -D sonar.projectBaseDir=${WORKSPACE} \
            -D sonar.projectKey=python-app \
            -D sonar.sourceEncoding=UTF-8 \
            -D sonar.language=python \
            -D sonar.host.url=${scannerURL}"
    }
}

// Function to run Trivy scan on the Docker image
def trivyScanImage(dockerimagename) {
    def trivyOutput = sh(script: "trivy image ${dockerimagename}", returnStdout: true).trim()
    println trivyOutput
    if (trivyOutput.contains("Total: 0")) {
        echo "No vulnerabilities found in the Docker image."
    }
    else {
        error "Vulnerabilities found in the Docker image."
        // You can take further actions here based on your requirements
        // For example, failing the build if vulnerabilities are found
        // error "Vulnerabilities found in the Docker image."
    }
}

// Function to update the Helm chart with the new Docker image tag
def setImageTagInHelmChart(PAT, imageTag) {
    withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
        sh """
            git clone https://$GITHUB_USERNAME:$GITHUB_TOKEN@github.com/AdilNehal/Python-CI-CD.git
            cd Python-CI-CD/helm-charts-deployment/python-app
            git config --global user.email ci-bot@argocd.com && git config --global user.name ci-bot
            sed -i "s|tag:.*|tag: \\\"$imageTag\\\"|" values.yaml
            git add -A
            git commit -m "Updated the Docker image tag: $imageTag in Helm chart"
            git push origin main
        """
    }
}
