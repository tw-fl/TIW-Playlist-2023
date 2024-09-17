package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkCredentials(String usrn, String pwd) throws SQLException {
		User user = new User();
		String query = "SELECT  id, username FROM playlist_db.user  WHERE username = ? AND password = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
				}
			}
		}
		return user;
	}
	
	public void createAccount(String usrn, String pwd) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM playlist_db.user WHERE username = ?";
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count > 0) {
			throw new SQLException("Username is taken");
		}
		
		query = "INSERT INTO playlist_db.user (username, password) VALUES (?,?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			pstatement.executeUpdate();
		}
	}
	
	public void deleteAccount(int userId) throws SQLException {
		String query = "DELETE FROM playlist_db.playlist_song WHERE owner_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM playlist_db.song WHERE owner = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM playlist_db.playlist WHERE owner = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM playlist_db.user WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.executeUpdate();
		}
	}
	
	public ArrayList<Playlist> findPlaylists(int id) throws SQLException {
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		String query = "SELECT id, name, creationdatetime FROM playlist_db.playlist WHERE owner = ? ORDER BY playlist_db.playlist.creationdatetime DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Playlist playlist = new Playlist();
					playlist.setId(result.getInt("id"));
					playlist.setUserID(id);
					playlist.setName(result.getString("name"));
					playlist.setCreationDateTime(result.getTimestamp("creationdatetime").toLocalDateTime());
					playlists.add(playlist);
				}
			}
		}
		return playlists;
	}
	
	public ArrayList<Song> getAllUserSongs(int userId, boolean getAlbumCovers) throws SQLException {
		String query = "SELECT id, title, image FROM song WHERE owner = ? ORDER BY playlist_db.song.year DESC";
		ArrayList<Song> songs = new ArrayList<>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setId(result.getInt("id"));
					song.setTitle(result.getString("title"));
					song.setUserId(userId);
					if(getAlbumCovers) {
						song.setEncodedAlbumCover(Base64.getEncoder().encodeToString(result.getBytes("image")));						
					}
					songs.add(song);
				}
			}
		}
		return songs;
	}
}
