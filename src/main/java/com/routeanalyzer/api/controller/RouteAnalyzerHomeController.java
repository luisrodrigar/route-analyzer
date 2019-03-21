
package com.routeanalyzer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteAnalyzerHomeController {
	@RequestMapping(value = "/")
	public String helloWorld() {
		String methods = "<ul><em>File</em></br></br><li style='text-align:center;list-style:none;'>"
				+ "<ul><strong>[POST]/upload</strong> : uploading a route (xml: tcx/gpx)</ul>"
				+ "<ul><strong>[GET]/get/{type}/{id}</strong> : get a file from AWS S3</ul>" + "</li></ul>"
				+ "<ul><em>Activity</em></br></br><li style='text-align:center;list-style:none;'>"
				+ "<ul><strong>[GET]/{id}</strong> : get an activity</ul>"
				+ "<ul><strong>[GET]/{id}/export/{type}</strong> : export the activity with current modifications</ul>"
				+ "<ul><strong>[PUT]/{id}/remove/point</strong> : remove an activity's point</ul>"
				+ "<ul><strong>[PUT]/{id}/join/laps</strong> : join two continuous activity's laps </ul>"
				+ "<ul><strong>[PUT]/{id}/split/lap</strong> : split one lap into two by the given point </ul>"
				+ "<ul><strong>[PUT]/{id}/remove/laps</strong> : remove point </ul>"
				+ "<ul><strong>[PUT]/{id}/color/laps</strong> : set color laps </ul>" + "</li></ul>";
		String title = "<br><div style='text-align:center;'>"
				+ "<h1>Route Analyzer</h1><hr><p>API for Route Analyzer web app which provides several methods to manipulate data from an activity route.</p><p>You can access to the website in this link: <a href='https://routeanalyzer.herokuapp.com/'>Route Analyzer Web</a></p><p>The different methods of the interface implemented:</p><li style='text-align:center;list-style:none;'>"
				+ methods + "</li></div>";

		return title;
	}
}
