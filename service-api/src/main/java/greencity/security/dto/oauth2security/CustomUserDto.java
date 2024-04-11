package greencity.security.dto.oauth2security;

import static greencity.constant.GoogleConstants.*;

import org.springframework.security.oauth2.core.user.OAuth2User;

public record CustomUserDto(String name, String firstName, String email, String picture, String locale) {

    public static CustomUserDto buildFromOAuth2User(OAuth2User oAuth2User) {
        return new CustomUserDto(oAuth2User.getAttribute(NAME),
                oAuth2User.getAttribute(FIRST_NAME),
                oAuth2User.getAttribute(EMAIL),
                oAuth2User.getAttribute(PICTURE),
                oAuth2User.getAttribute(LOCALE)
        );
    }
}
