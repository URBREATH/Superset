package eu.urbreathdsjobs.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import eu.urbreathdsjobs.model.TrafficCsvRow;
import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;

@Component
public class TrafficProcessor
        implements ItemProcessor<TrafficCsvRow, TrafficMeasurement> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public TrafficMeasurement process(TrafficCsvRow row) throws Exception {

        TrafficMeasurement m = new TrafficMeasurement();

        Snowflake snowflake = IdUtil.getSnowflake(1, 1);

        long idTrafficMeasurement = snowflake.nextId();
        
        m.setIdTrafficMeasure(idTrafficMeasurement);        
        m.setSegmentId(row.getSegment_id());
        m.setDate(row.getDate());
        m.setInterval(row.getInterval());
        m.setUptime(row.getUptime());
        m.setDirection(row.getDirection());
        m.setTimezone(row.getTimezone());

        m.setHeavy(row.getHeavy());
        m.setCar(row.getCar());
        m.setBike(row.getBike());
        m.setPedestrian(row.getPedestrian());
        m.setNight(row.getNight());

        m.setHeavyLft(row.getHeavy_lft());
        m.setHeavyRgt(row.getHeavy_rgt());
        m.setCarLft(row.getCar_lft());
        m.setCarRgt(row.getCar_rgt());
        m.setBikeLft(row.getBike_lft());
        m.setBikeRgt(row.getBike_rgt());
        m.setPedestrianLft(row.getPedestrian_lft());
        m.setPedestrianRgt(row.getPedestrian_rgt());
        m.setNightLft(row.getNight_lft());
        m.setNightRgt(row.getNight_rgt());

        m.setV85(row.getV85());

        List<TrafficHistogram> histogram70List = new ArrayList<>();       
        if (row.getCar_speed_hist_0to70plus() != null) {
        	
        	
            List<Double> h70List = mapper.readValue(
                    row.getCar_speed_hist_0to70plus(),
                    new TypeReference<>() {});
            
            for (int i = 0; i < h70List.size() ; i++) {
            	TrafficHistogram h70 = new TrafficHistogram();
            	h70.setIdTrafficMeasure(idTrafficMeasurement);
            	h70.setBucketIndex(i);
            	h70.setHistogramType("0_70");
            	h70.setValue(h70List.get(i));
            	histogram70List.add(h70);
            }
            
            

        } 
        m.setCarSpeedHist0to70plus(histogram70List);
        
        List<TrafficHistogram> histogram120List = new ArrayList<>();
        if (row.getCar_speed_hist_0to120plus() != null) {
        	
        	
            List<Double> h120List = mapper.readValue(
                    row.getCar_speed_hist_0to120plus(),
                    new TypeReference<>() {});
            
            for (int i = 0; i < h120List.size() ; i++) {
            	TrafficHistogram h120 = new TrafficHistogram();
            	h120.setIdTrafficMeasure(idTrafficMeasurement);
            	h120.setBucketIndex(i);
            	h120.setHistogramType("0_120");
            	h120.setValue(h120List.get(i));
            	histogram120List.add(h120);
            }

        }
        m.setCarSpeedHist0to120plus(histogram120List);

        return m;
    }
}

