package greencity.security.dto.googlesecurity;

import static greencity.constant.GoogleConstants.*;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public record GoogleUserDto(String name, String firstName, String email, String picture, String locale) {

    public static GoogleUserDto  buildFromOAuth2User(DefaultOAuth2User defaultOAuth2User) {
        return new GoogleUserDto(defaultOAuth2User.getAttribute(NAME),
                defaultOAuth2User.getAttribute(FIRST_NAME),
                defaultOAuth2User.getAttribute(EMAIL),
                defaultOAuth2User.getAttribute(PICTURE),
                defaultOAuth2User.getAttribute(LOCALE)
        );
    }
}
