package eu.urbreathdsjobs.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class TaskJson implements Serializable {
	
	private List<TaskJsonItem> items;

}
