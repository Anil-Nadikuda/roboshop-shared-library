def call(Map configMap){
    pipeline {
        agent {
            node {
                label 'AGENT-1'
            }
        }
        environment {
            packageVersion = ''
            nexusURL = '172.31.83.22:8081'
        }
        options {
            timeout(time: 1, unit: 'HOURS')
            disableConcurrentBuilds()
            ansiColor('xterm')
        }
        parameters {
        

            booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')

            // choice(name: 'CHOICE', choices: ['One', 'Two', 'Three'], description: 'Pick something')

            // password(name: 'PASSWORD', defaultValue: 'SECRET', description: 'Enter a password')
        }
        stages {
            stage('Get the version') {
                steps {
                    script {
                        def packageJson = readJSON file: 'package.json'
                        packageVersion = packageJson.version
                        echo "application version: $packageVersion"
                    }
                }
            }

        }
                stage('Install Dependencies') {
            steps {
                 sh """
                 npm install
                 """
            }
        }
        stage('Unit tests') {
            steps {
                 sh """
                 echo "unit tests will run here"
                 """
            }
        }
        stage('Sonar scan') {
            steps {
                 sh """
                    echo "sonar-scanner" 
                 """ // just sonar-scaner if sonar server created
            }
        }
        stage('Build') {
            steps {
                sh """
                ls -la
                zip -q -r catalogue.zip ./* -x ".git" -x "*.zip"
                ls -ltr
                 """
            }
        }
        stage('publish Artifacts') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: "${nexusURL}",
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: 'catalogue',
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: 'catalogue',
                        classifier: '',
                        file: 'catalogue.zip',
                        type: 'zip']
                    ]
                )
            }
        }
        stage('Deploy') {
            when {
                expression {
                    params.Deploy = true
                }
                
            }
            steps {
                script{
                    def params = [
                        string(name: 'version',value: "$packageVersion"),
                        string(name: 'environment',value: "dev")
                    ]
                    build job: "catalogue-deploy", wait: true, parameters: params
                }
            }
        }
    }
        // post build
        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            failure { 
                echo 'this runs when pipeline is failed, used generally to send some alerts'
            }
            success{
                echo 'I will say Hello when pipeline is success'
            }
        }
    }