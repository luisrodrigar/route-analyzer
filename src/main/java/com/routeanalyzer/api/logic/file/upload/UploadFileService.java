package com.routeanalyzer.api.logic.file.upload;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.logic.TrackPointOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.AbstractXMLService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class UploadFileService<T> {

    protected final AbstractXMLService<T> xmlService;
    protected final ActivityOperations activityOperationsService;
    protected final LapsOperations lapsOperationsService;
    protected final TrackPointOperations trackPointOperations;

    /**
     * Upload a specific xml file with the date of a sport activity
     * @param multiPartFile object with the data
     * @return A list with the data of every activity in the xml file.
     * @throws RuntimeException encapsulates: IOException, JAXBException
     */
    public Try<T> upload(MultipartFile multiPartFile) {
        return Try.of(() -> multiPartFile.getInputStream())
                .onFailure(err -> log.error("Error trying to get the input stream", err))
                .flatMap(xmlService::readXML);
    }

    public abstract List<Activity> toListActivities(T optXmlType);

}
