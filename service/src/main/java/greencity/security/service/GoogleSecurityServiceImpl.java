package greencity.security.service;

import static greencity.constant.ErrorMessage.*;
import static greencity.enums.UserStatus.*;
import static java.util.Objects.*;

import greencity.constant.AppConstant;
import greencity.constant.GoogleConstants;
import greencity.dto.user.UserVO;
import greencity.entity.Language;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadUserStatusException;
import greencity.repository.UserRepo;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.googlesecurity.GoogleUserDto;
import greencity.security.jwt.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleSecurityServiceImpl implements GoogleSecurityService {
    private final UserRepo userRepo;
    private final JwtTool jwtTool;
    private final ModelMapper modelMapper;

    @SneakyThrows
    @Transactional
    @Override
    public SuccessSignInDto authenticateWithGoogle(GoogleUserDto googleUser) {
        var optionalUser = getOptionalUserByEmail(googleUser.email());

        return optionalUser.map(this::authenticateUser)
                .orElseGet(() -> registerUser(googleUser));
    }

    private Optional<User> getOptionalUserByEmail(String email){
        return userRepo.findByEmail(email);
    }

    private SuccessSignInDto authenticateUser(User user) {
        checkUserStatusIsNot(user, DEACTIVATED, USER_DEACTIVATED);
        checkUserStatusIsNot(user, BLOCKED, USER_BLOCKED);
        checkUserStatusIsNot(user, CREATED, USER_CREATED);

        UserVO userVO = mapToUserVO(user);


        return signIn(userVO);
    }

    private void checkUserStatusIsNot(User user, UserStatus deactivated, String userDeactivated) {
        if (user.getUserStatus().equals(deactivated)) {
            throw new BadUserStatusException(userDeactivated);
        }
    }

    private SuccessSignInDto registerUser(GoogleUserDto googleUser) {
        var registeredUser = registerNewUser(googleUser);
        return signIn(mapToUserVO(registeredUser));
    }

    private User registerNewUser(GoogleUserDto googleUser) {
        var newUser = createUser(googleUser);
        return userRepo.save(newUser);
    }

    private User createUser(GoogleUserDto googleUser) {
        return User.builder()
                .name(googleUser.name())
                .firstName(googleUser.firstName())
                .email(googleUser.email())
                .profilePicturePath(googleUser.picture())
                .uuid(UUID.randomUUID().toString())
                .dateOfRegistration(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .refreshTokenKey(jwtTool.generateTokenKey())
                .lastActivityTime(LocalDateTime.now())
                .userStatus(ACTIVATED)
                .emailNotification(EmailNotification.DISABLED)
                .rating(AppConstant.DEFAULT_RATING)
                .language(Language.builder()
                        .id(modelMapper.map(getLanguageCode(googleUser), Long.class))
                        .build())
                .showEcoPlace(true)
                .showShoppingList(true)
                .showLocation(true)
                .build();
    }

    private String getLanguageCode(GoogleUserDto googleUser){
        return (nonNull(googleUser.locale()) && googleUser.locale().equals(GoogleConstants.GOOGLE_UKRAINIAN_LANGUAGE_CODE))?
                AppConstant.UKRAINIAN_LANGUAGE_CODE : AppConstant.DEFAULT_LANGUAGE_CODE;
    }

    private UserVO mapToUserVO(User registeredUser) {
        return modelMapper.map(registeredUser, UserVO.class);
    }

    private SuccessSignInDto signIn(UserVO userVO) {
        String accessToken = jwtTool.createAccessToken(userVO.getEmail(), userVO.getRole());
        String refreshToken = jwtTool.createRefreshToken(userVO);
        return new SuccessSignInDto(userVO.getId(), accessToken, refreshToken, userVO.getName(), false);
    }
}
