pipeline {
  agent any

  stages {
    stage('Create Dummy JSON File') {
      steps {
        groovy {
          script {
            // Create the dummy JSON file
            def jsonFile = new File('my-data.json')
            jsonFile.withWriter { writer ->
              writer.write('{')
              for (int i = 0; i < 10; i++) {
                writer.write('"resource_id' + i + '": {')
                writer.write('"name": "Name' + i + '",')
                writer.write('"value": "Value' + i + '"}')
                if (i < 9) {
                  writer.write(',')
                }
              }
              writer.write('}')
            }
          }
        }
      }
    }

    stage('Convert JSON to CSV') {
      steps {
        groovy {
          script {
            // Read the JSON file
            def jsonFile = new File('my-data.json')
            def json = new JsonSlurper().parseFile(jsonFile)

            // Create a map of resources
            def resources = [:]
            json.each { resourceId, data ->
              resources[resourceId] = data
            }

            // Convert the map to a CSV file
            def csvFile = new File('my-data.csv')
            csvFile.withWriter { writer ->
              writer.write("resource_id,name,value\n")
              resources.each { resourceId, data ->
                writer.write(resourceId + "," + data.name + "," + data.value + "\n")
              }
            }
          }
        }
      }
    }
  }
}