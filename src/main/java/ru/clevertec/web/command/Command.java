package ru.clevertec.web.command;

import jakarta.servlet.http.HttpServletRequest;

public interface Command {

    String execute(HttpServletRequest req);
}
