# RouteAnalyzer
API which provides some endpoints to manage activity routes.
## Endpoints
- File
  - **[POST]/upload**: uploading a route (xml: tcx/gpx)
  - **[GET]/get/_{type}_/_{id}_**: get a file from AWS S3
- Activity
  - **[GET]/_{id}_**: get an activity
  - **[GET]/_{id}_/export/_{type}_**: export the activity with current modifications
  - **[PUT]/_{id}_/remove/point**: remove an activity's point
  - **[PUT]/_{id}_/join/laps**: join two continuous activity's laps
  - **[PUT]/_{id}_/split/lap**: split one lap into two by the given point
  - **[PUT]/_{id}_/remove/laps**: remove point
  - **[PUT]/_{id}_/color/laps**: set color laps
  
You can access to the website which consumes this API here: [Route Analyzer Web](https://routeanalyzer.herokuapp.com/)