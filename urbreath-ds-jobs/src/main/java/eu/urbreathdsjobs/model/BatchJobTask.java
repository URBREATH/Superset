package eu.urbreathdsjobs.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BatchJobTask {

    private Long id;
    private Integer idBatch;
    private String jsonParam;
    private Integer status;
    private LocalDateTime dateIns;
    private LocalDateTime dateMod;
}

