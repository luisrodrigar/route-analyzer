package com.routeanalyzer.api.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MongoProperties.class)
@EnableMongoRepositories(basePackages= "com.routeanalyzer.api.database")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final MongoProperties properties;

    private final List<Converter<?, ?>> converters = new ArrayList<>();


    @Override
    protected String getDatabaseName() {
        return properties.getMongoDatabase();
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(new ConnectionString(properties.getMongoUri()));
    }

    @Bean
    @Override
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory());
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext()) {
            @Override
            public void setCustomConversions(CustomConversions conversions) {
                super.setCustomConversions(conversions);
                conversions.registerConvertersIn(conversionService);
            }

        };
        converter.setCustomConversions(customConversions());
        return converter;
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new ZonedDateTimeReadConverter());
        converters.add(new ZonedDateTimeWriteConverter());
        return new MongoCustomConversions(converters);
    }

    class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }

    class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(final ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }
    }


}
