package com.routeanalyzer.api.controller.rest;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.routeanalyzer.api.model.Activity;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-controller")
public class MockMvcTestController {

	@Rule
	public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("routeanalyzer-test");

	@Autowired
	protected MockMvc mockMvc;

	protected MockMultipartHttpServletRequestBuilder builder;

	/**
	 * HTTP POST with a file in the body uploadFile(...) test methods
	 * 
	 */
	protected void uploadFileBuilder() {
		builder = MockMvcRequestBuilders.multipart("/file/upload");
		builder.with(request -> {
			request.setMethod("POST");
			return request;
		});
	}

	/**
	 * HTTP POST call Method checks if it is thrown the following exception.
	 * 
	 * @param file:
	 *            file with the information
	 * @param xmlType:
	 *            type of the file which is uploading
	 * @param descriptionError:
	 *            description of the error which is expected to generate
	 * @param descriptionException:
	 *            description of the exception which is expected to generate
	 * @throws Exception
	 */
	protected void isThrowingExceptionByMockMultipartHTTPPost(MockMultipartFile file, ResultMatcher expectedResponse,
			String xmlType, String descriptionError, String descriptionException) throws Exception {
		String typeField = "type", errorField = "$.error", descriptionField = "$.description",
				exceptionField = "$.exception";
		mockMvc.perform(builder.file(file).param(typeField, xmlType)).andExpect(expectedResponse)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath(errorField, is(true))).andExpect(jsonPath(descriptionField, is(descriptionError)))
				.andExpect(jsonPath(exceptionField, is(descriptionException)));
	}

	/**
	 * HTTP POST call Method checks if it is generated the following exception.
	 * 
	 * @param file
	 * @param xmlType:
	 *            type of the file which is uploading
	 * @param descriptionError:
	 *            description of the error which is expected to generate
	 * @throws Exception
	 */
	protected void isGenerateErrorByMockMultipartHTTPPost(MockMultipartFile file, ResultMatcher expectedResponse,
			String xmlType, String descriptionError) throws Exception {
		String typeField = "type", errorField = "$.error", descriptionField = "$.description";
		mockMvc.perform(builder.file(file).param(typeField, xmlType)).andExpect(expectedResponse)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath(errorField, is(true))).andExpect(jsonPath(descriptionField, is(descriptionError)));
	}

	/**
	 * HTTP GET call Method checks if it is generated the following exception.
	 *
	 * @param requestBuilder:
	 *            to build the request
	 * @param expectedResponse:
	 *            expected response/value
	 * @param descriptionError:
	 *            description of the error which is expected to generate
	 * @param isError
	 * 			  boolean it determines if it is an error or not
	 * @throws Exception
	 */
	protected void isGenerateErrorHTTP(RequestBuilder requestBuilder, ResultMatcher expectedResponse,
			String descriptionError, boolean isError) throws Exception {
		String errorField = "$.error", descriptionField = "$.description";
		mockMvc.perform(requestBuilder).andExpect(expectedResponse)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath(errorField, is(isError)))
				.andExpect(jsonPath(descriptionField, is(descriptionError)));
	}

	/**
	 * HTTP GET call Method checks if it is thrown the following exception.
	 *
	 * @param requestBuilder:
	 *            to build the request
	 * @param expectedResponse:
	 *            expected response/value
	 * @param descriptionError:
	 *            description of the error which is expected to generate
	 * @param exceptionError
	 * 			  exception error generated
	 * @throws Exception
	 */
	protected void isThrowingExceptionHTTP(RequestBuilder requestBuilder, ResultMatcher expectedResponse,
			String descriptionError, String exceptionError) throws Exception {
		String errorField = "$.error", descriptionField = "$.description", exceptionField = "$.exception";
		mockMvc.perform(requestBuilder).andExpect(expectedResponse)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath(errorField, is(true))).andExpect(jsonPath(descriptionField, is(descriptionError)))
				.andExpect(jsonPath(exceptionField, is(exceptionError)));
	}
	
	/**
	 * HTTP GET call Method checks if it is generated the following error.
	 *
	 * @param requestBuilder:
	 *            to build the request
	 * @param expectedResponse:
	 *            expected response/value
	 * @param descriptionError:
	 *            description of the error which is expected to generate
	 * @throws Exception
	 */
	protected void isThrowingExceptionHTTP(RequestBuilder requestBuilder, ResultMatcher expectedResponse,
			String descriptionError) throws Exception {
		String errorField = "$.error", descriptionField = "$.description";
		mockMvc.perform(requestBuilder).andExpect(expectedResponse)
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath(errorField, is(true)))
				.andExpect(jsonPath(descriptionField, is(descriptionError)));
	}

	/**
	 *
	 * @param requestBuilder:
	 *            to build the request
	 * @param activity:
	 *            activity expected
	 * @throws Exception
	 */
	protected void isReturningActivityHTTP(RequestBuilder requestBuilder, Activity activity) throws Exception {
		mockMvc.perform(requestBuilder).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(activity.getId())))
				.andExpect(jsonPath("$.sourceXmlType", is(activity.getSourceXmlType())));
	}
	
	/**
	 *
	 * @param requestBuilder:
	 *            to build the request
	 * @param mediaType:
	 *            activity expected
	 * @throws Exception
	 */
	protected void isReturningFileHTTP(RequestBuilder requestBuilder, MediaType mediaType) throws Exception {
		mockMvc.perform(requestBuilder).andExpect(status().isOk())
				.andExpect(content().contentType(mediaType));
	}

}
