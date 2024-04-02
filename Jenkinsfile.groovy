pipeline {
    agent any

    triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'PR_ACTION', value: '$.action'],
                [key: 'PR_OPENER', value: '$.pull_request.user.login'],
                [key: 'PR_ID', value: '$.pull_request.number'],
                [key: 'PR_TITLE', value: '$.pull_request.title'],
                [key: 'PR_BODY', value: '$.pull_request.body'],
                [key: 'PR_MERGE_COMMIT_SHA', value: '$.pull_request.merge_commit_sha'],
                [key: 'PR_FROM_SHA', value: '$.pull_request.base.sha'],
                [key: 'PR_FROM_REF', value: '$.pull_request.base.ref'],
                [key: 'PR_TO_SHA', value: '$.pull_request.head.sha'],
                [key: 'PR_TO_REF', value: '$.pull_request.head.ref'],
            ],
            causeString: '#$PR_ID $PR_ACTION by $PR_OPENER',
            token: 'abc123',
            tokenCredentialId: '',
            printContributedVariables: true,
            printPostContent: true,
            silentResponse: false,
            shouldNotFlatten: false,
            regexpFilterText: '',
            regexpFilterExpression: ''
        )
    }

    parameters {
        string(name: 'DEPLOYED', defaultValue: 'true', description: 'Do you want to deploy?')
    }
//    parameters {
        // Parameter that allows us to choose which branch to build
//        gitParameter(branch: '',
//                //branchFilter: 'origin/(release.*)',              // Jenkins will detect and list only release/* branches
//                branchFilter: 'origin/(.*)',                       // Jenkins will detect and list all branches
//                defaultValue: 'release/1.0.0',                      // If we set this value main or master, Jenkins triggers build that branches too
//                description: 'Select branch to build',
//                name: 'github_branch',                      // If everything goes well, this variable will hold something like: release/3.12.3-jenkins
//                quickFilterEnabled: true,                          // It lists the existing branches faster, but it can cause new branches not to be listed.
//                listSize: '20',
//                selectedValue: 'DEFAULT',                          // Default selected value
//                sortMode: 'DESCENDING_SMART',                      // Branch list order by
//                tagFilter: '*',                                    // We don't use this settings but it lists all tags with this situation
//                type: 'PT_BRANCH',                                 // Jenkins will search and detect "branches". It can be search for tags
//                useRepository: 'https://github.com/redsfyre/jenkins-test.git'
//        )

//          gitParameter
//            branch: '',
//            branchFilter: '.*',
//            defaultValue: '1',
//            description: 'git_pr_builder',
//            name: 'git_pr',
//            quickFilterEnabled: true,
//            selectedValue: 'TOP',
//            sortMode: 'DESCENDING_SMART',
//            tagFilter: '*',
//            type: 'PT_PULL_REQUEST',
//            useRepository: 'git@github.com:redsfyre/jenkins-test.git'
//    }
 
    stages {
        stage ('SCM') {
            steps {
                echo "Building ${env.GitHub_Target_Branch}"
                checkout poll: true, scm: [
                    $class: 'GitSCM',
                    // only build release/* branches
                    branches: [[name: '**']],
                    extensions: [],
                    userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/redsfyre/jenkins-test.git']]]
            }
        }

        stage('env-vars') {
            steps {
                script {
                    sh '''
                        echo Variables from shell:
                        #echo PR_ACTION = $PR_ACTION
                        #echo PR_OPENER = $PR_OPENER
                        #echo PR_ID = $PR_ID
                        #echo PR_TITLE = $PR_TITLE
                        #echo PR_BODY = $PR_BODY
                        #echo PR_MERGE_COMMIT_SHA = $PR_MERGE_COMMIT_SHA
                        #echo PR_FROM_SHA = $PR_FROM_SHA
                        #echo PR_FROM_REF = $PR_FROM_REF
                        #echo PR_TO_SHA = $PR_TO_SHA
                        #echo PR_TO_REF = $PR_TO_REF
                        #echo BUILD_USER_EMAIL = $BUILD_USER_EMAIL
                        #echo GIT_BRANCH = $GIT_BRANCH
                        #echo DEPLOYED = $DEPLOYED
                        #export GIT_TAG="${GIT_TAG:-null}"
                        printenv
                    '''
                    def GIT_TAG = env.GIT_TAG ?: 'null'
                    def PAYLOAD = '{"author":"$PR_OPENER", "branch":"$GIT_BRANCH", "hash":"$GIT_COMMIT", "tag":"$GIT_TAG", "pull_request":"$PR_ID", "url":"$GIT_URL", "published":"$DEPLOYED"}'
                    writeJSON(file: 'payload.json', json: PAYLOAD)
                    def read = readJSON file: 'payload.json'
                    echo "Payload: ${read}"
                    //sh "echo $PAYLOAD > payload.json"
                    //sh "cat payload.json"
                }
            }
        }
    }
}
