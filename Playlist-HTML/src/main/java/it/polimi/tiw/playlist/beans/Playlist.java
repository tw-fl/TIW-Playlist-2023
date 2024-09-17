package it.polimi.tiw.playlist.beans;

import java.time.LocalDateTime;

public class Playlist {
	private int id;
	private int userID;
	private String name;
	private LocalDateTime creationDateTime;

	public int getId() {
		return id;
	}
	
	public int getUserID() {
		return userID;
	}

	public String getName() {
		return name;
	}

	public LocalDateTime getCreationDateTime() {
		return creationDateTime;
	}
	
	public void setId(int i) {
		id = i;
	}
	
	public void setUserID(int ui) {
		userID = ui;
	}

	public void setName(String un) {
		name = un;
	}

	public void setCreationDateTime(LocalDateTime ldt) {
		creationDateTime = ldt;		
	}
}
