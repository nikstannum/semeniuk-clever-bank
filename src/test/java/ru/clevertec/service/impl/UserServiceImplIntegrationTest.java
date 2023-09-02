package ru.clevertec.service.impl;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.UserService;
import ru.clevertec.service.dto.UserCreateDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.dto.UserUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;

public class UserServiceImplIntegrationTest {
    private static UserService service;
    private final BaseIntegrationTest baseIntegrationTest = new BaseIntegrationTest();

    @BeforeAll
    static void beforeAll() {
        BeanFactory factory = BeanFactory.INSTANCE;
        service = (UserService) factory.getBean("userService");
    }

    @BeforeEach
    void setUp() {
        baseIntegrationTest.runMigration();
    }

    @Test
    void checkFindByIdShouldReturnNotNull() {
        UserDto dto = service.findById(1L);
        assertThat(dto).isNotNull();
    }

    @Test
    void checkFindByIdShouldThrowNotFoundExc() {
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(0L));
    }

    @Test
    void checkFindAllShouldHasSize2() {
        Paging paging = new Paging(2, 0);
        List<UserDto> list = service.findAll(paging);
        assertThat(list).hasSize(2);
    }

    @Test
    void checkCreateShouldThrowEntityExistsException() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("ivanov@mail.com");
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        Assertions.assertThrows(EntityExistsException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldReturnNotNull() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("email@mail.com");
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        UserDto actual = service.create(dto);
        assertThat(actual).isNotNull();
    }

    @Test
    void checkUpdateShouldThrowEntityExistsExc() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("ivanov@mail.com");
        dto.setId(100L);
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        Assertions.assertThrows(EntityExistsException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("email@email.com");
        dto.setId(1L);
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        UserDto actual = service.update(dto);
        assertThat(actual.getEmail()).isEqualTo("email@email.com");
    }

    @Test
    void checkDeleteShouldSuccess() {
        service.deleteById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(1L));
    }


}
