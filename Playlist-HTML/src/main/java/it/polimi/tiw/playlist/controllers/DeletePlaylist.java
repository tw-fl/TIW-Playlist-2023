package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/DeletePlaylist")
public class DeletePlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

    public DeletePlaylist() {
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
		
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		try {
			pDao.deletePlaylist(playlistId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't delete playlist");
			e.printStackTrace();
			return;
		}
		 
		String path = getServletContext().getContextPath();
		String target = ("/GoHome");
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
