package greencity.aspects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import greencity.ModelUtils;
import greencity.controller.UserController;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.NotCurrentUserException;
import greencity.exception.exceptions.UserNotAuthenticatedException;
import greencity.service.UserService;
import lombok.SneakyThrows;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CurrentUserIdAspectTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private CurrentUserIdAspect aspect;

    private UserVO user;
    private Long userId;
    private JoinPoint joinPoint;
    private MethodSignature signature;

    @BeforeEach
    void setUp() {
        user = ModelUtils.getUserVO();
        userId = user.getId();

        joinPoint = mock(JoinPoint.class);
        signature = mock(MethodSignature.class);

        var authentication =new UsernamePasswordAuthenticationToken(user.getEmail(),"",null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @SneakyThrows
    @Test
    void validateCurrentUserIdParameter_withCurrentUserId_doesNotThrowException(){
        mockParameterExtractionLogic(joinPoint,signature);
        mockFindByIdMethodCall();

        assertDoesNotThrow(() -> aspect.validateCurrentUserIdParameter(joinPoint));

        verifyParameterExtractionLogic();
        verifyFindByIdMethodCall();
    }

    @SneakyThrows
    @Test
    void validateCurrentUserIdParameter_withNotCurrentUserId_throwsNotCurrentUserException(){
        userId = user.getId()+1;
        mockParameterExtractionLogic(joinPoint,signature);
        mockFindByIdMethodCall();

        assertThrows(NotCurrentUserException.class,
                () -> aspect.validateCurrentUserIdParameter(joinPoint));

        verifyParameterExtractionLogic();
        verifyFindByIdMethodCall();
    }


    @SneakyThrows
    @Test
    void validateCurrentUserIdParameter_withNotAuthenticatedUser_throwsUserNotAuthenticated(){
        SecurityContextHolder.getContext().setAuthentication(null);

        mockParameterExtractionLogic(joinPoint,signature);

        assertThrows(UserNotAuthenticatedException.class,
                () -> aspect.validateCurrentUserIdParameter(joinPoint));

        verifyParameterExtractionLogic();
    }

    private void mockParameterExtractionLogic(JoinPoint joinPoint, MethodSignature signature) throws NoSuchMethodException {
        var method = UserController.class.getMethod("getUserProfileInformation", Long.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{userId});
    }

    private void mockFindByIdMethodCall() {
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
    }

    private void verifyParameterExtractionLogic() {
        verify(joinPoint).getSignature();
        verify(signature).getMethod();
        verify(joinPoint).getArgs();
    }

    private void verifyFindByIdMethodCall() {
        verify(userService).findByEmail(anyString());
    }
}