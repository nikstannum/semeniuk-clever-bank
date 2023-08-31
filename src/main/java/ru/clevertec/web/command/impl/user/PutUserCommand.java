package ru.clevertec.web.command.impl.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.UserService;
import ru.clevertec.service.dto.UserUpdateDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PutUserCommand implements Command {
    private static final String EXC_MSG_INCOMING_ID_IN_BODY_DOESN_T_MATCH_PATH = "Incoming id in body doesn't match path";
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        String idStr = req.getRequestURI().substring(1).split("/")[1];
        Long id = Long.parseLong(idStr);
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            UserUpdateDto dto = objectMapper.readValue(bytes, UserUpdateDto.class);
            if (!id.equals(dto.getId())) {
                throw new BadRequestException(EXC_MSG_INCOMING_ID_IN_BODY_DOESN_T_MATCH_PATH);
            }
            return objectMapper.writeValueAsString(userService.update(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
