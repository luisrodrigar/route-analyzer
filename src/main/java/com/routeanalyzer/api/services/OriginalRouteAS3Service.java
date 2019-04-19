package com.routeanalyzer.api.services;

import java.io.BufferedReader;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public interface OriginalRouteAS3Service {

	void uploadFile(byte[] byteArray, String fileName) throws AmazonClientException;
	BufferedReader getFile(String fileName) throws AmazonClientException;

}
