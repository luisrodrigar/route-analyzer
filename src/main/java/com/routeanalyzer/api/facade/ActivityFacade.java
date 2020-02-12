package com.routeanalyzer.api.facade;

import com.routeanalyzer.api.model.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityFacade {

    Activity getActivityById(final String id);

    Optional<String> exportAs(final String id, final String type);

    Activity removePoint(final String id, final String lat, final String lng, final Long timeInMillis,
                         final Integer index);

    Activity joinLaps(final String id, final Integer indexLeft, final Integer indexRight);

    Activity splitLap(final String id, final String lat, final String lng, final Long timeInMillis,
                      final Integer index);

    Activity removeLaps(final String id,final List<String> startTimeLaps, final List<String> indexLaps);

    Activity setColorLap(final String id, final String data);

}
