package com.routeanalyzer.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.routeanalyzer.database.ActivityMongoRepository;
import com.routeanalyzer.logic.ActivityUtils;
import com.routeanalyzer.model.Activity;

@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ActivityRestTestController {

	@Autowired
	private ActivityUtils activityUtilsService;
	@Autowired
	private ActivityMongoRepository activityMongoRepository;
	@Autowired
	private MockMvc mockMvc;

	private Activity activity;

	@Before
	public void setUp() {
		activity = new Activity();
		activity.setId("1234567890");
		activity.setName("Activity test.");
	}

	@Test
	public void getActivityByIdTest() throws Exception {
		mockMvc.perform(get("/activity/{id}", "fake")).andExpect(status().isBadRequest())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.error", is(true)))
				.andExpect(jsonPath("$.description", is("Given activity id not found in database.")));
		mockMvc.perform(get("/activity/{id}", "real")).andExpect(status().isOk())
		.andExpect(content().contentType("application/json;charset=UTF-8"));
	}

	@Test
	public void exportAsTest() {

	}

	@Test
	public void removePointTest() {

	}

	@Test
	public void joinLapsTest() {

	}

	@Test
	public void splitLapTest() {

	}

	@Test
	public void removeLapsTest() {

	}

	@Test
	public void setColorLapsTest() {
	}
}
