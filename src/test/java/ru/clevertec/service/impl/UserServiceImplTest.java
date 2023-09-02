package ru.clevertec.service.impl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.impl.UserRepositoryImpl;
import ru.clevertec.service.dto.UserCreateDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.dto.UserUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Captor
    ArgumentCaptor<Long> captor;
    @Mock
    private UserRepositoryImpl repository;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void checkFindByIdShouldThrowNotFoundExc() {
        doReturn(Optional.empty()).when(repository).findById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email@email.com");
        user.setDeleted(false);
        return user;
    }

    @Test
    void checkFindByIdShouldTReturnEquals() {
        doReturn(Optional.of(getUser())).when(repository).findById(1L);

        UserDto expected = new UserDto();
        expected.setId(1L);
        expected.setFirstName("firstName");
        expected.setLastName("lastName");
        expected.setEmail("email@email.com");

        UserDto actual = service.findById(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllShouldHasSize2() {
        doReturn(List.of(new User(), new User())).when(repository).findAll(2, 0);
        Paging paging = new Paging(2, 0);
        List<UserDto> actualList = service.findAll(paging);
        assertThat(actualList).hasSize(2);

    }

    @Test
    void checkCreateShouldThrowEntityExistsException() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("exists@email.com");
        User user = getUser();
        doReturn(Optional.of(user)).when(repository).findUserByEmail("exists@email.com");

        Assertions.assertThrows(EntityExistsException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldReturnEquals() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("email@email.com");
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        doReturn(Optional.empty()).when(repository).findUserByEmail("email@email.com");

        User user = getUser();
        User forCreation = getUser();
        forCreation.setId(null);
        doReturn(user).when(repository).create(forCreation);

        UserDto actual = service.create(dto);
        assertThat(actual.getId()).isEqualTo(1L);
    }

    @Test
    void checkUpdateShouldThrowEntityExistsExc() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(2L);
        dto.setEmail("email@email.com");
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        User existing = getUser();
        doReturn(Optional.of(existing)).when(repository).findUserByEmail("email@email.com");
        Assertions.assertThrows(EntityExistsException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1L);
        dto.setEmail("email@email.com");
        dto.setFirstName("firstName");
        dto.setLastName("lastName");
        User existing = getUser();
        doReturn(Optional.of(existing)).when(repository).findUserByEmail("email@email.com");
        doReturn(existing).when(repository).update(existing);
        UserDto expected = new UserDto();
        expected.setId(1L);
        expected.setFirstName("firstName");
        expected.setLastName("lastName");
        expected.setEmail("email@email.com");

        UserDto actual = service.update(dto);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void deleteById() {
        service.deleteById(1L);
        verify(repository).deleteById(captor.capture());
        Long captured = captor.getValue();
        assertThat(captured).isEqualTo(1L);

    }
}