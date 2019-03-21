package com.routeanalyzer.logic.file.export;

import com.routeanalyzer.model.Activity;

import javax.xml.bind.JAXBException;

public interface ExportFileService {
    /**
     * Export method to a specific file type
     * @param act: activity to export
     * @return String object with the data in the specific xml type.
     * @throws JAXBException
     */
    String export(Activity act)  throws JAXBException;
}
