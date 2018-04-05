package com.routeanalyzer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteAnalyzerHomeController {
	@RequestMapping(value="/")
	public String helloWorld() {
		String methods = "<ul>File<li style='text-align:center;'>"
				+ "<ul>[POST]/upload : uploading a route (xml: tcx/gpx)</ul>"
				+ "<ul>[GET]/get/{type}/{id} : get a file from AWS S3</ul>"
				+ "</li></ul>"
				+ "<ul>Activity<li style='text-align:center;'>"
				+ "<ul>[GET]/{id} : get an activity</ul>"
				+ "<ul>[GET]/{id}/export/{type} : export the activity with current modifications</ul>"
				+ "<ul>[PUT]/{id}/remove/point : remove an activity's point</ul>"
				+ "<ul>[PUT]/{id}/join/laps : join two continuous activity's laps </ul>"
				+ "<ul>[PUT]/{id}/split/lap : split one lap into two by the given point </ul>"
				+ "<ul>[PUT]/{id}/remove/laps : remove point </ul>"
				+ "<ul>[PUT]/{id}/color/laps : set color laps </ul>"
				+ "</li></ul>";
		String title = "<br><div style='text-align:center;'>"
				+ "<h2>Route Analyzer</h2><br><br><li style='text-align:center;'>"+ methods + "</li></div>";
		
		return title;
	}
}
