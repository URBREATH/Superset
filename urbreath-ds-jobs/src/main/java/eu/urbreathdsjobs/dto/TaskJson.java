package eu.urbreathdsjobs.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TaskJson implements Serializable {
	
	private List<TaskJsonItem> items;

}
