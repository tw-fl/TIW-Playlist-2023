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

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GoHome")
public class GoHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private JavaxServletWebApplication application;

	public GoHome() {
		super();
	}

	public void init() throws ServletException {
		this.application = JavaxServletWebApplication.buildApplication(getServletContext());
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");

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
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get playlists from database");
			e.printStackTrace();
			return;
		}
		
		ArrayList<Song> allSongs = null;
		try {
			allSongs = uDao.getAllUserSongs(u.getId(), false);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from database");
			e.printStackTrace();
			return;
		}
		
		String path = "/WEB-INF/Home.html";
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		final WebContext ctx = new WebContext(webExchange, request.getLocale());
		ctx.setVariable("playlists", playlists);
		ctx.setVariable("allSongs", allSongs);
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
