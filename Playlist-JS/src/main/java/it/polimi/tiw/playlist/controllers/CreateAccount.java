package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CreateAccount")
@MultipartConfig
public class CreateAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateAccount() {
		super();
	}

	public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String usrn = request.getParameter("username");
		String pwd = request.getParameter("pwd");
		
		if (usrn == null || usrn.isEmpty() || pwd == null || pwd.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		//At least 5 characters with letters, numbers and underscores
		Pattern userPattern = Pattern.compile("^[a-zA-Z0-9_]{5,}$");
		//At least 5 characters with letters, numbers and special characters
		Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_.!@$%^&]{5,}$");
		if(!userPattern.matcher(usrn).matches() || !passwordPattern.matcher(pwd).matches()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username or password.");
			return;
		}
		
		UserDAO usr = new UserDAO(connection);
		try {
			usr.createAccount(usrn, pwd);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't create account");
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
