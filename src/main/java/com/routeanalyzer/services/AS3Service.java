package com.routeanalyzer.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AS3Service {

	private AmazonS3 s3Client;
	private String bucket;

	public AS3Service(String bucket) {
		s3Client = AmazonS3ClientBuilder.defaultClient();
		this.bucket = bucket;
	}

	private ObjectMetadata getMetadata(byte[] byteArray) {
		byte[] resultByte = DigestUtils.md5(byteArray);
		String streamMD5 = new String(Base64.encodeBase64(resultByte));
		ObjectMetadata meta = new ObjectMetadata();

		meta.setContentLength(byteArray.length);
		meta.setContentMD5(streamMD5);
		return meta;
	}

	public void uploadFile(byte[] byteArray, String fileName)
			throws AmazonClientException{
		System.out.println("Uploading a new file to S3 from a file, name of the file: " + fileName + "\n");
		ObjectMetadata meta = getMetadata(byteArray);
		s3Client.putObject(new PutObjectRequest(bucket, fileName, new ByteArrayInputStream(byteArray), meta));
	}
	
	public BufferedReader getFile(String fileName)
			throws AmazonServiceException, AmazonClientException, IOException {
		System.out.println("Getting a file from S3 with identify:" + fileName + "\n");
		S3Object object = s3Client.getObject(new GetObjectRequest(bucket, fileName));
		return new BufferedReader(new 
        		InputStreamReader(object.getObjectContent()));
	}

}
