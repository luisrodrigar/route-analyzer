package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDateTime;
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
    public static Optional<Date> toDate(LocalDateTime localDateTime) {
        return toInstant(localDateTime)
                .map(Date::from);
    }

    public static Optional<Long> toTimeMillis(LocalDateTime localDateTime) {
        return toInstant(localDateTime)
                .map(Instant::toEpochMilli);
    }

    public static Optional<LocalDateTime> toLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .flatMap(DateUtils::toLocalDateTime);
    }

    public static Optional<LocalDateTime> toLocalDateTime(GregorianCalendar xmlGregorianCalendar) {
        return ofNullable(xmlGregorianCalendar)
                .map(GregorianCalendar::getTime)
                .map(Date::toInstant)
                .flatMap(DateUtils::toLocalDateTime);
    }

    public static Optional<LocalDateTime> toLocalDateTime(long timeMillis) {
        return ofNullable(timeMillis)
                .map(Instant::ofEpochMilli)
                .flatMap(DateUtils::toLocalDateTime);
    }

    public static Optional<LocalDateTime> toLocalDateTime(Instant instant) {
        return ofNullable(instant)
                .map(inst -> inst.atZone(ZoneId.systemDefault()))
                .map(ZonedDateTime::toLocalDateTime);
    }

    public static GregorianCalendar createGregorianCalendar(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        return gregorianCalendar;
    }

    public static XMLGregorianCalendar createXmlGregorianCalendar(GregorianCalendar gregorianCalendar) {
        return Try.of(() -> DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar))
                .getOrElse(() -> null);
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
