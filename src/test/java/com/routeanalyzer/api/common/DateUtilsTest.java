package com.routeanalyzer.api.common;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
class DateUtilsTest {

    @Test
    void toDate() {
        // Given
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 05, 12,
                17, 36, 00, 00, ZoneId.of("Europe/Madrid"));
        Long timeMillis = 1589297760000L;

        // When
        Optional<Date> result = DateUtils.toDate(zonedDateTime);

        // Then
        assertThat(result).contains(new Date(timeMillis));
    }

    @Test
    void nullToDate() {
        // Given
        ZonedDateTime zonedDateTime = null;

        // When
        Optional<Date> result = DateUtils.toDate(zonedDateTime);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toTimeMillis() {
        // Given
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 05, 12,
                17, 36, 00, 00, ZoneId.of("Europe/Madrid"));
        Long timeMillis = 1589297760000L;

        // When
        Optional<Long> result = DateUtils.toTimeMillis(zonedDateTime);

        // Then
        assertThat(result).contains(timeMillis);
    }

    @Test
    void nullToTimeMillis() {
        // Given
        ZonedDateTime zonedDateTime = null;

        // When
        Optional<Long> result = DateUtils.toTimeMillis(zonedDateTime);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void timeMillisToZonedDateTime() {
        // Given
        Long utcTimeMillis = 1589297760000L;
        ZonedDateTime utcZonedDateTime = ZonedDateTime.of(2020, 05, 12,
                15, 36, 00, 00, ZoneId.of("UTC"));

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(utcTimeMillis);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    void nullTimeMillisToZonedDateTime() {
        // Given
        Long utcTimeMillis = null;

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(utcTimeMillis);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testToZonedDateTime() {
        // Given
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 05, 12,
                17, 36, 00, 00, ZoneId.of("Europe/Madrid"));
        ZonedDateTime utcZonedDateTime = ZonedDateTime.of(2020, 05, 12,
                15, 36, 00, 00, ZoneId.of("UTC"));

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(zonedDateTime.toInstant());

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    void nullInstantToZonedDateTime() {
        // Given
        Instant instant = null;

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(instant);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testToZonedDateTime1() {
        // Given
        ZonedDateTime utcZonedDateTime = ZonedDateTime.of(2020, 05, 12,
                15, 36, 00, 00, ZoneId.of("UTC"));
        Date utcDate = new Date(1589297760000L);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(utcDate);

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(gregorianCalendar);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    void nullGregorianToZonedDateTime() {
        // Given
        GregorianCalendar gregorianCalendar = null;

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(gregorianCalendar);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testToZonedDateTime2() throws DatatypeConfigurationException {
        // Given
        ZonedDateTime utcZonedDateTime = ZonedDateTime.of(2020, 05, 12,
                15, 36, 00, 00, ZoneId.of("UTC"));
        Date utcDate = new Date(1589297760000L);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(utcDate);
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(gregorianCalendar);


        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(xmlGregorianCalendar);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    void nullXmlGregorianToZonedDateTime() throws DatatypeConfigurationException {
        // Given
        XMLGregorianCalendar xmlGregorianCalendar = null;


        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(xmlGregorianCalendar);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createGregorianCalendar() {
        // Given
        Date utcDate = new Date(1589290560000L);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(utcDate);

        // When
        GregorianCalendar result = DateUtils.createGregorianCalendar(utcDate);

        // Then
        assertThat(result).isEqualTo(gregorianCalendar);
    }

    @Test
    void createGregorianCalendarPassingNullParam() {
        // Given

        // When
        GregorianCalendar result = DateUtils.createGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void createXmlGregorianCalendar() throws DatatypeConfigurationException {
        // Given
        Date utcDate = new Date(1589290560000L);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(utcDate);
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(gregorianCalendar);

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(gregorianCalendar);

        // Then
        assertThat(result).isEqualTo(xmlGregorianCalendar);
    }

    @Test
    void createXmlGregorianCalendarPassingNullParam() throws DatatypeConfigurationException {
        // Given

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void createXmlGregorianCalendarFromNull() {
        // Given

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void millisToSeconds() {
        // Given
        double millis = 123000d;

        // When
        Double result = DateUtils.millisToSeconds(millis);

        // Then
        assertThat(result).isEqualTo(123d);
    }

    @Test
    void nullMillisToSeconds() {
        // Given
        Double nullValue = null;

        // When
        Double result = DateUtils.millisToSeconds(nullValue);

        // Then
        assertThat(result).isNull();
    }
}
