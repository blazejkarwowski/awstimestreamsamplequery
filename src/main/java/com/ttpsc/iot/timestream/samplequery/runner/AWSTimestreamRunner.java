package com.ttpsc.iot.timestream.samplequery.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.timestream.jdbc.TimestreamConnection;
import software.amazon.timestream.jdbc.TimestreamDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class AWSTimestreamRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try {
            String region = "eu-west-1";
            if(args.length > 1 && !args[1].isEmpty()) {
                region = args[1];
            }
            Connection connection;
            if(args.length > 0 && "DriverManager".equals(args[0])) {
                connection = DriverManager.getConnection("jdbc:timestream://Region="+region, new Properties());
            } else {
                TimestreamDataSource dataSource = new TimestreamDataSource();
                dataSource.setRegion(region);
                //dataSource.setEndpoint("query.timestream."+region+".amazonaws.com");
                dataSource.setRequestTimeout(1000);
                connection = dataSource.getConnection();
            }
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"test\".\"IoT\" LIMIT 10");
            PrintResult(resultSet);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }
    }

    private void PrintResult(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> columns = new ArrayList<>(IntStream.range(1, columnCount+1).mapToObj(i -> {
            try {
                return metaData.getColumnName(i);
            } catch (SQLException throwables) {
                return "";
            }
        }).collect(Collectors.toList()));

        while (resultSet.next()) {
            String row = IntStream.range(1, columnCount+1).mapToObj(i -> {
                try {
                    return columns.get(i-1) + ":" + resultSet.getString(i);
                } catch (SQLException throwables) {
                    return "";
                }
            }).collect(Collectors.joining(", "));
            log.info("{} ", row);
        }
    }
}
