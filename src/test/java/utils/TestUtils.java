package utils;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mongodb.Function;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.model.Activity;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.newInputStream;

@Slf4j
@UtilityClass
public class TestUtils {

	public static final String GPX_ID_XML 			= "5add80014e237c10148aa6b1";
	public static final String TCX_ID_XML 			= "bac08c0b4e2a7910148aa5c2";
	public static final String ACTIVITY_GPX_ID 		= "5ace8cd14c147400048aa6b0";
	public static final String ACTIVITY_TCX_ID 		= "5ace8caf4c147400048aa6af";
	public static final String ACTIVITY_TCX_1_ID 	= "9aab30a74e13840004822bcb";
	public static final String ACTIVITY_TCX_2_ID 	= "5f1b82a74e138400048bb60a";
	public static final String ACTIVITY_TCX_3_ID 	= "1b1b82a74e13840004822c1a";
	public static final String ACTIVITY_TCX_4_ID 	= "000b82a74e1384000481b10a";
	public static final String ACTIVITY_TCX_5_ID 	= "2b1b82a74aab840004822c1e";
	public static final String ACTIVITY_TCX_6_ID	= "cacb82a74aa1230004822c6a";
	public static final String NOT_EXIST_1_ID 		= "aaaccc111222000333555fff";
	public static final String NOT_EXIST_2_ID 		= "000ccc111222aaa333555bbb";

	public static Function<Path, S3ObjectInputStream> toS3ObjectInputStream = path -> Try.of(() ->
			new S3ObjectInputStream(newInputStream(path), null)).getOrNull();

	public static byte[] getFileBytes(Resource resource) {
		Function<Path, Try<byte[]>> toByteArray = path -> Try.of(() -> Files.readAllBytes(path));
		return Try.of(() -> resource.getFile().toPath()).flatMap(toByteArray::apply).getOrNull();
	}

	public static Activity toActivity(Resource resource)  {
		return Try.of(() -> resource.getURL())
				.onFailure(err -> log.error("It could not be possible to get the url"))
				.flatMap(urlTcx -> Try.of(() -> Resources.toString(urlTcx, Charsets.UTF_8))
						.onFailure(err -> log.error("It could not be possible to get to json string"))
						.flatMap(jsonStr -> JsonUtils.fromJson(jsonStr, Activity.class)))
				.getOrNull();
	}

}
