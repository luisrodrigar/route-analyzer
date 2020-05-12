package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@UtilityClass
public class DateUtils {

    /**
     * Date Time operations
     */
    public static Optional<Date> toDate(final ZonedDateTime offsetDateTime) {
        return ofNullable(offsetDateTime)
                .map(ZonedDateTime::toInstant)
                .map(Date::from);
    }

    public static Optional<Long> toTimeMillis(final ZonedDateTime offsetDateTime) {
        return ofNullable(offsetDateTime)
                .map(ZonedDateTime::toInstant)
                .map(Instant::toEpochMilli);
    }

    public static Optional<ZonedDateTime> toUtcZonedDateTime(final XMLGregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .flatMap(DateUtils::toUtcZonedDateTime);
    }

    public static Optional<ZonedDateTime> toUtcZonedDateTime(final GregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(GregorianCalendar::getTime)
                .map(Date::toInstant)
                .flatMap(DateUtils::toUtcZonedDateTime);
    }

    public static Optional<ZonedDateTime> toUtcZonedDateTime(final Long timeMillis) {
        return ofNullable(timeMillis)
                .map(Instant::ofEpochMilli)
                .flatMap(DateUtils::toUtcZonedDateTime);
    }

    public static Optional<ZonedDateTime> toUtcZonedDateTime(final Instant instant) {
        return ofNullable(instant)
                .map(__ -> ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }

    public static GregorianCalendar createGregorianCalendar(final Date date) {
        return ofNullable(date)
                .map(__ -> {
                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
                    gregorianCalendar.setTime(date);
                    return gregorianCalendar;
                })
                .orElse(null);
    }

    public static XMLGregorianCalendar createXmlGregorianCalendar(final GregorianCalendar gregorianCalendar) {
        return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
                .getOrNull();
    }

    public static Double millisToSeconds(final Double milliSeconds){
        return ofNullable(milliSeconds)
                .map(__ -> milliSeconds / 1000.0)
                .orElse(null);
    }
}
