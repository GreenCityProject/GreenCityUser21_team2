package greencity.security.handler;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.AuthenticationFailureException;
import greencity.security.dto.oauth2security.CustomUserDto;
import greencity.security.service.CustomSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

import static greencity.constant.GoogleConstants.SUCCESS_AUTH_REDIRECT_URL;
import static java.lang.String.format;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomSecurityService customSecurityService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication auth) throws IOException {
        checkAuthenticationIsNotNull(auth);

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) auth;
        var customUser = CustomUserDto.buildFromOAuth2User(oAuth2AuthenticationToken.getPrincipal());
        var successSignIn = customSecurityService.authenticate(customUser);

        String providerName = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        log.info("User {} is authenticated using {} provider ", customUser, providerName);

        response.sendRedirect(
                format(SUCCESS_AUTH_REDIRECT_URL, successSignIn.getAccessToken(), successSignIn.getRefreshToken()));
    }

    private void checkAuthenticationIsNotNull(Authentication auth) {
        if (Objects.isNull(auth)) throw new AuthenticationFailureException(ErrorMessage.GOOGLE_AUTHENTICATION_FAILURE);
    }
}
