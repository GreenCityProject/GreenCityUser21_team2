package greencity.security.handler;

import static greencity.constant.GoogleConstants.SUCCESS_AUTH_REDIRECT_URL;
import static java.lang.String.format;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.GoogleAuthenticationFailureException;
import greencity.security.dto.googlesecurity.GoogleUserDto;
import greencity.security.service.GoogleSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    private final GoogleSecurityService googleSecurityService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication auth) throws IOException {
        checkAuthenticationIsNotNull(auth);
        var googleUser = GoogleUserDto.buildFromOAuth2User((DefaultOAuth2User) auth.getPrincipal());
        var successSignIn = googleSecurityService.authenticateWithGoogle(googleUser);

        log.info("User {} is authenticated using Google provider", googleUser);

        response.sendRedirect(format(SUCCESS_AUTH_REDIRECT_URL,successSignIn.getAccessToken(),successSignIn.getRefreshToken()));
    }

    private void checkAuthenticationIsNotNull(Authentication auth) {
        if (Objects.isNull(auth))
            throw new GoogleAuthenticationFailureException(ErrorMessage.GOOGLE_AUTHENTICATION_FAILURE);
    }
}
