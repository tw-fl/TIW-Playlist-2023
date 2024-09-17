package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Song;

public class PlaylistDAO {
	private Connection connection;

	public PlaylistDAO(Connection connection) {
		this.connection = connection;
	}
	
	//Create a new playlist. If a set of songs is provided, add them to the new playlist
	public void createPlaylist(String name, ArrayList<Integer> songList, int userId) throws SQLException {
		connection.setAutoCommit(false);
		
		String query = "INSERT INTO playlist_db.playlist (name, owner, creationdatetime) VALUES (?,?,?)";
		int playlistId;
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, name);
			pstatement.setInt(2, userId);
			pstatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
			pstatement.executeUpdate();
			
			ResultSet result = pstatement.getGeneratedKeys();
			result.next();
			playlistId = result.getInt(1);
			
			result.close();
		}
		
		if(songList.size() > 0) {
			SongDAO sDao = new SongDAO(this.connection);
			try {
				for(int songId : songList) {
					sDao.addSongToPlaylist(songId, playlistId, userId);
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			} finally {
				connection.setAutoCommit(true);
			}
		}
		else {
			connection.commit();
			connection.setAutoCommit(true);
		}
	}
	
	public ArrayList<Song> getSongsFiltered(int playlistId, int userId, int offset) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);
		
		String query = "SELECT id, title, image, album, artist, year FROM playlist_db.song WHERE id IN (SELECT song_id FROM playlist_db.playlist_song WHERE playlist_id = ? AND owner_id = ?) ORDER BY playlist_db.song.year DESC LIMIT 5 OFFSET ?";
		ArrayList<Song> songs = new ArrayList<>();
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			pstatement.setInt(3, offset);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setId(result.getInt("id"));
					song.setTitle(result.getString("title"));
					song.setEncodedAlbumCover(Base64.getEncoder().encodeToString(result.getBytes("image")));
					song.setYear(result.getInt("year"));
					song.setUserId(userId);
					songs.add(song);
				}
			}
		}
		
		return songs;
	}
	
	public void removeSongFromPlaylist(int songId, int playlistId, int userId) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);
		SongDAO sDao = new SongDAO(this.connection);
		sDao.checkSongOwner(songId, userId);
		
		String query = "DELETE FROM playlist_db.playlist_song WHERE song_id = ? AND playlist_id = ? AND owner_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setInt(2, playlistId);
			pstatement.setInt(3, userId);
			pstatement.executeUpdate();
		}
	}
	
	public String getPlaylistTitle(int playlistId, int userId) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);
		
		String query = "SELECT name FROM playlist_db.playlist WHERE id = ? AND owner = ?";
		String title = null;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {					
					title = result.getString("name");
				}
			}
		}
		return title;
	}
	
	public void deletePlaylist(int playlistId, int userId) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);

		String query = "DELETE FROM playlist_db.playlist_song WHERE playlist_id = ? AND owner_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM playlist_db.playlist WHERE id = ? AND owner = ?";		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			pstatement.executeUpdate();
		}
		
	}
	
	public int countSongs (int playlistId, int userId) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);
		
		String query = "SELECT COUNT(*) AS total FROM playlist_db.playlist_song WHERE playlist_id = ? AND owner_id = ?";
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		return count;
	}
	
	public ArrayList<Song> getSongsNotInPlaylist(int userId, int playlistId) throws SQLException {
		this.checkPlaylistOwner(playlistId, userId);
		
		String query = "(SELECT id, title FROM playlist_db.song WHERE owner = ?) EXCEPT (SELECT id, title FROM playlist_db.song WHERE id IN (SELECT song_id FROM playlist_db.playlist_song WHERE playlist_id = ? AND owner_id = ?))";
		ArrayList<Song> songs = new ArrayList<>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.setInt(2, playlistId);
			pstatement.setInt(3, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setId(result.getInt("id"));
					song.setTitle(result.getString("title"));
					song.setUserId(userId);
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	public void checkPlaylistOwner(int playlistId, int userId) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM playlist_db.playlist WHERE id = ? AND owner = ?";
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count <= 0) {
			throw new SQLException("Invalid playlist");
		}
	}
}