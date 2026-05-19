package eu.urbreathdsjobs.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BatchJobTask {

    private Long id;
    private Integer idBatch;
    private String jsonParam;
    private Integer status;
    private LocalDateTime dateIns;
    private LocalDateTime dateMod;
}

