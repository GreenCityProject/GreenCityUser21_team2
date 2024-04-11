package greencity.security.service;

import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadUserStatusException;
import greencity.repository.UserRepo;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.oauth2security.CustomUserDto;
import greencity.security.jwt.JwtTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static greencity.constant.ErrorMessage.USER_BLOCKED;
import static greencity.constant.ErrorMessage.USER_CREATED;
import static greencity.constant.ErrorMessage.USER_DEACTIVATED;
import static greencity.enums.UserStatus.ACTIVATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomSecurityServiceImplTest {

    private final String accessToken = "access";
    private final String refreshToken = "refresh";

    @InjectMocks
    private CustomSecurityServiceImpl googleSecurityService;

    @Mock
    private UserRepo userRepo;
    @Mock
    private JwtTool jwtTool;
    @Mock
    private ModelMapper modelMapper;

    private User user;
    private CustomUserDto customUserDto;
    private UserVO userVO;
    private SuccessSignInDto expected;

    @BeforeEach
    void setUp() {
        customUserDto = setUpCustomUserDto();
        user = setUpUser();
        userVO = setUpUserVO(user);
        expected = setUpExpectedSuccessSignIn(userVO);
    }

    private SuccessSignInDto setUpExpectedSuccessSignIn(UserVO userVO) {
        return SuccessSignInDto.builder().accessToken(accessToken).refreshToken(refreshToken).name(userVO.getName()).userId(userVO.getId()).ownRegistrations(false).build();
    }

    @Test
    void authenticateWithGoogle_whenUserDoesNotExist_expectSuccess() {
        var langId = 1L;

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(modelMapper.map(anyString(), eq(Long.class))).thenReturn(langId);
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserVO.class)).thenReturn(userVO);
        when(jwtTool.createAccessToken(userVO.getEmail(), userVO.getRole())).thenReturn(accessToken);
        when(jwtTool.createRefreshToken(userVO)).thenReturn(refreshToken);

        var actual = googleSecurityService.authenticate(customUserDto);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullSource
    void authenticateWithGoogle_whenGoogleUserIsNull_expectNullPointer(CustomUserDto googleUser) {
        assertThrows(NullPointerException.class, () -> googleSecurityService.authenticate(googleUser));
    }


    @Test
    void authenticateWithGoogle_whenUserExists_expectSuccess() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserVO.class)).thenReturn(userVO);

        when(jwtTool.createAccessToken(userVO.getEmail(), userVO.getRole())).thenReturn(accessToken);
        when(jwtTool.createRefreshToken(userVO)).thenReturn(refreshToken);

        var actual = googleSecurityService.authenticate(customUserDto);
        assertEquals(expected, actual);
    }

    @Test
    void authenticateWithGoogle_whenUserHasBlockedStatus_expectBadUserStatusException() {
        user.setUserStatus(UserStatus.BLOCKED);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        var badRequestException =
                assertThrows(BadUserStatusException.class, () -> googleSecurityService.authenticate(customUserDto));
        assertEquals(USER_BLOCKED, badRequestException.getMessage());

    }

    @Test
    void authenticateWithGoogle_whenUserHasCreatedStatus_expectBadUserStatusException() {
        user.setUserStatus(UserStatus.CREATED);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        var badRequestException =
                assertThrows(BadUserStatusException.class, () -> googleSecurityService.authenticate(customUserDto));
        assertEquals(USER_CREATED, badRequestException.getMessage());

    }

    @Test
    void authenticateWithGoogle_whenUserHasDeactivatedStatus_expectBadUserStatusException() {
        user.setUserStatus(UserStatus.DEACTIVATED);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        var badRequestException =
                assertThrows(BadUserStatusException.class, () -> googleSecurityService.authenticate(customUserDto));
        assertEquals(USER_DEACTIVATED, badRequestException.getMessage());

    }


    private CustomUserDto setUpCustomUserDto() {
        return new CustomUserDto("name", "firstName",
                "email", "picture", "uk");
    }

    private User setUpUser() {
        return User.builder()
                .id(1L)
                .name("name")
                .firstName("firstName")
                .email("email")
                .uuid(UUID.randomUUID().toString())
                .dateOfRegistration(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .refreshTokenKey(jwtTool.generateTokenKey())
                .lastActivityTime(LocalDateTime.now())
                .userStatus(ACTIVATED)
                .emailNotification(EmailNotification.DISABLED)
                .build();
    }

    private UserVO setUpUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .userStatus(user.getUserStatus())
                .build();
    }

}
