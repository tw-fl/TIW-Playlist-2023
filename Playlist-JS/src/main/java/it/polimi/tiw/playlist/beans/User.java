package it.polimi.tiw.playlist.beans;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String username;

	public int getId() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setId(int i) {
		id = i;
	}
	
	public void setUsername(String un) {
		username = un;
	}

}
