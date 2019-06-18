package com.routeanalyzer.api.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.common.ThrowingFunction;
import com.routeanalyzer.api.services.OriginalRouteAS3Service;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class OriginalRouteAS3ServiceImpl implements OriginalRouteAS3Service {

	private AmazonS3 s3Client;
	@Value("${jsa.s3.bucket}")
	private String bucketName;

	@Autowired
	public OriginalRouteAS3ServiceImpl(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}
	
	@Override
	public void uploadFile(byte[] byteArray, String fileName) {
		log.info("Uploading a new file to S3 from a file, name of the file: " + fileName + "\n");
		ofNullable(byteArray)
				.map(this::getMetadata)
				.flatMap(metadata -> of(byteArray)
						.map(ByteArrayInputStream::new)
						.map(byteArrayIn -> new PutObjectRequest(bucketName, fileName, byteArrayIn, metadata)))
				.ifPresent(putObject -> Try.run(() -> s3Client.putObject(putObject))
						.onFailure(error -> log.error(error.getMessage(), error)));
	}

	private ObjectMetadata getMetadata(byte[] byteArray) {
		return ofNullable(byteArray)
				.map(DigestUtils::md5)
				.map(Base64::encodeBase64)
				.map(String::new)
				.map(streamMD5 -> {
					ObjectMetadata meta = new ObjectMetadata();
					meta.setContentLength(byteArray.length);
					meta.setContentMD5(streamMD5);
					return meta;
				})
				.orElse(null);
	}
	
	@Override
	public Optional<S3ObjectInputStream> getFile(String fileName) {
		log.info("Getting a file from S3 with identify: " + fileName + "\n");
		return getS3Object(fileName)
				.map(S3Object::getObjectContent);
	}

	private Optional<S3Object> getS3Object(final String fileName) {
		return Try.of(() -> ofNullable(fileName)
				.map(__ -> new GetObjectRequest(bucketName, fileName))
				.map(ThrowingFunction.unchecked(s3Client::getObject)))
				.recover((e) -> {
						log.error(e.getMessage(), e);
						return empty();
				})
				.get();
	}
	
}
