package com.routeanalyzer.api.facade.impl;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.facade.FileFacade;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.file.upload.UploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.GpxUploadFileService;
import com.routeanalyzer.api.logic.file.upload.impl.TcxUploadFileService;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.FileNotFoundException;
import com.routeanalyzer.api.model.exception.FileOperationNotExecutedException;
import com.routeanalyzer.api.services.OriginalActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static com.routeanalyzer.api.common.Constants.SOURCE_GPX_XML;
import static com.routeanalyzer.api.common.Constants.SOURCE_TCX_XML;
import static io.vavr.API.*;
import static io.vavr.Predicates.is;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class FileFacadeImpl implements FileFacade {

    private final TcxUploadFileService tcxService;
    private final GpxUploadFileService gpxService;
    private final ActivityOperations activityOperations;
    private final ActivityMongoRepository mongoRepository;

    @Override
    public List<String> uploadFile(final MultipartFile multiPart, final String type)
            throws FileOperationNotExecutedException{
        return Match(type).option(
                Case($(is(SOURCE_TCX_XML)), tcxType -> uploadAndSave(multiPart, tcxService)),
                Case($(is(SOURCE_GPX_XML)), gpxType -> uploadAndSave(multiPart, gpxService)))
                .toJavaOptional()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getIdsByActivities)
                .orElseThrow(() -> new FileOperationNotExecutedException("uploadFile", type));
    }

    @Override
    public String getFile(final String id, final String type) throws FileNotFoundException{
        return activityOperations.getOriginalFile(id, type)
                .orElseThrow(() -> new FileNotFoundException(id, type));
    }

    private Optional<List<Activity>> uploadAndSave(MultipartFile multiPartFile, UploadFileService fileService) {
        return activityOperations.upload(multiPartFile, fileService)
                .map(activities -> mongoRepository.saveAll(activities))
                .map(activities -> activityOperations.pushToS3(activities, multiPartFile));
    }

    private List<String> getIdsByActivities(final List<Activity> activities) {
        return activities
                .stream()
                .map(Activity::getId)
                .collect(toList());
    }

}
