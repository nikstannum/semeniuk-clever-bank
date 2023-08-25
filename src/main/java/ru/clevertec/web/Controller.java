package ru.clevertec.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.factory.CommandFactory;

@WebServlet("/")
public class Controller extends HttpServlet {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    private static final String PUT = "PUT";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        String command = parts[0] + GET;
        Command instance = getCommand(command);
        String result = instance.execute(req);
        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.getWriter().print(result);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(1);
        Command instance = getCommand(uri + POST);
        String result = instance.execute(req);
        resp.setStatus(201);
        resp.setContentType("application/json");
        resp.getWriter().print(result);

    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        String command = parts[0] + PUT;
        Command instance = getCommand(command);
        String result = instance.execute(req);
        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.getWriter().print(result);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        String command = parts[0] + DELETE;
        Command instance = getCommand(command);
        instance.execute(req);
        resp.setStatus(204);
    }

    private static Command getCommand(String command) {
        CommandFactory factory = CommandFactory.INSTANCE;
        return factory.getCommand(command);
    }

    @Override
    public void destroy() {
        try {
            DataSource.INSTANCE.close();
        } catch (Exception e) {
            // FIXME add logging
        }
    }
}
