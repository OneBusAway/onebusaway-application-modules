pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn package -Ptravis -Dmaven.javadoc.skip=true -Dvalidate.silent=true -Dlog4j.configuration= -B -V -q'
      }
    }    
  }
}
