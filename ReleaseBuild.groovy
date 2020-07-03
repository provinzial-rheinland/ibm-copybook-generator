node('S00260411UV || S00260402UV') {
    timestamps() {
        try {
            stage('checkout') {
                checkout scm
            }
            stage('build') {
                withMaven(jdk: 'openJDK 11',  maven: 'Maven 3.5.4', options: [artifactsPublisher(disabled: true)]) {
                    def gitref = params["kontext.version"]
                    def mashupServerURL = params["mashup.server"]
                    def mashupThreadId = params["mashup.thread.id"]
                    mashupBuildStarted itemId: mashupThreadId, version: gitref
                    sh "mvn -B -V -U -e clean versions:set -DnewVersion=$gitref -P release-build,jenkins-build -U"
                    sh 'mvn -B -V -U -e -P release-build,jenkins-build clean deploy'
                    mashupBuildSuccess itemId: params["mashup.thread.id"], packagingType: 'JAR', version: params["kontext.version"], sonarUrl: "", deployArtifact: ""
                }
            }
        } catch(e) {
            currentBuild.result = 'FAILURE'
            println e
            mashupBuildFailed itemId: params["mashup.thread.id"]
        }

    }
}
