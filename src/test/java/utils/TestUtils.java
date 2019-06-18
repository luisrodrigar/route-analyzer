package utils;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mongodb.Function;
import com.routeanalyzer.api.common.JsonUtils;
import com.routeanalyzer.api.model.Activity;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.nio.file.Files.newInputStream;

@UtilityClass
public class TestUtils {

	public static final String ACTIVITY_GPX_ID = "5ace8cd14c147400048aa6b0";
	public static final String ACTIVITY_TCX_ID = "5ace8caf4c147400048aa6af";

	public static Supplier<Activity> createUnknownActivity = () -> Activity.builder().build();

	public static Function<Path, S3ObjectInputStream> toS3ObjectInputStream = path -> Try.of(() ->
			new S3ObjectInputStream(newInputStream(path), null)).getOrNull();

	public static byte[] getFileBytes(Resource resource) {
		Function<Path, Try<byte[]>> toByteArray = path -> Try.of(() -> Files.readAllBytes(path));
		return Try.of(() -> resource.getFile().toPath()).flatMap(toByteArray::apply).getOrNull();
	}

	public static RuntimeException toRuntimeException(Exception exception) {
		return new RuntimeException(exception);
	}

	public static Supplier<Activity>  toActivity(Resource resource)  {
		return () -> Try.of(() -> resource.getURL())
				.flatMap(urlTcx -> Try.of(() -> Resources.toString(urlTcx, Charsets.UTF_8))
						.map(jsonStr -> JsonUtils.fromJson(jsonStr, Activity.class))).getOrNull();
	}

}
