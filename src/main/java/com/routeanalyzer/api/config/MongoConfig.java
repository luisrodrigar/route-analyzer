package com.routeanalyzer.api.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.internal.connection.ServerAddressHelper;
import com.routeanalyzer.api.database.ZonedDateTimeReadConverter;
import com.routeanalyzer.api.database.ZonedDateTimeWriteConverter;
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

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

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


}
