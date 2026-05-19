package eu.urbreathdsjobs.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskJsonItem implements Serializable {
	
	private String key;
	private String value;
	private TaskJsonItemType type;

}
