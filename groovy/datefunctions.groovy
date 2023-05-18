def getDaysUntilThreshold(daysThreshold) {
  def timestamp = sh(script: "date '+%Y-%m-%d %H:%M:%S'", returnStdout: true).trim()
  def epochTimestamp = sh(script: "date +%s -d '${timestamp}'", returnStdout: true).trim().toInteger()
  def currentTime = sh(script: "date +%s", returnStdout: true).trim() as Long
  def thresholdTime = currentTime + (daysThreshold * 24 * 60 * 60) // Convert days to seconds
 
  def remainingSeconds = thresholdTime - epochTimestamp
  def remainingDays = (remainingSeconds / (24 * 60 * 60)) as Integer
 
  return remainingDays
}

def getRemainingDays(daysThreshold, storedDate) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
 
  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    return env.REMAINING_DAYS
  } else {
    // Calculate the remaining days based on the new threshold and store it
    def remainingDays = getDaysUntilThreshold(daysThreshold)
    env.REMAINING_DAYS = remainingDays
    env.EMAIL_SENT_DATE = currentDate
    return remainingDays
  }
}

// Your existing code
stage('Process Violations') {
  steps {
    script {
      // Create copy of txt file for editing
      sh "cp ${WORKSPACE}/src/main/resources/ViolationReportTemplate.html ViolationReport.html"  

      // Retrieve credentials and set environment variables
      withCredentials([usernamePassword(credentialsId: PROXY_CREDENTIALS_ID, passwordVariable: 'PROXY_PASS', usernameVariable: 'PROXY_USER')]) {
        def encodedPassword = URLEncoder.encode(PROXY_PASS, "UTF-8")
        withEnv(awsEnv + "https_proxy=http://${PROXY_USER}:${encodedPassword}@${PROXY_ADDRESS}") {
          // ... rest of your code ...

          // Call getRemainingDays function passing the daysThreshold and storedDate
          def remainingDays = getRemainingDays(daysThreshold, storedDate)

          // Use the calculated remainingDays value in your code as needed
          // For example:
          if (remainingDays > 0) {
            println "There are ${remainingDays} days remaining until the threshold."
          } else {
            println "The threshold has been reached."
          }

          // ... rest of your code ...
        }
      }
    }
  }
}

