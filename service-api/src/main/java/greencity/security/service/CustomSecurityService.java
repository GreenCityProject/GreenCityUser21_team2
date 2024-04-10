package greencity.security.service;

import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.oauth2security.CustomUserDto;

public interface CustomSecurityService {
    SuccessSignInDto authenticate(CustomUserDto googleUser);
}
