pipeline {
  agent any

  environment {
    ANDROID_HOME = "${HOME}/Library/Android/sdk"
    PATH = "${env.PATH}:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:/opt/homebrew/bin:/usr/local/bin"
    FIREBASE_TOKEN = credentials('FIREBASE_TOKEN')
    FIREBASE_APP_ID = credentials('FIREBASE_APP_ID')
  }

  parameters {
    choice(name: 'BUILD_TYPE', choices: ['release', 'debug'], description: 'Build type')
    choice(name: 'ANDROID_TYPE', choices: ['APK', 'AAB'], description: 'Build output type')
    booleanParam(name: 'GENERATE_BASELINE_PROFILE', defaultValue: false, description: 'Generate Baseline Profile?')
    string(name: 'RELEASE_NOTES', defaultValue: 'Release notes here', description: 'Release notes text')
    string(name: 'FIREBASE_GROUPS', defaultValue: 'internal-testers', description: 'Firebase distribution groups')
  }

  stages {
    stage('Setup') {
      steps {
        echo "Setup environment"
        sh '''
          echo "sdk.dir=$ANDROID_HOME" > local.properties
          chmod +x ./gradlew
        '''
      }
    }

    stage('Generate Baseline Profile') {
      when {
        expression { params.GENERATE_BASELINE_PROFILE }
      }
      steps {
        echo "Generating baseline profile"
        sh './gradlew generateBaselineProfileFull'
      }
    }

    stage('Build') {
      steps {
        script {
          def variant = params.BUILD_TYPE == 'release' ? 'Release' : 'Debug'
          if (params.ANDROID_TYPE == 'AAB') {
            sh "./gradlew clean bundle${variant}"
          } else {
            sh "./gradlew clean assemble${variant}"
          }
        }
      }
    }

    stage('List Outputs') {
      steps {
        echo "Listing build output files:"
        sh "find app/build/outputs -type f"
      }
    }

    stage('Upload to Firebase') {
      steps {
        script {
          def outputPath = params.ANDROID_TYPE == 'AAB'
            ? "app/build/outputs/bundle/${params.BUILD_TYPE}/app-${params.BUILD_TYPE}.aab"
            : "app/build/outputs/apk/${params.BUILD_TYPE}/app-${params.BUILD_TYPE}.apk"

          sh """
            echo "${params.RELEASE_NOTES}" > release-notes.txt

            if [ -f "${outputPath}" ]; then
              echo "Uploading to Firebase App Distribution..."
              if firebase appdistribution:distribute "${outputPath}" \\
                --app "$FIREBASE_APP_ID" \\
                --token "$FIREBASE_TOKEN" \\
                --groups "${params.FIREBASE_GROUPS}" \\
                --release-notes-file release-notes.txt; then
                echo "Upload success"
              else
                echo "Upload failed, copying build output to output/ folder"
                mkdir -p output
                cp "${outputPath}" output/
              fi
            else
              echo "ERROR: Build output file not found at ${outputPath}"
              exit 1
            fi
          """
        }
      }
    }
  }
}
