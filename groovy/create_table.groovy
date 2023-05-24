def getRemainingDays(daysThreshold, storedDateS3Path, eventTime) {
  def currentDate = sh(script: "date '+%Y-%m-%d'", returnStdout: true).trim()
  storedDateS3Path = "s3://${DEPLOYMENT_ACCOUNT}-${AWS_DEFAULT_REGION}-${ARCHIVE_BUCKET_NAME}/counter/" + $date
  s3 = sh(script: "aws s3 cp ${fileName} ${storedDateS3Path}", returnStdout: true).trim()

  if (storedDate == currentDate) {
    // The report has already been sent today, so use the stored remaining days
    return env.REMAINING_DAYS
  } else {
    // Calculate the remaining days based on the stored date and the event time
    def remainingDays = getDaysUntilThreshold(daysThreshold, eventTime)
    env.REMAINING_DAYS = remainingDays
    env.EMAIL_SENT_DATE = currentDate

    def label = ""
    def tableColor = ""

    if (remainingDays <= 2) {
      label = "green"
      tableColor = "green"
    } else if (remainingDays <= 5) {
      label = "amber"
      tableColor = "orange"
    } else {
      label = "red"
      tableColor = "red"
    }

    s3 = sh(script: "aws s3 cp ${filename} s3://${DEPLOYMENT_ACCOUNT}-${AWS_DEFAULT_REGION}-${ARCHIVE_BUCKET_NAME}/counter/{currentDate}-${label}.txt", returnStdout:true).trim()

    // Call the function to generate the table HTML with the appropriate color
    def tableHtml = generateTableHtmlWithColor(label, tableColor)

    // Rest of the code...
    // Include the tableHtml in your email or report generation logic

    return remainingDays
  }
}

// Function to generate the table HTML with color
def generateTableHtmlWithColor(label, tableColor) {
  def html = """
    <table style="background-color: ${tableColor};">
      <tr>
        <th>Header 1</th>
        <th>Header 2</th>
        <!-- Add more table headers as needed -->
      </tr>
      <tr>
        <td>Data 1</td>
        <td>Data 2</td>
        <!-- Add more table data rows as needed -->
      </tr>
    </table>
  """
  return html
}

// Example usage
stage("Send Report") {
  steps {
    script {
      def daysThreshold = env.DAYS_THRESHOLD as Integer
      def remainingDays = getRemainingDays(daysThreshold)
      
      // Rest of the code...
      // Use the generated tableHtml in your email or report generation logic
    }
  }
}
