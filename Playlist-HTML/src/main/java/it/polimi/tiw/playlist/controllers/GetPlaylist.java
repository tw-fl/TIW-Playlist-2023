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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetPlaylist")
public class GetPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private JavaxServletWebApplication application;
	private TemplateEngine templateEngine;

	public GetPlaylist() {
		super();
	}

	public void init() throws ServletException {
		this.application = JavaxServletWebApplication.buildApplication(getServletContext());
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
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
		
		//Default page parameter
		int page;
		String pageString = request.getParameter("page");
		if(pageString == null || pageString.isEmpty()) {
			page = 1;
		}
		else {
			try {
				page = Integer.parseInt(pageString);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
				e.printStackTrace();
				return;
			}
		}
		
		//Count how many songs the playlist contains to decide whether the provided page number is valid
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		int count = 0;
		try {
			count = pDao.countSongs(playlistId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get song count from the database");
			e.printStackTrace();
			return;
		}
		
		if(page > ((count-1)/5)+1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid page");
			return;
		}
		
		//Get the list of songs in the playlist from the database, filtering them to get at most 5
		ArrayList<Song> dbSongs = null;
		try {
			dbSongs = pDao.getSongsFiltered(playlistId, u.getId(), (page - 1) * 5);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from the database");
			e.printStackTrace();
			return;
		}
		
		//Get the playlist's name
		String playlistName = null;
		try {
			playlistName = pDao.getPlaylistTitle(playlistId, u.getId());
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get playlist name from database");
			e.printStackTrace();
			return;
		}
		
		//If there are no songs, set a variable
		boolean noSongs = false;
		if(dbSongs == null || dbSongs.isEmpty()) {
			noSongs = true;
		}
		
		//Set some parameters which will be used by Thymeleaf to decide whether to create certain buttons
		boolean lastPage = false;
		if(dbSongs.size() < 5 || page * 5 == count) {
			lastPage = true;
		}
		boolean firstPage = false;
		if(page - 1 == 0) {
			firstPage = true;
		}
		
		//Get the list of all the songs which aren't in the playlist
		ArrayList<Song> excludedSongs = null;
		try {
			excludedSongs = pDao.getSongsNotInPlaylist(u.getId(), playlistId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from the database");
			e.printStackTrace();
			return;
		}
		
		//Set context variables and process the template
		String path = "/WEB-INF/PlaylistPage.html";
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		final WebContext ctx = new WebContext(webExchange, request.getLocale());
		ctx.setVariable("songs", dbSongs);
		ctx.setVariable("firstPage", firstPage);
		ctx.setVariable("lastPage", lastPage);
		ctx.setVariable("page", page);
		ctx.setVariable("excludedSongs", excludedSongs);
		ctx.setVariable("playlistId", request.getParameter("playlistId"));
		ctx.setVariable("playlistName", playlistName);
		ctx.setVariable("noSongs", noSongs);
		templateEngine.process(path, ctx, response.getWriter());
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
