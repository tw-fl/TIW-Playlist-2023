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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetSong")
public class GetSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private JavaxServletWebApplication application;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    public GetSong() {
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
		String playlistIdString = request.getParameter("playlistId");
		
		if(songIdString == null || songIdString.isEmpty() || playlistIdString == null || playlistIdString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}

		int songId, playlistId;
		try {
			songId = Integer.parseInt(songIdString);
			playlistId = Integer.parseInt(playlistIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			e.printStackTrace();
			return;
		}
		
		String playlistName = "";
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		try {
			playlistName = pDao.getPlaylistTitle(playlistId, u.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get playlist title from database");
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
		
		String path = "/WEB-INF/SongPage.html";
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		final WebContext ctx = new WebContext(webExchange, request.getLocale());
		ctx.setVariable("song", song);
		ctx.setVariable("songId", songIdString);
		ctx.setVariable("playlistId", playlistIdString);
		ctx.setVariable("playlistName", playlistName);
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
