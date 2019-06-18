package com.routeanalyzer.api.logic.file.upload;

import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.logic.LapsOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.AbstractXMLService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.routeanalyzer.api.common.ThrowingFunction.unchecked;
import static java.util.Optional.ofNullable;

public abstract class UploadFileService<T> {

    protected AbstractXMLService<T> xmlService;
    protected ActivityOperations activityOperationsService;
    protected LapsOperations lapsOperationsService;

    public UploadFileService(AbstractXMLService<T> xmlService, ActivityOperations activityOperations,
                             LapsOperations lapsOperations) {
        this.xmlService = xmlService;
        this.activityOperationsService = activityOperations;
        this.lapsOperationsService = lapsOperations;
    }

    /**
     * Upload a specific xml file with the date of a sport activity
     * @param multiPartFile object with the data
     * @return A list with the data of every activity in the xml file.
     * @throws RuntimeException encapsulates: IOException, JAXBException
     */
    public List<Activity> upload(MultipartFile multiPartFile) {
        return ofNullable(multiPartFile)
                .map(unchecked(MultipartFile::getInputStream))
                .map(xmlService::readXML)
                .map(this::toListActivities)
                .orElseGet(Collections::emptyList);
    }

    protected abstract List<Activity> toListActivities(T optXmlType);

}
