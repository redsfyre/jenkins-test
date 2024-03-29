pipeline {
  agent any
  
  stages {
    stage('Build') {
      steps {
        echo 'Building..'
      }
    }
    stage('Test') {
      steps {
        echo 'Testing..'
      }
    }
    stage('Deploy') {
      steps {
        echo 'Deploying....'
      }
    }
  }
  post {
    success {
      script {
        def WEBHOOK_URL = 'https://sub.domain.tld/endpoint'
        def PAYLOAD_COMMIT_HASH = sh(script: 'git rev-parse --short HEAD', returnStdout: true) // f7e6010ed04bfc2b06713e9fb18cf58127ff3e0e
        def PAYLOAD_BRANCH = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true) // main
        def PAYLOAD_COMMITTER_NAME = sh(script: 'git log -1 --pretty=format:"%an"|xargs', returnStdout: true) // Name Surname
        def PAYLOAD_COMMITTER_EMAIL = sh(script: 'git log -1 --pretty=format:"%ae"|xargs', returnStdout: true) // johndoe@example.com
        def PAYLOAD_COMMIT_TIMESTAMP = sh(script: 'git log -1 --pretty=format:"%at"|xargs', returnStdout: true) // 1711630044
        def PAYLOAD_COMMIT_MESSAGE = sh(script: 'git log -1 --pretty=format:"%f"|xargs', returnStdout: true) // fix: fix foo to enable bar

        def PAYLOAD = """
        {
          "commit_hash": "${PAYLOAD_COMMIT_HASH}",
          "branch": "${PAYLOAD_BRANCH}",
          "committer_name": "${PAYLOAD_COMMITTER_NAME}",
          "committer_email": "${PAYLOAD_COMMITTER_EMAIL}",
          "commit_timestamp": "${PAYLOAD_COMMIT_TIMESTAMP}",
          "commit_message": "${PAYLOAD_COMMIT_MESSAGE}"
        }
        """

        httpRequest httpMethod: 'POST', url: WEBHOOK_URL, contentType: 'application/json', body: PAYLOAD
      }
      echo 'This will run only if successful'
    }
    failure {
      echo 'This will run only if failed'
    }
    unstable {
      echo 'This will run only if the run was unstable'
    }
  }
}
