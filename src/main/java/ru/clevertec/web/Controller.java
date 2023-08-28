package ru.clevertec.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.factory.BeanFactory;

@WebServlet("/")
public class Controller extends HttpServlet {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String URI_DIVIDER = "/";
    private static final String ATTR_NAME_EXC = "exc";
    private static final int CODE_OK = 200;
    private static final String COMMAND_NAME_ERROR = "error";
    private static final String CONTENT_TYPE_APP_JSON = "application/json";
    private static final int CODE_CREATED = 201;
    private static final int CODE_NO_CONTENT = 204;

    private Command getCommand(String command) {
        BeanFactory factory = BeanFactory.INSTANCE;
        return (Command) factory.getBean(command);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String uri = req.getRequestURI().substring(1);
            String[] parts = uri.split(URI_DIVIDER);
            String command = parts[0] + GET;
            Command instance = getCommand(command);
            String result = instance.execute(req, resp);
            sendResponse(resp, CODE_OK, result);
        } catch (Exception e) {
            req.setAttribute(ATTR_NAME_EXC, e);
            Command instance = getCommand(COMMAND_NAME_ERROR);
            String result = instance.execute(req, resp);
            resp.setContentType(CONTENT_TYPE_APP_JSON);
            resp.getWriter().print(result);
        }
    }

    private void sendResponse(HttpServletResponse resp, int sc, String result) throws IOException {
        resp.setStatus(sc);
        resp.setContentType(CONTENT_TYPE_APP_JSON);
        resp.getWriter().print(result);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().substring(1);
        Command instance = getCommand(uri + POST);
        String result = instance.execute(req, resp);
        sendResponse(resp, CODE_CREATED, result);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        String command = parts[0] + PUT;
        Command instance = getCommand(command);
        String result = instance.execute(req, resp);
        sendResponse(resp, CODE_OK, result);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        String command = parts[0] + DELETE;
        Command instance = getCommand(command);
        instance.execute(req, resp);
        resp.setStatus(CODE_NO_CONTENT);
    }
}
