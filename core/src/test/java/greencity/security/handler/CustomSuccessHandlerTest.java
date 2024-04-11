package greencity.security.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import greencity.exception.exceptions.AuthenticationFailureException;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.service.CustomSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@ExtendWith(MockitoExtension.class)
class CustomSuccessHandlerTest {

    @InjectMocks
    private CustomSuccessHandler customSuccessHandler;

    @Mock
    private CustomSecurityService customSecurityService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private DefaultOAuth2User defaultOAuth2User;


    @SneakyThrows
    @Test
    void onAuthentication_withCorrectParameters_doesNoThrowException() {
        var successSignInDto = new SuccessSignInDto();
        successSignInDto.setAccessToken("access");
        successSignInDto.setRefreshToken("refresh");

        when(authentication.getPrincipal()).thenReturn(defaultOAuth2User);
        when(customSecurityService.authenticate(any())).thenReturn(successSignInDto);
        doNothing().when(response).sendRedirect(anyString());

        assertDoesNotThrow(() -> customSuccessHandler.onAuthenticationSuccess(request, response, authentication));

        verify(customSecurityService).authenticate(any());
        verify(response).sendRedirect(anyString());
    }

    @ParameterizedTest
    @NullSource
    void onAuthentication_withNullAuthentication_throwsException(Authentication auth) {
        assertThrows(AuthenticationFailureException.class,
                () -> customSuccessHandler.onAuthenticationSuccess(request, response, auth));
    }
}
