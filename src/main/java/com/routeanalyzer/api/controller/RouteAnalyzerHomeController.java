
package com.routeanalyzer.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RestController
public class RouteAnalyzerHomeController {

	@Autowired
	private TemplateEngine templateGenerator;

	private static final String HOME_PAGE = "home";

	@RequestMapping(value = "/")
	public String helloWorld() {
		Context context = new Context();
		return templateGenerator.process(HOME_PAGE, context);
	}
}
