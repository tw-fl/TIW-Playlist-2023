package it.polimi.tiw.playlist.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Song;

public class SongDAO {
	private Connection connection;

	public SongDAO(Connection connection) {
		this.connection = connection;
	}

	public void uploadSong(String songName, String albumName, String artistName, int year, String genre,
			InputStream albumCover, InputStream audioFile, int userId) throws SQLException {
		String query = "INSERT INTO playlist_db.song (title, album, image, artist, year, genre, audio, owner) VALUES (?,?,?,?,?,?,?,?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, songName);
			pstatement.setString(2, albumName);
			pstatement.setBinaryStream(3, albumCover);
			pstatement.setString(4, artistName);
			pstatement.setInt(5, year);
			pstatement.setString(6, genre);
			pstatement.setBinaryStream(7, audioFile);
			pstatement.setInt(8, userId);
			pstatement.executeUpdate();
		}	
	}
	
	public void addSongToPlaylist(int songId, int playlistId, int userId) throws SQLException {
		
		//Check if the song and the playlist actually belong to the user
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		pDao.checkPlaylistOwner(playlistId, userId);
		this.checkSongOwner(songId, userId);
		
		//Check if the song is already in the playlist
		String query = "SELECT COUNT(*) AS total FROM playlist_db.playlist_song WHERE playlist_id = ? AND song_id = ? AND owner_id = ?";
		int count = 0;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setInt(2, songId);
			pstatement.setInt(3, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count > 0) {
			throw new SQLException("Song already in playlist");
		}
		
		query = "INSERT INTO playlist_db.playlist_song (song_id, playlist_id, owner_id) VALUES (?,?,?)";
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setInt(2, playlistId);
			pstatement.setInt(3, userId);
			pstatement.executeUpdate();
		}
	}
	
	public void addSongsToPlaylist(ArrayList<Integer> songList, int playlistId, int userId) throws SQLException {
		connection.setAutoCommit(false);
		if(songList.size() > 0) {
			try {
				for(int songId : songList) {
					this.addSongToPlaylist(songId, playlistId, userId);
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
			connection.setAutoCommit(true);
		}
	}
	
	public Song getSong(int songId, int userId) throws SQLException {
		this.checkSongOwner(songId, userId);
		
		String query = "SELECT title, album, image, artist, year, genre, audio FROM playlist_db.song WHERE id = ? AND owner = ?";
		Song song = new Song();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					song.setId(songId);
					song.setTitle(result.getString("title"));
					song.setAlbum(result.getString("album"));
					song.setEncodedAlbumCover(Base64.getEncoder().encodeToString(result.getBytes("image")));
					song.setArtist(result.getString("artist"));
					song.setYear(result.getInt("year"));
					song.setGenre(result.getString("genre"));
					song.setEncodedSong(Base64.getEncoder().encodeToString(result.getBytes("audio")));
					song.setUserId(userId);
				}
			}
		}
		return song;
	}

	public void deleteSong(int songId, int userId) throws SQLException {
		this.checkSongOwner(songId, userId);
		
		//Delete song from all playlists
		String query = "DELETE FROM playlist_db.playlist_song WHERE song_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.executeUpdate();
		}
		
		//Delete song data
		query = "DELETE FROM playlist_db.song WHERE id = ?";		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.executeUpdate();
		}
	}
	
	public void checkSongOwner(int songId, int userId) throws SQLException {
		int count = 0;
		String query = "SELECT COUNT(*) AS total FROM playlist_db.song WHERE id = ? AND owner = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setInt(2, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count <= 0) {
			throw new SQLException("Invalid song");
		}
	}
}
