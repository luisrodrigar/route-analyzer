package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    public static Optional<Date> toDate(ZonedDateTime offsetDateTime) {
        return ofNullable(offsetDateTime)
                .map(ZonedDateTime::toInstant)
                .map(Date::from);
    }

    public static Optional<Long> toTimeMillis(ZonedDateTime offsetDateTime) {
        return ofNullable(offsetDateTime)
                .map(ZonedDateTime::toInstant)
                .map(Instant::toEpochMilli);
    }

    public static Optional<ZonedDateTime> toZonedDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .flatMap(DateUtils::toZonedDateTime);
    }

    public static Optional<ZonedDateTime> toZonedDateTime(GregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(GregorianCalendar::getTime)
                .map(Date::toInstant)
                .flatMap(DateUtils::toZonedDateTime);
    }

    public static Optional<ZonedDateTime> toZonedDateTime(Long timeMillis) {
        return ofNullable(timeMillis)
                .map(Instant::ofEpochMilli)
                .flatMap(DateUtils::toZonedDateTime);
    }

    public static Optional<ZonedDateTime> toZonedDateTime(Instant instant) {
        return ofNullable(instant)
                .map(__ -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public static GregorianCalendar createGregorianCalendar(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        return gregorianCalendar;
    }

    public static XMLGregorianCalendar createXmlGregorianCalendar(GregorianCalendar gregorianCalendar) {
        return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
                .getOrNull();
    }

    private static Optional<Instant> toInstant(LocalDateTime localDateTime) {
        return ofNullable(localDateTime)
                .map(dateTime -> dateTime.atZone(ZoneId.systemDefault()))
                .map(ZonedDateTime::toInstant);
    }

    public static double millisToSeconds(double milliSeconds){
        return milliSeconds / 1000.0;
    }
}
