node("launchpad-maven") {
  checkout scm
  stage("Build") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
  stage("Deploy")
}
