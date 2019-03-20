package com.routeanalyzer.logic.file.upload;

import com.amazonaws.AmazonClientException;
import com.routeanalyzer.model.Activity;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

public interface UploadService {
    /**
     * Upload a specific xml file with the date of a sport activity
     * @param multiPart object with the data
     * @return A list with the data of every activity in the xml file.
     * @throws IOException
     * @throws AmazonClientException
     * @throws JAXBException
     * @throws SAXParseException
     */
    List<Activity> upload(MultipartFile multiPart)
            throws IOException, AmazonClientException, JAXBException, SAXParseException;
}
