podTemplate(containers: [
    containerTemplate(
        name: 'jnlp', 
        image: 'adil22/jenkins-agent-groovy:18125-v7'
        )
    ],
    volumes: [
        hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
    ]
    ) 
  {

    node(POD_LABEL) {

        def dockerimagename = "adil22/python-app:${currentBuild.number}"
        def PAT = credentials('github')
        def registryCredential = 'dockerhub'
        def scannerName = 'quality-check'
        def scannerURL = 'http://quality-sonarqube.ci.svc.cluster.local:9000'

        container('jnlp') {
            stage('Checkout Source') {
                checkoutSource()
            }

            stage('SonarQube Code Analysis') {
                dir("${WORKSPACE}"){
                    sonarqubeCheck(scannerName, scannerURL)
                }
            }

            stage('Build image') {
                buildDockerImage(dockerimagename)
            }

            stage('Pushing Image') {
                pushDockerImage(registryCredential)
            }
        }

    }
}

// Function to checkout source code
def checkoutSource() {
    withCredentials([usernamePassword(credentialsId: 'github', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
        git branch: 'main', credentialsId: 'github', url: "http://${env.PAT}@github.com/AdilNehal/Python-CI-CD.git"
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

def sonarqubeCheck(scannerName, scannerURL) {
    def scannerHome = tool name: scannerName, type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    withSonarQubeEnv('sonarqube') {
        sh "${scannerHome}/bin/sonar-scanner \
            -D sonar.projectVersion=1.0-SNAPSHOT \
            -D sonar.qualityProfile='Sonar way' \
            -D sonar.projectBaseDir=${WORKSPACE} \
            -D sonar.projectKey=python-sample-app \
            -D sonar.sourceEncoding=UTF-8 \
            -D sonar.language=python \
            -D sonar.host.url=${scannerURL}"
    }
}