package com.routeanalyzer.api.logic.file.export;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.services.reader.AbstractXMLService;

import javax.xml.bind.JAXBElement;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class ExportFileService<T> {

    private AbstractXMLService<T> xmlService;

    public ExportFileService(AbstractXMLService<T> xmlService) {
        this.xmlService = xmlService;
    }

    /**
     * Export method to a specific file type
     * @param act: activity to export
     * @return String object with the data in the specific xml type.
     */
    public String export(Activity act) {
        return ofNullable(act)
                .flatMap(this::convertToXmlObjects)
                .map(xmlService::createXML)
                .orElse(null);
    }

    public abstract Optional<JAXBElement<T>> convertToXmlObjects(Activity activity);
}
