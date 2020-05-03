package com.routeanalyzer.api.facade.impl;

import com.routeanalyzer.api.database.ActivityMongoRepository;
import com.routeanalyzer.api.facade.ActivityFacade;
import com.routeanalyzer.api.logic.ActivityOperations;
import com.routeanalyzer.api.model.Activity;
import com.routeanalyzer.api.model.exception.ActivityColorsNotAssignedException;
import com.routeanalyzer.api.model.exception.ActivityNotFoundException;
import com.routeanalyzer.api.model.exception.ActivityOperationNotExecutedException;
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
    public Activity getActivityById(final String id) throws ActivityNotFoundException {
        return mongoRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
    }

    @Override
    public Optional<String> exportAs(final String id, final String type) throws ActivityNotFoundException {
        return of(getActivityById(id))
                .map(activity -> activityOperationsService.exportByType(type, activity));
    }

    @Override
    public Activity removePoint(final String id, final String lat, final String lng, final Long timeInMillis,
                                final Integer index)
            throws ActivityNotFoundException, ActivityOperationNotExecutedException {
        return of(getActivityById(id))
                .flatMap(activity -> activityOperationsService.removePoint(activity, lat, lng, timeInMillis, index))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNotExecutedException(id, "removePoint"));
    }

    @Override
    public Activity joinLaps(final String id, final Integer indexLeft, final Integer indexRight)
            throws ActivityNotFoundException, ActivityOperationNotExecutedException {
        return of(getActivityById(id))
                .map(activity -> activityOperationsService.joinLaps(activity, indexLeft, indexRight))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNotExecutedException(id, "joinLaps"));
    }

    @Override
    public Activity splitLap(final String id, final String lat, final String lng, final Long timeInMillis,
                             final Integer index)
            throws ActivityNotFoundException, ActivityOperationNotExecutedException {
        return  of(getActivityById(id))
                .flatMap(activity -> activityOperationsService.splitLap(activity, lat, lng, timeInMillis, index))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNotExecutedException(id, "splitLap"));
    }

    @Override
    public Activity removeLaps(final String id, final List<String> startTimeLaps, final List<String> indexLaps)
            throws ActivityNotFoundException, ActivityOperationNotExecutedException {
        List<Integer> indexes = toListOfType(indexLaps, Integer::parseInt);
        return of(getActivityById(id))
                .flatMap(activity -> of(toListOfType(startTimeLaps, Long::valueOf))
                        .filter(not(List::isEmpty))
                        .map(dates -> activityOperationsService.removeLaps(activity, dates, indexes))
                        .orElseGet(() -> activityOperationsService.removeLaps(activity, null, indexes)))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityOperationNotExecutedException(id, "removeLaps"));
    }

    @Override
    public Activity setColorLap(final String id, final String data) throws ActivityNotFoundException,
            ActivityColorsNotAssignedException {
        return of(getActivityById(id))
                .flatMap(activity -> activityOperationsService.setColorsGetActivity(activity, data))
                .map(mongoRepository::save)
                .orElseThrow(() -> new ActivityColorsNotAssignedException(id));
    }

}
