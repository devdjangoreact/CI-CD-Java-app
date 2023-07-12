#!/usr/bin/env groovy

def gv

pipeline {
    agent { label 'ubuntu_java' }
    tools {
        maven 'Maven'
    }

    parameters {
        booleanParam(name: 'destroy', defaultValue: false, description: 'Destroy Terraform build?')
    }
    stages {
        stage("init") {
            steps {
                script {
                    gv = load "./script.groovy"
                }
            }
        }
        stage('Checkout') {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            steps {
                script{
                    gv.checkout()
                }
            }
        }
        stage ("increment version"){
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            steps {
                script {
                    gv.incrementVersion()
                }
            }
        }
        stage("build jar") {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            steps {
                script {
                    gv.buildJar()
                }
            }
        }
        stage("build image") {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            steps {
                script {
                    gv.buildImage()
                }
            }
        }
        stage('provision server') {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_access_key')
            }
            steps {
                script {
                    gv.provisionServer()
                }
            }
        }
        stage("deploy") {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            environment {
                DOCKER_CREDS = credentials('docker-hub')
            }
            steps {
                script {
                    gv.deployApp()
                }
            }
        }
        stage("commit version update") {
            when {
                not {
                    equals expected: true, actual: params.destroy
                }
            }
            steps {
                script {
                    gv.commitUpdateVersion()
                }
            }
        }
        stage("Destroy"){
            when {
                equals expected: true, actual: params.destroy
            }
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_access_key')
            }
            steps{
                script{
                    gv.destroy()
                }
            }
        }
    }
}
