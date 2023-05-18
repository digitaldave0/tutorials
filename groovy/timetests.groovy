def getRemainingDays(daysThreshold, storedDate) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
  def s3Path = "${DEPLOYMENT_ACCOUNT}-${AWS_DEFAULT_REGION}-${ARCHIVE_BUCKET_NAME}/vigilante/remaining_days.txt"

  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    def remainingDays = sh(script: "aws s3 cp s3://${s3Path} -", returnStdout: true).trim().toInteger()
    if (remainingDays > 0) {
      remainingDays -= 1
      sh(script: "aws s3 cp --acl=private --sse AES256 --content-type=text/plain --content-length=${remainingDays.toString().length()} --body=- s3://${s3Path} -", stdin: remainingDays.toString())
    }
    env.REMAINING_DAYS = remainingDays
    return remainingDays
  } else {
    // Calculate the remaining days based on the new threshold and store it
    env.REMAINING_DAYS = daysThreshold - 1
    env.EMAIL_SENT_DATE = currentDate
    sh(script: "aws s3 cp --acl=private --sse AES256 --content-type=text/plain --content-length=${env.REMAINING_DAYS.toString().length()} --body=- s3://${s3Path} -", stdin: env.REMAINING_DAYS.toString())
    return daysThreshold - 1
  }
}

