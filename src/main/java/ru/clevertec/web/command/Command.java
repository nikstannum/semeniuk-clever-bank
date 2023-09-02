package ru.clevertec.web.command;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface for processing the request and sending responses to the user
 */
public interface Command {

    String execute(HttpServletRequest req, HttpServletResponse res);
}
