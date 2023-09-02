package ru.clevertec.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.service.UserService;
import ru.clevertec.service.dto.UserCreateDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.dto.UserUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto findById(Long id) {
        return toDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("wasn't found user with id = " + id)));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Override
    public List<UserDto> findAll(Paging paging) {
        return userRepository.findAll(paging.getLimit(), paging.getOffset()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserDto create(UserCreateDto dto) {
        String email = dto.getEmail();
        userRepository.findUserByEmail(email)
                .ifPresent(user -> {
                    throw new EntityExistsException("Already exists user with email " + email);
                });
        return toDto(userRepository.create(toEntity(dto)));
    }

    private User toEntity(UserCreateDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        return user;
    }

    private User toEntity(UserUpdateDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        return user;
    }

    @Override
    public UserDto update(UserUpdateDto dto) {
        String email = dto.getEmail();
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if (optionalUser.isPresent() && !optionalUser.get().getId().equals(dto.getId())) {
            throw new EntityExistsException("Already exists user with email " + email);
        }
        return toDto(userRepository.update(toEntity(dto)));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
