package com.routeanalyzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RouteAnalyzerHomeController {
	@RequestMapping(value="/")
	public ModelAndView helloWorld() {
 
		String message = "<br><div style='text-align:center;'>"
				+ "<h3>Route Analyzer</h3></div><br><br>";
		return new ModelAndView("welcome", "message", message);
	}
}
