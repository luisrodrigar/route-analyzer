package com.routeanalyzer.api.database;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
    @Override
    public Date convert(final ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }
}
