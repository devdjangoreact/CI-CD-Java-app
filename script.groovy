def checkout() {
    scmSkip(deleteBuild: false, skipPattern:'.*\\[ci skip\\].*')
}

def incrementVersion() {
    echo 'incrementing app version...'
    sh 'mvn build-helper:parse-version versions:set \
    -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
    versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1]
    env.IMAGE_NAME = "$version-$BUILD_NUMBER"

}

def buildJar() {
    echo "building the application..."
    sh 'mvn clean package'
} 

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t farnsworth1988/java-maven-app:${IMAGE_NAME} ."
        sh 'echo $PASS| docker login -u $USER --password-stdin'
        sh "docker push farnsworth1988/java-maven-app:${IMAGE_NAME}"
    }
} 

def provisionServer() {
    dir('terraform') {
        sh "terraform init"
        sh "terraform apply --auto-approve"
        EC2_PUBLIC_IP = sh(
        script: "terraform output ec2_public_ip",
        returnStdout: true
        ).trim()
    }
}

def deployApp() {
    echo "waiting for EC2 server to initialize"
    sleep(time: 60, unit: "SECONDS")

    echo "deploying docker image to EC2"    
    echo "${EC2_PUBLIC_IP}"

    
    def shellCmd = 'bash /home/ubuntu/server-cmds.sh ${IMAGE_NAME} $DOCKER_CREDS_USR $DOCKER_CREDS_PSW'
    def ec2Instance = "ubuntu@${EC2_PUBLIC_IP}"
    sshagent(['Ubuntu']) {
        sh "scp -o StrictHostKeyChecking=no docker-compose.yaml ${ec2Instance}:/home/ubuntu"
        sh "scp -o StrictHostKeyChecking=no server-cmds.sh ${ec2Instance}:/home/ubuntu"
        sh "ssh -o StrictHostKeyChecking=no ${ec2Instance} ${shellCmd}"
    }
}

def commitUpdateVersion(){
    withCredentials([usernamePassword(credentialsId: 'gitlab-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]){
        /*sh 'git config --global user.email "nevero.anton@gmail.com"'
        sh 'git config --global user.name "Jenkins"'*/

        sh 'git remote set-url origin https://$USER:$PASS@gitlab.com/nevero.anton/my-java-maven-app.git'
        sh 'git add .'
        sh 'git commit -m "ci: version bump [ci skip]"'
        sh 'git push origin HEAD:main'

    }
}

def destroy(){
    dir('terraform') {
        sh "terraform destroy --auto-approve"
    }
}

return this
