Library('tc15-cicd-resources') _
 
import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
 
final AUTOMATION_ROLE_NAME = ''
final AWS_API_URL = ''
final PROXY_ADDRESS = ''
final TASK_DISTRIBUTION_URL = ''
final FILE_NAME = ''
final BITBUCKET_URL = ''
 
def awsAccountNumber = DEPLOYMENT_ACCOUNT
def currentDate = new Date().format("dd-MM-yyyy")
def violatingResourcesGlobal = []
def fileName = 'violationReport-' + currentDate + '.csv'
def zeroViolations = false
def my_count = 0
def violationData = ''
 
 
pipeline {
  agent {
    label 'Linux-AWS'
  }
 
  stages {
    stage('Prepare') {
      steps {
        script {
          // Record the AWS CLI version.
          sh 'aws --version'
        }
      }
    }
   
    stage('Authenticate') {
        steps {
            script {
                awsEnv = awsAuthenticate()
            }
        }            
    }
 
    stage('Process Violations') {
      steps {
        script {
 
          // Create copy of txt file for editing
          sh "cp ${WORKSPACE}/src/main/resources/ViolationReportTemplate.html ViolationReport.html"  
 
          withCredentials([usernamePassword(credentialsId: PROXY_CREDENTIALS_ID, passwordVariable: 'PROXY_PASS', usernameVariable: 'PROXY_USER')])
          {
            def encodedPassword = URLEncoder.encode(PROXY_PASS, "UTF-8")
            withEnv(awsEnv + "https_proxy=http://${PROXY_USER}:${encodedPassword}@${PROXY_ADDRESS}")
            {
              dynamoData = sh(
                script: "aws dynamodb scan --table-name '${VIOLATIONS_TABLE_NAME}'",
                returnStdout: true
              ).trim()
 
              //write to reported violations
              def violationsJson = new JsonSlurperClassic().parseText(dynamoData)
 
              // generate csv report
              def violatingResources = []
              if (violationsJson.Items.size() > 0)
                {
                  for (vio in violationsJson.Items)
                  {
                    violatingResources = parseDynamoJsonToCsv(vio, violatingResources)
                    // used to indicate metrics have been archived for this violation - prevents ArchiveMetrics job picking it up.
                    vio.archivedToS3 = [BOOL: true]
                    writeJSON file: "item.json", json: JsonOutput.toJson(vio)
                    archiveItemOutput = sh(script: "aws dynamodb put-item --table-name ${REPORTED_VIOLATIONS_TABLE_NAME} --item file://item.json", returnStdout: true).trim()
                    reportedRemoveOutput = sh(script: "aws dynamodb delete-item --table-name ${VIOLATIONS_TABLE_NAME} --key '{\"eventID\":{\"S\":\"${vio.eventID.S}\"}}'", returnStdout: true).trim()
                  }
               
                  env.numberViolations = violatingResources.size()
                  violatingResources.add(0, ["eventTime", "region", "resourceId", "resourceType", "description", "ruleName", "cfnStack", "cfnResourceCreatedTimestamp", "productName", "sourceRepo", "commitId"]);
                  violatingResourcesGlobal = violatingResources
                  writeCSV file: fileName, records: violatingResources
                 
                  for (v in violatingResources) {
                    println "Item" + v
                    if (my_count !=0)
                    {
                      violationData+= """<TR><TD>${currentDate}</TD><TD>${v[1]}</TD><TD>${v[2]}</TD><TD>${v[4]}</TD><TD>${v[5]}</TD><TD>${v[6]}</TD><TD>${v[7]}</TD><TD>${v[8]}</TD></TR>"""
                    }
                 
                  my_count++
                }
               
                  violationDate = currentDate.split("-")
               
                  // add violation to s3 for metrics
                  s3path = "s3://${DEPLOYMENT_ACCOUNT}-${AWS_DEFAULT_REGION}-${ARCHIVE_BUCKET_NAME}/vigilante/" + violationDate[2] + "/" + violationDate[1] + "/" + violationDate[0] + "/" + fileName
                  s3 = sh(script: "aws s3 cp ${fileName} ${s3path}", returnStdout: true).trim()
                 
                  println "Violations archived to " + s3path
                }
             
              else
              {
                println "No violations found"
                 zeroViolations = true
              }
            }
          }
        }
      }
    }
 
    stage("Send Report")
    {
      steps {
        script {
          def daysThreshold = 5
          def remainingDays = getDaysUntilThreshold(daysThreshold)
          def storedDate = env.EMAIL_SENT_DATE
          emailSendList = ""
           
          if(zeroViolations) {
              emailSendList = ADMIN_EMAIL_LIST
          } else {
              emailSendList = EMAIL_LIST
          }
         
          if (violationData=='') { violationData='<TR><TD colspan="8">**No Violations Reported</TD></TR>' }
 
           output = sh(script: """(sed -i 's;%violationsCountPlaceholder%;${env.numberViolations};g;
                                          s;%accountNumberPlaceholder%;${awsAccountNumber};g;
                                          s;%tableDataPlaceholder%;${violationData};g;
                                          s;%buildUrlPlaceholder%;${BUILD_URL};g;
                                          s;%remainingDaysPlaceholder%;${remainingDays};g;
                                          s;%stylesheetPlaceholder%;<link rel="stylesheet" type="text/css" href="style.css">;g'  ViolationReport.html) &&
                                          cat ViolationReport.html""", returnStdout: true)
 
          emailext attachLog: false,
            attachmentsPattern: fileName,
            body: output,
            recipientProviders: [],
            subject: "Vigilante - AWS/BLZ Non-Compliance Report for # " + awsAccountNumber + " - " + currentDate,
            to: "${emailSendList}"
        }
      }
    }
  }
 
  post {
    always {
      cleanWs()
    }
 
    failure {
      emailext attachLog: false,
        attachmentsPattern: fileName,
        body: "Please investigate the failure for -\n${BUILD_URL}",
        recipientProviders: [],
        subject: "Job Failed || Vigilante - AWS/BLZ Non-Compliance Report for # " + awsAccountNumber + " - " + currentDate,
        to: "${ADMIN_EMAIL_LIST}"
    }
  }
}
 
def parseDynamoJsonToCsv(dynamoJson, violationsList) {
  for (evaluation in dynamoJson.requestParameters.M.evaluations.L) {
    if (evaluation.M.complianceType.S == "NON_COMPLIANT") {
      // ensure cloudformation stack already checked for - handled by violationTagger
      if (!evaluation.M.keySet().contains("cfnStack")) {
        // get the resources' cfn stack if exists
        cfnResources = isCloudformationResource(evaluation.M.complianceResourceId.S)
        evaluation.M.cfnStack = [S: cfnResources["stackName"]]
        evaluation.M.cfnTimestamp = [S: cfnResources["stackTimestamp"]]
        evaluation.M.productName = [S: ""]
        evaluation.M.sourceRepo = [S: ""]
        evaluation.M.commitId = [S: ""]
      }
      // get stack owners and add to email list
      addStackOwnersToEmailList(evaluation)
 
      // add to violationsList for reporting
      violationsList.add([
        dynamoJson.eventTime.S.replace("T", " "),
        dynamoJson.awsRegion.S,
        evaluation.M.complianceResourceId.S,
        evaluation.M.complianceResourceType.S,
        evaluation.M.annotation.S,
        dynamoJson.additionalEventData.M.configRuleName.S,
        evaluation.M.cfnStack.S,
        evaluation.M.cfnTimestamp.S,
        evaluation.M.productName.S,
        evaluation.M.sourceRepo.S,
        evaluation.M.commitId.S
      ])
    }
  }
  return violationsList
}
 
def isCloudformationResource(resource) {
  try {
    cfnCheck = sh(script: "aws cloudformation describe-stack-resources --physical-resource-id ${resource}", returnStdout: true).trim()
    cfnjson = new JsonSlurperClassic().parseText(cfnCheck)
    timestamp = ""
    for (stackResource in cfnjson.StackResources) {
      if (stackResource.PhysicalResourceId == resource) {
        timestamp = stackResource.Timestamp.replace("T", " ")
        break
      }
    }
    return ["stackName": cfnjson.StackResources[0].StackName, "stackTimestamp": timestamp]
  } catch (err) {
    return ["stackName": "", "stackTimestamp": ""]
  }
}
 
def addStackOwnersToEmailList(evaluation) {
  if (evaluation.M.keySet().contains("sourceRepo") && evaluation.M.sourceRepo.S.length() > 0) {
    try {
      // get repo slug
      repoPath = evaluation.M.sourceRepo.S.toLowerCase().split("https://stash.:8443/scm/awscc/")[-1].split(".git")[0]
      def repoMetadataFilePath = 'https://stash/projects/AWSCC/repos/' + repoPath + '/browse/.metadata.yml?at=refs/heads/master&raw'
      // get repo owners from metadata file
      def metadataResponse = httpRequest(
        authentication: GIT_HTTPS_CREDENTIALS_ID,
        url: repoMetadataFilePath,
        validResponseCodes: '200'
      )
      metadata = readYaml text: metadataResponse.content
      EMAIL_LIST += "," + metadata['Owners'].join(',')
    } catch (err) {
      println err
      return
    }
  } else {
    // TODO: can we evaluate owner from stack name?
    return
  }
}
 
def updateViolationItem(item){
  writeJSON file: "item.json", json: JsonOutput.toJson(item)
  sh(script: "aws dynamodb put-item --table-name ${REPORTED_VIOLATIONS_TABLE_NAME} --item file://item.json", returnStdout: true)
}
 
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
