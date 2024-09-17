package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetPlaylistSongs")
public class GetPlaylistSongs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetPlaylistSongs() {
		super();
	}

	public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//If there is no active session, return to the index
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
		}
		
		//Check if the request is missing the necessary parameters
		String playlistIdString = request.getParameter("playlistId");
		if(playlistIdString == null || playlistIdString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		int playlistId;
		try {
			playlistId = Integer.parseInt(playlistIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
			return;
		}
		
		
		//Get the list of songs in the playlist from the database
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		ArrayList<Song> dbSongs = null;
		try {
			dbSongs = pDao.getSongs(playlistId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from database");
			e.printStackTrace();
			return;
		}
		
		response.setContentType("application/json");
		response.getWriter().write(new Gson().toJson(dbSongs));
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
