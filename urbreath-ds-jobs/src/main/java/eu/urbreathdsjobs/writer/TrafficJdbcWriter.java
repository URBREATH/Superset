package eu.urbreathdsjobs.writer;

import javax.sql.DataSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrafficJdbcWriter {

    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<TrafficMeasurement> trafficMeasurementImportWriter() {

        JdbcBatchItemWriter<TrafficMeasurement> writer =
                new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);

        writer.setSql("""
            INSERT INTO traffic_measurement(id_traffic_measure,
              segment_id,date,interval,uptime,direction,timezone,
              heavy,car,bike,pedestrian,night,
              heavy_lft,heavy_rgt,car_lft,car_rgt,
              bike_lft,bike_rgt,pedestrian_lft,pedestrian_rgt,
              night_lft,night_rgt,v85
            )
            VALUES (
              :idTrafficMeasure,
              :segmentId,:date,:interval,:uptime,:direction,:timezone,
              :heavy,:car,:bike,:pedestrian,:night,
              :heavyLft,:heavyRgt,:carLft,:carRgt,
              :bikeLft,:bikeRgt,:pedestrianLft,:pedestrianRgt,
              :nightLft,:nightRgt,:v85
            )
        """);

        writer.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>());

        return writer;
    }
    
    @Bean
    public JdbcBatchItemWriter<TrafficHistogram> trafficHistogramImportWriter() {

        JdbcBatchItemWriter<TrafficHistogram> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);

        writer.setSql("""
            INSERT INTO traffic_speed_histogram(
              id_traffic_measure,histogram_type,bucket_index,value
            )
            VALUES (
        		:idTrafficMeasure,:histogramType,:bucketIndex,:value
            )
        """);

        writer.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>());

        return writer;
    }
    

    

}

