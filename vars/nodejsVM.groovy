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