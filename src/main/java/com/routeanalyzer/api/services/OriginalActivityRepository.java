package com.routeanalyzer.api.services;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.util.Optional;

public interface OriginalActivityRepository {

	void uploadFile(byte[] byteArray, String fileName);
	Optional<S3ObjectInputStream> getFile(String fileName);

}
