podTemplate(containers: [
    containerTemplate(
        name: 'jnlp', 
        image: 'adil22/jenkins-agent-groovy:18125-v5'
        )
  ]) {

    node(POD_LABEL) {

        def dockerimagename = "adil22/python-app:${currentBuild.number}"
        def PAT = credentials('github')
        def registryCredential = 'dockerhub'

        container('jnlp') {
            stage('Checkout Source') {
                checkoutSource()
            }

            stage('Build image') {
                buildDockerImage(dockerimagename)
            }

            stage('Pushing Image') {
                pushDockerImage(dockerimagename)
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
def pushDockerImage(dockerimagename) {
    docker.withRegistry('https://registry.hub.docker.com', env.registryCredential) {
        dockerImage.push(dockerimagename)
    }
}