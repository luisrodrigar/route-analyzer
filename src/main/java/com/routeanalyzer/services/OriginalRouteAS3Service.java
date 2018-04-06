package com.routeanalyzer.services;

import java.io.BufferedReader;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public interface OriginalRouteAS3Service {

	public void uploadFile(byte[] byteArray, String fileName)
			throws AmazonClientException;
	public BufferedReader getFile(String fileName)
			throws AmazonServiceException, AmazonClientException, IOException;

}
