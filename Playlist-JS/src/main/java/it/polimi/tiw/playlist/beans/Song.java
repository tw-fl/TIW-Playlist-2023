package it.polimi.tiw.playlist.beans;

public class Song {
	private int id;
	private String title;
	private String encodedAlbumCover;
	private String encodedSong;
	private String album;
	private String artist;
	private int year;
	private int userId;
	private String genre;
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getEncodedAlbumCover() {
		return encodedAlbumCover;
	}
	
	public String getEncodedSong() {
		return encodedSong;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setEncodedAlbumCover(String encodedAlbumCover) {
		this.encodedAlbumCover = encodedAlbumCover;
	}
	
	public void setEncodedSong(String encodedSong) {
		this.encodedSong = encodedSong;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
}
