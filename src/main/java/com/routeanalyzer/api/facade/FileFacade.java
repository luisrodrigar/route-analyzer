package com.routeanalyzer.api.facade;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileFacade {

    List<String> uploadFile(final MultipartFile multiPart, final String type);

    String getFile(final String id, final String type);
}
