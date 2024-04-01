pipeline {
    agent any

    triggers {
        GenericTrigger token: 'abc123'
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
                    sh "printenv"
                }
            }
        }
    }
}
