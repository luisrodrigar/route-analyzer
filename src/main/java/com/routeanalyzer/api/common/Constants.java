package com.routeanalyzer.api.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SOURCE_GPX_XML = "gpx";
    public static final String SOURCE_TCX_XML = "tcx";
    public static final String DELIMITER_POSITIONS = "|";
    public static final String DELIMITER_COORDINATES = ",";

    private static final String ACTIVITY_ROOT_PATH = "/activity";

    public static final String GET_ACTIVITY_PATH = ACTIVITY_ROOT_PATH + "/{id}";
    public static final String EXPORT_AS_PATH = ACTIVITY_ROOT_PATH + "/{id}/export/{type}";
    public static final String REMOVE_POINT_PATH = ACTIVITY_ROOT_PATH + "/{id}/remove/point";
    public static final String REMOVE_LAP_PATH = ACTIVITY_ROOT_PATH + "/{id}/remove/laps";
    public static final String JOIN_LAPS_PATH = ACTIVITY_ROOT_PATH + "/{id}/join/laps";
    public static final String SPLIT_LAP_PATH = ACTIVITY_ROOT_PATH + "/{id}/split/lap";
    public static final String COLORS_LAP_PATH = ACTIVITY_ROOT_PATH + "/{id}/color/laps";

    private static final String FILE_ROOT_PATH = "/file";

    public static final String GET_FILE_PATH =  FILE_ROOT_PATH + "/get/{type}/{id}";
    public static final String UPLOAD_FILE_PATH = FILE_ROOT_PATH + "/upload";

    public static final String SAX_PARSE_EXCEPTION_MESSAGE = "Problem trying to parser xml file. Check if its correct.";
    public static final String JAXB_EXCEPTION_MESSAGE = "Problem with the file format exported/uploaded.";
    public static final String AMAZON_CLIENT_EXCEPTION_MESSAGE =
            "Problem trying to delete/get the activity/file :: Amazon S3 Problem";
    public static final String IO_EXCEPTION_MESSAGE = "Problem trying to get the file :: Input/Output Problem";
    public static final String BAD_REQUEST_MESSAGE = "Activity was not found or other params are not valid.";
    public static final String BAD_TYPE_MESSAGE = "Xml file type not found";
}
