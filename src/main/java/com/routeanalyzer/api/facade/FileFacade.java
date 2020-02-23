package com.routeanalyzer.api.facade;

import com.routeanalyzer.api.model.exception.FileNotFoundException;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileFacade {

    List<String> uploadFile(final MultipartFile multiPart, final String type) throws FileOperationNotExecutedException;

    String getFile(final String id, final String type) throws FileNotFoundException;
}
