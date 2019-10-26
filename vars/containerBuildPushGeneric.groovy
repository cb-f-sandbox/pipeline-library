// vars/kanikoBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  def label = "img-gcloud-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/containerBuildPush.yml'
  podTemplate(name: 'img-gcloud', label: label, yaml: podYaml, nodeSelector: 'type=agent') {
    node(label) {
      body()
      imageNameTag()
      gitShortCommit()
      container('img-gcloud') {
        sh """
          img build --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor="${env.COMMIT_AUTHOR}" -t ${dockerReg}/${imageName}:${imageTag} ${pwd()}
          /home/user/google-cloud-sdk/bin/gcloud  auth configure-docker --quiet
          img push ${dockerReg}/${imageName}:${imageTag}
        """
      }
    }
  }
}