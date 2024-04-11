package greencity.mapping;

import static java.util.Objects.*;

import greencity.dto.language.LanguageVO;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.user.UserVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.entity.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserVOMapper extends AbstractConverter<User, UserVO> {
    @Override
    protected UserVO convert(User user) {
        return UserVO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .userCredo(user.getUserCredo())
            .emailNotification(user.getEmailNotification())
            .userStatus(user.getUserStatus())
            .rating(user.getRating())
            .verifyEmail(user.getVerifyEmail() != null ? VerifyEmailVO.builder()
                .id(user.getVerifyEmail().getId())
                .user(UserVO.builder()
                    .id(user.getVerifyEmail().getUser().getId())
                    .name(user.getVerifyEmail().getUser().getName())
                    .build())
                .expiryDate(user.getVerifyEmail().getExpiryDate())
                .token(user.getVerifyEmail().getToken())
                .build() : null)
            .refreshTokenKey(user.getRefreshTokenKey())
            .ownSecurity(user.getOwnSecurity() != null ? OwnSecurityVO.builder()
                .id(user.getOwnSecurity().getId())
                .password(user.getOwnSecurity().getPassword())
                .user(UserVO.builder()
                    .id(user.getOwnSecurity().getUser().getId())
                    .email(user.getOwnSecurity().getUser().getEmail())
                    .role(user.getOwnSecurity().getUser().getRole())
                    .build())
                .build() : null)
            .dateOfRegistration(user.getDateOfRegistration())
            .profilePicturePath(user.getProfilePicturePath())
            .city(user.getCity())
            .showShoppingList(user.getShowShoppingList())
            .showEcoPlace(user.getShowEcoPlace())
            .showLocation(user.getShowLocation())
            .lastActivityTime(user.getLastActivityTime())
            .languageVO(nonNull(user.getLanguage()) ? getLanguage(user) : null)
            .build();
    }

    private LanguageVO getLanguage(User user) {
            .languageVO(nonNull(user.getLanguage()) ? getUserLanguage(user): null)
            .build();
    }

    private LanguageVO getUserLanguage(User user) {
        return LanguageVO.builder()
                .id(user.getLanguage().getId())
                .code(user.getLanguage().getCode())
                .build();
    }
}
