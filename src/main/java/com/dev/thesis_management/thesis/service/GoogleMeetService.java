package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.thesis.dto.group.MeetingRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleMeetService {

    private final String CLIENT_ID = "YOUR_CLIENT_ID";
    private final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    private final String REFRESH_TOKEN = "YOUR_REFRESH_TOKEN";

    private final RestTemplate restTemplate = new RestTemplate();

    public String createMeeting(MeetingRequest request) throws Exception {

        String accessToken = getAccessToken();

        String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1";

        Map<String, Object> body = new HashMap<>();

        body.put("summary", request.title());
        body.put("description", request.description());

        body.put("start", Map.of(
                "dateTime", request.start().toString(),
                "timeZone", "Asia/Ho_Chi_Minh"
        ));

        body.put("end", Map.of(
                "dateTime", request.end().toString(),
                "timeZone", "Asia/Ho_Chi_Minh"
        ));

        body.put("conferenceData", Map.of(
                "createRequest", Map.of(
                        "requestId", UUID.randomUUID().toString(),
                        "conferenceSolutionKey", Map.of(
                                "type", "hangoutsMeet"
                        )
                )
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> data = response.getBody();

        return (String) data.get("hangoutLink");
    }

    private String getAccessToken() {

        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("refresh_token", REFRESH_TOKEN);
        form.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        return (String) response.getBody().get("access_token");
    }
}
