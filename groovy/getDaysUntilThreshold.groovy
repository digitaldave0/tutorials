def getDaysUntilThreshold(daysThreshold, eventTime) {
  def currentTime = sh(script: "date +%s", returnStdout: true).trim() as Long
  def eventTimestamp = sh(script: "date +%s -d '${eventTime}'", returnStdout: true).trim().toInteger()
  def thresholdTime = eventTimestamp + (daysThreshold * 24 * 60 * 60) // Convert days to seconds
   
  def remainingSeconds = thresholdTime - currentTime
  def remainingDays = (remainingSeconds / (24 * 60 * 60)) as Integer
   
  return remainingDays
}
 
def getRemainingDays(daysThreshold, storedDate, eventTime) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
 
  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    return env.REMAINING_DAYS
  } else {
    // Calculate the remaining days based on the new threshold and store it
    def remainingDays = getDaysUntilThreshold(daysThreshold, eventTime)
    env.REMAINING_DAYS = remainingDays
    env.EMAIL_SENT_DATE = currentDate
    return remainingDays
  }
}

