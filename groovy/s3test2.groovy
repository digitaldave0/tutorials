import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput

def getRemainingDays(daysThreshold, s3Bucket, s3ObjectKey) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
  
  def storedDate = readStoredDateFromS3(s3Bucket, s3ObjectKey)
  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    return env.REMAINING_DAYS
  } else {
    // Check if the stored date is different from the current date
    if (storedDate != null && storedDate != currentDate) {
      // Increment the remaining days by 1 if the stored date is different
      env.REMAINING_DAYS += 1
    }
    
    // Calculate the remaining days based on the fixed 7-day threshold
    def remainingDays = calculateRemainingDays(daysThreshold)
    env.REMAINING_DAYS = remainingDays
    writeDateToS3(s3Bucket, s3ObjectKey, currentDate)
    return remainingDays
  }
}

// Rest of the code...


