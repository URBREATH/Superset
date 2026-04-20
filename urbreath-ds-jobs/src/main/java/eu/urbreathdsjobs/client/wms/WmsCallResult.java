package eu.urbreathdsjobs.client.wms;

import eu.urbreathdsjobs.client.wms.dto.WmsResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WmsCallResult {
    private WmsRequest request;
    private Integer statusCode;
    private WmsResponse response;
}

