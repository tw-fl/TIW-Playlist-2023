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
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetAllSongs")
public class GetAllSongs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public GetAllSongs() {
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
		
		boolean getAlbumCovers = false;
		String getAlbumCoversString = request.getParameter("getAlbumCovers");
		if(getAlbumCoversString != null && getAlbumCoversString.equals("true")) {
			getAlbumCovers = true;
		} else {
			getAlbumCovers = false;
		}
		
		UserDAO uDao = new UserDAO(this.connection);
		ArrayList<Song> allSongs = null;
		try {
			allSongs = uDao.getAllUserSongs(u.getId(), getAlbumCovers);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from database");
			e.printStackTrace();
			return;
		}
		
		response.setContentType("application/json");
		response.getWriter().write(new Gson().toJson(allSongs));
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
