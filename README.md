# Route Analyzer API
Provides some endpoints with the purpose of analyzing and processing an activity route.<br />
The activity route's type files allow are:
- TCX [Training Center XML Schema](https://www8.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd)
- GPX 1.1 [GPs eXchange Format Schema](https://www.topografix.com/GPX/1/1/gpx.xsd)
## Getting started
This project is using:
 - Java 8: [Java Link](https://www.java.com/es/download/)
 - Maven  [Maven Link](http://maven.apache.org/download.cgi)
 - Mongo database: [Mongo Link](https://docs.mongodb.com/manual/administration/install-community/)
 - Lombok: [Lombok Project](https://projectlombok.org)
### Installation
#### Build
To clean and build the project: 
```
mvn install
```
The jar generated will have the following path: `/target/api-{version}.jar`
#### Clean
To clean the project: 
```
mvn clean
```
#### Test
To run test classes is needed to have docker application started. 
Docker provides some resources to run the integration tests:
- Mongo database using docker compose file
- AWS platform (S3 service basically) using local stack dependency 
To test:
```
mvn test
```
#### Run the application
```
mvn spring-boot:run
```
## Endpoints
All this information is available in more detail on Swagger: [Route Analyzer API Swagger](https://route-analyzer-api.herokuapp.com/swagger-ui.html)
- File
  - **[POST]`/upload`**: uploading a route (xml: tcx/gpx)
  - **[GET]`/get/{type}/{id}`**: get a file from AWS S3
- Activity
  - **[GET]`/{id}`**: get an activity
  - **[GET]`/{id}/export/{type}`**: export the activity with current modifications
  - **[PUT]`/{id}/remove/point`**: remove an activity's point
  - **[PUT]`/{id}/join/laps`**: join two continuous activity's laps
  - **[PUT]`/{id}/split/lap`**: split one lap into two by the given point
  - **[PUT]`/{id}/remove/laps`**: remove point
  - **[PUT]`/{id}/color/laps`**: set color laps
## Deployment
Deployed with [Heroku](www.heroku.com)<br />
This application [Route Analyzer Web](https://routeanalyzer.herokuapp.com/) consumes this API
## Author
**Luis Angel Rodriguez Garcia** - GitHub: [luisrodrigar](https://github.com/luisrodrigar)
