node ('Jenkins_Agent') {
   def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git 'https://github.com/Vivek1717/Helloworld.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      mvnHome = tool 'M3'
   }
   stage('Build') {
      // Run the maven build
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore clean package'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore clean package/)
         }
      }
   }
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'server/target/*.jar'
   }
   stage ('ArtifactStore') {
        if(env.PROD.toBoolean()){  
            echo "Deploying to PROD"
            sh 'scp webapp/target/*.war 10.0.0.4:/archive'
         } else {
                echo "Not deploying to PROD env"
         }
    }
    node ('Tomcat_Deploy'){
    stage ('Get_War_File'){
            sh 'scp 10.0.0.4:/archive/*.war .'
    }
    stage ('Deploy_To_Tomcat'){
        def tc8 = tomcat8(
        credentialsId:'0efe15a0-b29b-4872-aee1-8d3b4e786a10',
        path:'',
        url:'http://52.66.167.53:8080',
        contextPath:'webapp', 
        war:'**/*.war',
        password:'s3cret', 
        userName:'tomcat'
    )
       // url: 'http://52.66.167.53:8080')], contextPath: 'webapp', war: '**/*.war'
       deploy adapters: [tomcat8(credentialsId: '56f3cea2-c9cc-457f-9493-0583d06aadeb', path: '', url: 'http://52.66.167.53:8080')], contextPath: 'webapp', war: '**/*.war'
    }
    }
}