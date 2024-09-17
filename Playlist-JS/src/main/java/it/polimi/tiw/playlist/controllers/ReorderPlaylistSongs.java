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
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/ReorderPlaylistSongs")
@MultipartConfig
public class ReorderPlaylistSongs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

    public ReorderPlaylistSongs() {
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
		
		String songIdsString = request.getParameter("songIds");
		String playlistIdString = request.getParameter("playlistId");
		if(songIdsString == null || songIdsString.isEmpty() || playlistIdString == null || playlistIdString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		int playlistId;
		String[] strings;
		ArrayList<Integer> songIds = new ArrayList<>();
		try {
			strings = songIdsString.split(",");
			for(String string : strings) {
				songIds.add(Integer.parseInt(string));
			}
			playlistId = Integer.parseInt(playlistIdString);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
			return;
		}
		
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		try {
			pDao.setPlaylistOrder(playlistId, songIds, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error in setting playlist order");
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
