package com.routeanalyzer.api.common;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DateUtilsTest {

    private static ZonedDateTime zonedDateTime;
    private static Long timeMillis;
    private static Long timeMillisNull;
    private static ZonedDateTime zonedDateTimeNull;
    private static ZonedDateTime utcZonedDateTime;
    private static Instant instantNull;
    private static GregorianCalendar gregorianCalendar;
    private static GregorianCalendar gregorianCalendarNull;
    private static GregorianCalendar utcGregorianCalendar;
    private static XMLGregorianCalendar xmlGregorianCalendar;
    private static XMLGregorianCalendar utcXmlGregorianCalendar;
    private static XMLGregorianCalendar xmlGregorianCalendarNull;
    private static Date date;
    private static Date utcDate;

    @BeforeClass
    public static void setUpClass() throws Exception {
        zonedDateTime = ZonedDateTime.of(2020, 05, 12,
                17, 36, 00, 00, ZoneId.of("Europe/Madrid"));
        utcZonedDateTime = ZonedDateTime.of(2020, 05, 12,
                15, 36, 00, 00, ZoneId.of("UTC"));
        timeMillis = 1589297760000L;

        date = new Date(timeMillis);
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);

        xmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(gregorianCalendar);

        utcDate = new Date(1589290560000L);
        utcGregorianCalendar = new GregorianCalendar();
        utcGregorianCalendar.setTime(utcDate);

        utcXmlGregorianCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(utcGregorianCalendar);
    }

    @Test
    public void toDate() {
        // Given

        // When
        Optional<Date> result = DateUtils.toDate(zonedDateTime);

        // Then
        assertThat(result).contains(new Date(timeMillis));
    }

    @Test
    public void nullToDate() {
        // Given


        // When
        Optional<Date> result = DateUtils.toDate(zonedDateTimeNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void toTimeMillis() {
        // Given


        // When
        Optional<Long> result = DateUtils.toTimeMillis(zonedDateTime);

        // Then
        assertThat(result).contains(timeMillis);
    }

    @Test
    public void nullToTimeMillis() {
        // Given

        // When
        Optional<Long> result = DateUtils.toTimeMillis(zonedDateTimeNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void timeMillisToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(timeMillis);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    public void nullTimeMillisToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(timeMillisNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(zonedDateTime.toInstant());

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    public void nullInstantToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(instantNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testToZonedDateTime1() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(gregorianCalendar);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    public void nullGregorianToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(gregorianCalendarNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testToZonedDateTime2() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(xmlGregorianCalendar);

        // Then
        assertThat(result).contains(utcZonedDateTime);
    }

    @Test
    public void nullXmlGregorianToZonedDateTime() {
        // Given

        // When
        Optional<ZonedDateTime> result = DateUtils.toUtcZonedDateTime(xmlGregorianCalendarNull);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void createGregorianCalendar() {
        // Given

        // When
        GregorianCalendar result = DateUtils.createGregorianCalendar(utcDate);

        // Then
        assertThat(result).isEqualTo(utcGregorianCalendar);
    }

    @Test
    public void createGregorianCalendarPassingNullParam() {
        // Given

        // When
        GregorianCalendar result = DateUtils.createGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void createXmlGregorianCalendar() {
        // Given

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(utcGregorianCalendar);

        // Then
        assertThat(result).isEqualTo(utcXmlGregorianCalendar);
    }

    @Test
    public void createXmlGregorianCalendarPassingNullParam() {
        // Given

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void createXmlGregorianCalendarFromNull() {
        // Given

        // When
        XMLGregorianCalendar result = DateUtils.createXmlGregorianCalendar(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void millisToSeconds() {
        // Given
        double millis = 123000d;

        // When
        Double result = DateUtils.millisToSeconds(millis);

        // Then
        assertThat(result).isEqualTo(123d);
    }

    @Test
    public void nullMillisToSeconds() {
        // Given
        Double nullValue = null;

        // When
        Double result = DateUtils.millisToSeconds(nullValue);

        // Then
        assertThat(result).isNull();
    }
}
