package com.routeanalyzer.api.services.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.routeanalyzer.api.config.AWSConfigurationProperties;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class OriginalActivityS3Service implements OriginalActivityRepository {

	private final AWSConfigurationProperties awsProperties;
	private final AmazonS3 s3Client;
	
	@Override
	public void uploadFile(byte[] byteArray, String fileName) {
		log.info("Uploading a new file to S3 from a file, name of the file: " + fileName + "\n");
		ofNullable(byteArray)
				.map(this::getMetadata)
				.flatMap(metadata -> of(byteArray)
						.map(ByteArrayInputStream::new)
						.map(byteArrayIn -> new PutObjectRequest(awsProperties.getS3Bucket(), fileName, byteArrayIn,
								metadata)))
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
		return Try.of(() -> s3Client.getObject(new GetObjectRequest(awsProperties.getS3Bucket(), fileName)))
				.onFailure(err -> log.error("Error trying to get the file from S3 AWS bucket", err))
				.map(S3Object::getObjectContent)
				.toJavaOptional();
	}
	
}
