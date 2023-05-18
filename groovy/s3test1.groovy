import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput

def getRemainingDays(daysThreshold, s3Bucket, s3ObjectKey) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
  
  def storedDate = readStoredDateFromS3(s3Bucket, s3ObjectKey)
  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    return env.REMAINING_DAYS
  } else {
    // Calculate the remaining days based on the new threshold and store it
    def remainingDays = calculateRemainingDays(daysThreshold)
    env.REMAINING_DAYS = remainingDays
    writeDateToS3(s3Bucket, s3ObjectKey, currentDate)
    return remainingDays
  }
}

def calculateRemainingDays(daysThreshold) {
  def timestamp = sh(script: "date '+%Y-%m-%d %H:%M:%S'", returnStdout: true).trim()
  def epochTimestamp = sh(script: "date +%s -d '${timestamp}'", returnStdout: true).trim().toInteger()
  def currentTime = sh(script: "date +%s", returnStdout: true).trim() as Long
  def thresholdTime = currentTime + (daysThreshold * 24 * 60 * 60) // Convert days to seconds
  
  def remainingSeconds = thresholdTime - epochTimestamp
  def remainingDays = (remainingSeconds / (24 * 60 * 60)) as Integer
  
  return remainingDays
}

def readStoredDateFromS3(s3Bucket, s3ObjectKey) {
  def s3Command = "aws s3api get-object --bucket ${s3Bucket} --key ${s3ObjectKey} storedDate.json"
  def storedDateJson = sh(script: s3Command, returnStdout: true).trim()
  def storedDate = new JsonSlurperClassic().parseText(storedDateJson).date
  return storedDate
}

def writeDateToS3(s3Bucket, s3ObjectKey, currentDate) {
  def storedDateJson = new JsonSlurperClassic().toJson([date: currentDate])
  sh "echo '${storedDateJson}' > storedDate.json"
  def s3Command = "aws s3 cp storedDate.json s3://${s3Bucket}/${s3ObjectKey}"
  sh s3Command
}

