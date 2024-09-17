package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetSong")
@MultipartConfig
public class GetSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    public GetSong() {
        super();
    }

    public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
		}
		
		String songIdString = request.getParameter("songId");
		
		if(songIdString == null || songIdString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}

		int songId;
		try {
			songId = Integer.parseInt(songIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
			return;
		}
		
		SongDAO sDao = new SongDAO(this.connection);
		Song song = new Song();
		try {
			song = sDao.getSong(songId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get song from database");
			e.printStackTrace();
			return;
		}
		
		response.setContentType("application/json");
		response.getWriter().write(new Gson().toJson(song));
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
