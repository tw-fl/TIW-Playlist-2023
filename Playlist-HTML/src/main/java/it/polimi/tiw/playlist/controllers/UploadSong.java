package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/UploadSong")
@MultipartConfig(
		fileSizeThreshold = 10000,
		maxFileSize = 1024 * 1024 * 1000,
		maxRequestSize = 1024 * 1024 * 10000
		)
public class UploadSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public UploadSong() {
        super();
    }

    public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
		}
		
		String songName = request.getParameter("songname");
		String albumName = request.getParameter("albumname");
		String artistName = request.getParameter("artistname");
		String yearString = request.getParameter("year");
		
		int year;
		try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
			return;
		}
		
		if(request.getParameterValues("genre").length > 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Too many parameters");
			return;
		}
		
		String genre = request.getParameter("genre");
		Part coverPart = request.getPart("albumcover");
		Part audioPart = request.getPart("audiofile");
		InputStream albumCover;
		InputStream audioFile;
		
		try {
			albumCover = coverPart.getInputStream();
			audioFile = audioPart.getInputStream();			
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file");
			e.printStackTrace();
			return;
		}
		
		/*String imageMimeType = URLConnection.guessContentTypeFromStream(albumCover);
		String audioMimeType = URLConnection.guessContentTypeFromStream(audioFile);*/
		
		if(songName == null || songName.isEmpty() || albumName == null || albumName.isEmpty() || artistName == null || artistName.isEmpty()
				|| yearString == null || yearString.isEmpty() || genre == null || genre.isEmpty() || albumCover == null || audioFile == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		String imageMimeType = getServletContext().getMimeType(coverPart.getSubmittedFileName());
		String audioMimeType = getServletContext().getMimeType(audioPart.getSubmittedFileName());
		
		if(imageMimeType == null) {
			albumCover.close();
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image");
			return;
		}
		if(audioMimeType == null) {
			albumCover.close();
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid audio");
			return;
		}
		if(!imageMimeType.startsWith("image")) {
			albumCover.close();
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image");
			return;
		}
		if(!audioMimeType.startsWith("audio")) {
			albumCover.close();
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid audio");
			return;
		}
		
		
		SongDAO sDAO = new SongDAO(this.connection);
		try {
			sDAO.uploadSong(songName, albumName, artistName, year, genre, albumCover, audioFile, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in uploading song");
			e.printStackTrace();
			return;
		}
		
		albumCover.close();
		audioFile.close();
		
		String path = getServletContext().getContextPath();
		String target = "/GoHome";
		path = path + target;
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
}
