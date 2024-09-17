package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/AddSongsToPlaylist")
@MultipartConfig
public class AddSongsToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public AddSongsToPlaylist() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
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
		
		String playlistIdString = request.getParameter("playlistId");
		if(playlistIdString == null || playlistIdString.isEmpty() || request.getParameterValues("songIds") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		ArrayList<Integer> songIds = new ArrayList<>();
		try {
			for(String str : request.getParameterValues("songIds")) {
				songIds.add(Integer.parseInt(str));
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
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
		
		SongDAO sDao = new SongDAO(this.connection);
		try {
			sDao.addSongsToPlaylist(songIds, playlistId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't add song to playlist");
			e.printStackTrace();
			return;
		}
		
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
