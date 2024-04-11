package greencity.aspects;
import greencity.annotations.CurrentUserId;
import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.NotCurrentUserException;
import greencity.exception.exceptions.UserNotAuthenticatedException;
import greencity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class CurrentUserIdAspect {
    private final UserService userService;

    @Before("execution(public * greencity.controller..*.*(..,java.lang.Long,..))")
    public void validateCurrentUserIdParameter(JoinPoint joinPoint) {
        getUserIdParameterValue(joinPoint).ifPresent(this::checkIsIdOfCurrentUser);
    }

    private void checkIsIdOfCurrentUser(Long userId) {
        String userEmail = getEmailFromAuthentication();
        Long currentUserId = getCurrentUserId(userEmail);
        if (!currentUserId.equals(userId)) {
            throw new NotCurrentUserException(ErrorMessage.NOT_CURRENT_USER);
        }
    }

    private String getEmailFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.isNull(authentication))
            throw new UserNotAuthenticatedException(ErrorMessage.USER_NOT_AUTHENTICATED);

        return authentication.getName();
    }

    private Long getCurrentUserId(String userEmail) {
        return userService.findByEmail(userEmail).getId();
    }

    private Optional<Long> getUserIdParameterValue(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] annotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType().equals(CurrentUserId.class) && args[i] instanceof Long) {
                    return Optional.of((Long) args[i]);
                }
            }
        }
        return Optional.empty();
    }
}

