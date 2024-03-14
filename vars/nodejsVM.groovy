def call(Map configMap){
    pipeline {
        agent {
            node {
                label 'AGENT-1'
            }
        }
        environment {
            packageVersion = ''
            // nexusURL = '172.31.83.22:8081' mentioned in pipelineGlobal

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
                    zip -q -r ${configMap.component}.zip ./* -x ".git" -x "*.zip"
                    ls -ltr
                    """
                }
            }
            stage('publish Artifacts') {
                steps {
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: pipelineGlobal.nexusURL(),
                        groupId: 'com.roboshop',
                        version: "${packageVersion}",
                        repository: "${configMap.component}",
                        credentialsId: 'nexus-auth',
                        artifacts: [
                            [artifactId: "${configMap.component}",
                            classifier: '',
                            file: "${configMap.component}.zip",
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
                        build job: "../${configMap.component}-deploy", wait: true, parameters: params
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
}