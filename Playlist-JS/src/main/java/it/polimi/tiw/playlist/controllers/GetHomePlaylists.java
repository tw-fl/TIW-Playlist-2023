package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.LocalDateTimeAdapter;

@WebServlet("/GetHomePlaylists")
public class GetHomePlaylists extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetHomePlaylists() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
		}
		
		UserDAO uDao = new UserDAO(this.connection);
		ArrayList<Playlist> playlists = null;
		try {
			playlists = uDao.findPlaylists(u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get playlists");
			e.printStackTrace();
			return;
		}
		
		response.setContentType("application/json");
		Gson gson = new GsonBuilder()
			    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			    .create();
		response.getWriter().write(gson.toJson(playlists));
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
