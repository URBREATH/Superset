package eu.urbreathdsjobs.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaskJsonItem implements Serializable {
	
	private String key;
	private String value;
	private TaskJsonItemType type;

}
