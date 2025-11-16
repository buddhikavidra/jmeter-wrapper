curl -X POST "http://localhost:8080/api/start-test"   -H "Content-Type: application/x-www-form-urlencoded"   -d "jmxPath=/home/buddhi/IdeaProjects/jmeter-wrapper/src/main/resources/jmxfiles/HTTPRequest.jmx"   -d "testName=MyPerformanceTest"

curl "http://localhost:8080/api/executions"


curl "http://localhost:8080/api/executions/1"

curl "http://localhost:8080/api/executions/running"

