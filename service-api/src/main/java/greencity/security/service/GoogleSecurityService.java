package greencity.security.service;

import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.googlesecurity.GoogleUserDto;

public interface GoogleSecurityService {
    SuccessSignInDto authenticateWithGoogle(GoogleUserDto googleUser);
}
