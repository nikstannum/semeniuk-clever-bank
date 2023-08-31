package ru.clevertec.web.command.impl.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.UserService;
import ru.clevertec.service.dto.UserCreateDto;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PostUserCommand implements Command {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            UserCreateDto dto = objectMapper.readValue(bytes, UserCreateDto.class);
            return objectMapper.writeValueAsString(userService.create(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
