package com.routeanalyzer.api.facade;

import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityColorsNotAssignedException;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityOperationNoExecutedException;

import java.util.List;
import java.util.Optional;

public interface ActivityFacade {

    Activity getActivityById(final String id) throws ActivityNotFoundException;

    Optional<String> exportAs(final String id, final String type) throws ActivityNotFoundException;

    Activity removePoint(final String id, final String lat, final String lng, final Long timeInMillis,
                         final Integer index) throws ActivityNotFoundException, ActivityOperationNoExecutedException;

    Activity joinLaps(final String id, final Integer indexLeft, final Integer indexRight)
            throws ActivityNotFoundException, ActivityOperationNoExecutedException;

    Activity splitLap(final String id, final String lat, final String lng, final Long timeInMillis,
                      final Integer index) throws ActivityNotFoundException, ActivityOperationNoExecutedException;

    Activity removeLaps(final String id,final List<String> startTimeLaps, final List<String> indexLaps)
            throws ActivityNotFoundException, ActivityOperationNoExecutedException;

    Activity setColorLap(final String id, final String data) throws ActivityNotFoundException,
            ActivityColorsNotAssignedException;

}
