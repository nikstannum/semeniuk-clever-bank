package ru.clevertec.service;

import java.util.List;
import ru.clevertec.service.dto.UserCreateDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.dto.UserUpdateDto;
import ru.clevertec.web.util.PagingUtil.Paging;

public interface UserService {

    UserDto findById(Long id);

    List<UserDto> findAll(Paging paging);

    UserDto create(UserCreateDto dto);

    UserDto update(UserUpdateDto dto);

    void deleteById(Long id);
}
