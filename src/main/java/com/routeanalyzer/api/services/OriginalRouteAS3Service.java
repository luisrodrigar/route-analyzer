package com.routeanalyzer.api.services;

import com.amazonaws.AmazonClientException;

import java.io.BufferedReader;

public interface OriginalRouteAS3Service {

	void uploadFile(byte[] byteArray, String fileName) throws AmazonClientException;
	BufferedReader getFile(String fileName) throws AmazonClientException;

}
