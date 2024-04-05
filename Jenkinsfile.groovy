pipeline {
    agent any

    triggers {
        //GenericTrigger(
        //    genericVariables: [
        //        [key: 'REF_TYPE', value: '$.ref_type'],
        //        [key: 'PR_ACTION', value: '$.action'],
        //        [key: 'PR_OPENER', value: '$.sender.login'],
        //        [key: 'PR_ID', value: '$.pull_request.number'],
        //        [key: 'PR_TITLE', value: '$.pull_request.title'],
        //        [key: 'PR_BODY', value: '$.pull_request.body'],
        //        [key: 'PR_MERGE_COMMIT_SHA', value: '$.pull_request.merge_commit_sha'],
        //        [key: 'PR_FROM_SHA', value: '$.pull_request.base.sha'],
        //        [key: 'PR_FROM_REF', value: '$.pull_request.base.ref'],
        //        [key: 'PR_TO_SHA', value: '$.pull_request.head.sha'],
        //        [key: 'PR_TO_REF', value: '$.pull_request.head.ref'],
        //        [key: 'TAG_NAME', value: '$.ref'],
        //        [key: 'TAG_CREATOR', value: '$.sender.login'],
        //        [key: 'TAG_BRANCH', value: '$.master_branch'],
        //        [key: 'REPO_URL', value: '$.repository.clone_url']
        //    ],
        //    causeString: '#$PR_ID $PR_ACTION by $PR_OPENER',
        //    token: 'abc123',
        //    tokenCredentialId: '',
        //    printContributedVariables: true,
        //    printPostContent: false,
        //    silentResponse: false,
        //    shouldNotFlatten: false,
        //    regexpFilterText: '$REF_TYPE',
        //    regexpFilterExpression: '^(?!branch$)'
        //)

        GenericTrigger(
            genericVariables: [
                [key: 'POST_CONTENT', value: '$']

                //[key: 'REF_TYPE', value: '$.object_kind'],
                //[key: 'PR_ACTION', value: '$.object_attributes.state'],
                //[key: 'PR_OPENER', value: '$.user.username'],
                //[key: 'PR_ID', value: '$.object_attributes.iid'],
                //[key: 'PR_TITLE', value: '$.object_attributes.title'],
                //[key: 'PR_BODY', value: '$.object_attributes.description'],
                //[key: 'PR_MERGE_COMMIT_SHA', value: '$.object_attributes.merge_commit_sha'],
                //[key: 'PR_FROM_SHA', value: '$.object_attributes.source.sha'],
                //[key: 'PR_FROM_REF', value: '$.object_attributes.source.branch'],
                //[key: 'PR_TO_SHA', value: '$.object_attributes.target.sha'],
                //[key: 'PR_TO_REF', value: '$.object_attributes.target.branch'],
                //[key: 'TAG_NAME', value: '$.ref', regexpFilter: '^refs\\/tags\\/'],
                //[key: 'TAG_CREATOR', value: '$.user_username'],
                //[key: 'TAG_BRANCH', value: '$.repository.default_branch'],
                //[key: 'REPO_URL', value: '$.project.http_url']
            ],
            //causeString: '#$PR_ID $TAG_NAME $PR_ACTION $TAG_BRANCH by $PR_OPENER $TAG_CREATOR',
            causeString: '#$PR_ID $TAG_NAME $PR_ACTION $TAG_BRANCH by $PR_OPENER $TAG_CREATOR',
            token: 'abc123',
            tokenCredentialId: '',
            overrideQuietPeriod: true,
            printContributedVariables: true,
            printPostContent: true,
            silentResponse: false,
            shouldNotFlatten: false,
            regexpFilterText: '$REF_TYPE',
            regexpFilterExpression: '^(?!branch$)'
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
                        printenv
                    '''
                    def RELEASE_CREATOR = env.PR_OPENER ?: env.TAG_CREATOR ?: env.BUILD_USER_EMAIL ?: ''
                    def GIT_BRANCH = env.GIT_BRANCH ?: env.TAG_BRANCH ?: ''
                    def COMMIT_SHA = env.PR_MERGE_COMMIT_SHA ?: env.GIT_COMMIT ?: ''
                    def GIT_TAG = env.TAG_NAME ?: ''
                    def PR_ID = env.PR_ID ?: ''
                    def GIT_URL = env.GIT_URL ?: env.REPO_URL ?: ''
                    def DEPLOYED = env.DEPLOYED ?: 'false' ?: ''
                    def PAYLOAD = """
{
    "author":"$RELEASE_CREATOR",
    "branch":"$GIT_BRANCH",
    "hash":"$COMMIT_SHA",
    "tag":"$GIT_TAG",
    "pull_request":"$PR_ID",
    "url":"$GIT_URL",
    "published":"$DEPLOYED"
}
                    """
                    def NEW_PAYLOAD = """
$POST_CONTENT
                    """
                    echo "PAYLOAD: $POST_CONTENT"
                    httpRequest consoleLogResponseBody: true, contentType: 'APPLICATION_JSON', customHeaders: [[maskValue: false, name: 'jenkins-event-type', value: 'workflow-completed']], httpMode: 'POST', ignoreSslErrors: true, requestBody: NEW_PAYLOAD, responseHandle: 'NONE', url: 'https://stale-ducks-speak.loca.lt/jenkins', wrapAsMultipart: false
                    //sh "cat payload.json"
                }
            }
        }
    }
}
