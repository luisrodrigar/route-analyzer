package com.routeanalyzer.api.facade.impl;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.facade.ActivityFacade;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityColorsNotAssignedException;
import com.routeanalyzer.api.model.exception.ActivityOperationNoExecutedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.routeanalyzer.api.common.CommonUtils.not;
import static com.routeanalyzer.api.common.CommonUtils.toListOfType;
import static java.util.Optional.of;

@Service
@RequiredArgsConstructor
public class ActivityFacadeImpl implements ActivityFacade {

    private final ActivityMongoRepository mongoRepository;
    private final ActivityOperations activityOperationsService;

    @Override
    public Activity getActivityById(final String id) {
        return getActivity(id);
    }

    @Override
    public Optional<String> exportAs(final String id, final String type) {
        return of(getActivity(id))
                .map(activity -> activityOperationsService.exportByType(type, activity));
    }

    @Override
    public Activity removePoint(final String id, final String lat, final String lng, final Long timeInMillis, final Integer index) {
        return of(getActivity(id))
                .map(activity -> activityOperationsService.removePoint(activity, lat, lng, timeInMillis, index))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNoExecutedException(id, "removePoint"));
    }

    @Override
    public Activity joinLaps(final String id, final Integer indexLeft, final Integer indexRight) {
        return of(getActivity(id))
                .map(activity -> activityOperationsService.joinLaps(activity, indexLeft, indexRight))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNoExecutedException(id, "joinLaps"));
    }

    @Override
    public Activity splitLap(final String id, final String lat, final String lng, final Long timeInMillis, final Integer index) {
        return  of(getActivity(id))
                .map(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNoExecutedException(id, "splitLap"));
    }

    @Override
    public Activity removeLaps(final String id, final List<String> startTimeLaps, final List<String> indexLaps) {
        List<Integer> indexes = toListOfType(indexLaps, Integer::parseInt);
        return of(getActivity(id))
                .map(activity -> of(toListOfType(startTimeLaps, Long::valueOf))
                        .filter(not(List::isEmpty))
                        .map(dates -> activityOperationsService.removeLaps(activity, dates, indexes))
                        .orElseGet(() -> activityOperationsService.removeLaps(activity, null, indexes)))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNoExecutedException(id, "removeLaps"));
    }

    @Override
    public Activity setColorLap(final String id, final String data) {
        return of(getActivity(id))
                .map(activity -> activityOperationsService.setColorsGetActivity(activity, data))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityColorsNotAssignedException(id));
    }

    private Activity getActivity(String id) {
        return mongoRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
    }

}
