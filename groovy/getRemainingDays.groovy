def getRemainingDays(daysThreshold, storedDate) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()

  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    def remainingDays = readFile('remaining_days.txt').trim().toInteger()
    if (remainingDays > 0) {
      remainingDays -= 1
      writeFile file: 'remaining_days.txt', text: remainingDays.toString()
    }
    env.REMAINING_DAYS = remainingDays
    return remainingDays
  } else {
    // Calculate the remaining days based on the new threshold and store it
    env.REMAINING_DAYS = daysThreshold - 1
    env.EMAIL_SENT_DATE = currentDate
    writeFile file: 'remaining_days.txt', text: env.REMAINING_DAYS.toString()
    return daysThreshold - 1
  }
}

