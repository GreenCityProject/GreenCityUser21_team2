package greencity.client;

import greencity.constant.RestTemplateLinks;
import greencity.dto.shoppinglist.CustomShoppingListItemResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

import static greencity.constant.AppConstant.AUTHORIZATION;
import static greencity.constant.AppConstant.IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestClientTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Value("${greencity.server.address}")
    private String greenCityServerAddress;
    @InjectMocks
    private RestClient restClient;

    @Test
    void getAllAvailableCustomShoppingListItems() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Long userId = 1L;
        Long habitId = 1L;
        CustomShoppingListItemResponseDto customShoppingListItemResponseDto =
            new CustomShoppingListItemResponseDto(1L, "test");
        CustomShoppingListItemResponseDto[] customShoppingListItemResponseDtos =
            new CustomShoppingListItemResponseDto[1];
        customShoppingListItemResponseDtos[0] = customShoppingListItemResponseDto;
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityServerAddress
            + RestTemplateLinks.CUSTOM_SHOPPING_LIST_ITEMS + userId + "/" + habitId, HttpMethod.GET, entity,
            CustomShoppingListItemResponseDto[].class))
                .thenReturn(ResponseEntity.ok(customShoppingListItemResponseDtos));

        assertEquals(Arrays.asList(customShoppingListItemResponseDtos),
            restClient.getAllAvailableCustomShoppingListItems(userId, habitId));
    }

    @Test
    void uploadImage() throws IOException {
        String imagePath = "image";
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultipartFile image =
            new MockMultipartFile("data", "filename.png", "image/png",
                "some xml".getBytes());
        ByteArrayResource fileAsResource = new ByteArrayResource(image.getBytes()) {

            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(IMAGE, fileAsResource);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.postForObject(greenCityServerAddress +
            RestTemplateLinks.FILES_IMAGE, requestEntity,
            String.class)).thenReturn(imagePath);
        assertEquals(imagePath,
            restClient.uploadImage(image));
    }

    @Test
    void findAmountOfPublishedNews() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Long publishedNews = 5L;
        Long userId = 1L;
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityServerAddress
            + RestTemplateLinks.ECONEWS_COUNT + RestTemplateLinks.USER_ID + userId, HttpMethod.GET, entity, Long.class))
                .thenReturn(ResponseEntity.ok(publishedNews));
        assertEquals(publishedNews, restClient.findAmountOfPublishedNews(userId));
    }

    @Test
    void findAmountOfAcquiredHabits() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Long acquiredHabits = 5L;
        Long userId = 1L;
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityServerAddress
            + RestTemplateLinks.HABIT_STATISTIC_ACQUIRED_COUNT + RestTemplateLinks.USER_ID + userId, HttpMethod.GET,
            entity, Long.class)).thenReturn(ResponseEntity.ok(acquiredHabits));
        assertEquals(acquiredHabits, restClient.findAmountOfAcquiredHabits(userId));
    }

    @Test
    void findAmountOfHabitsInProgress() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        Long habitsInProgress = 5L;
        Long userId = 1L;
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityServerAddress
            + RestTemplateLinks.HABIT_STATISTIC_IN_PROGRESS_COUNT + RestTemplateLinks.USER_ID + userId, HttpMethod.GET,
            entity, Long.class)).thenReturn(ResponseEntity.ok(habitsInProgress));
        assertEquals(habitsInProgress, restClient.findAmountOfHabitsInProgress(userId));
    }

    @Test
    void getAllLanguageCodes() {
        String[] allLanguageCodes = new String[3];
        allLanguageCodes[0] = "en";
        allLanguageCodes[1] = "uk";
        allLanguageCodes[2] = "fr";
        when(restTemplate.getForObject(greenCityServerAddress
            + RestTemplateLinks.LANGUAGE, String[].class)).thenReturn(allLanguageCodes);

        assertEquals(Arrays.asList(allLanguageCodes), restClient.getAllLanguageCodes());
    }

}
