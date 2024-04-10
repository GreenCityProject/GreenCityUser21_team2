package greencity.controller;

import greencity.config.SecurityConfig;
import greencity.config.WebMvcConfig;
import greencity.security.jwt.JwtTool;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = { WebMvcConfig.class, SecurityConfig.class })
class UserControllerIntegrationTest {
    private static final String userProfilePictureLink = "/user/profilePicture";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private JwtTool jwtTool;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void updateUserProfilePicture_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(patch(userProfilePictureLink)
                .with(anonymous()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUserProfilePicture(any(), anyString(), anyString());
    }
}