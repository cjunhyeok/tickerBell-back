package com.tickerBell.domain.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickerBell.domain.event.dtos.SaveEventRequest;
import com.tickerBell.domain.event.entity.Category;
import com.tickerBell.domain.event.service.EventService;
import com.tickerBell.domain.member.entity.Role;
import com.tickerBell.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private EventService eventService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        memberService.join("username", "testPass1!", "010-1234-5679", true, Role.ROLE_REGISTRANT, null);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void afterTest() {
        transactionManager.rollback(transactionStatus);
    }

    @Test
    @DisplayName("이벤트 저장 api 테스트")
    @WithUserDetails(value = "username", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void saveEventTest() throws Exception {
        // given
        SaveEventRequest mockRequest = createMockSaveEventRequest();

        MockMultipartFile thumbNailImage = new MockMultipartFile("thumbNailImage", "image1.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile eventImage1 = new MockMultipartFile("eventImages", "image1.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile eventImage2 = new MockMultipartFile("eventImages", "image2.png", "image/png", new byte[0]);
        String requestJson = objectMapper.writeValueAsString(mockRequest);
        MockMultipartFile request = new MockMultipartFile("request", "request", "application/json", requestJson.getBytes(StandardCharsets.UTF_8));

        // when
        ResultActions perform = mockMvc.perform(multipart("/api/event")
                        .file(thumbNailImage)
                        .file(eventImage1)
                        .file(eventImage2)
                        .file(request)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .accept(MediaType.APPLICATION_JSON));
        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이벤트 등록에 성공하였습니다."));
    }

    private SaveEventRequest createMockSaveEventRequest() {
        SaveEventRequest request = new SaveEventRequest();
        request.setStartEvent(LocalDateTime.now());
        request.setEndEvent(LocalDateTime.now());
        request.setName("mockName");
        request.setNormalPrice(10000);
        request.setPremiumPrice(15000);
        request.setSaleDegree(1000F);
        List<String> castings = new ArrayList<>();
        castings.add("casting1");
        request.setCastings(castings);
        List<String> hosts = new ArrayList<>();
        hosts.add("host1");
        request.setHosts(hosts);
        request.setPlace("mockPlace");
        request.setIsAdult(false);
        request.setIsSpecialA(true);
        request.setIsSpecialB(true);
        request.setIsSpecialC(true);
        request.setCategory(Category.CONCERT);
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        request.setTags(tags);
        return request;
    }

    @Test
    @DisplayName("카테고리를 통해 이벤트 조회 테스트")
    @WithUserDetails(value = "username", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getEventByCategoryTest() throws Exception {
        // todo
    }

    @Test
    @DisplayName("이벤트 PK를 통한 이벤트 조회 테스트")
    void getEventById() throws Exception {
        // given
        Long testUserId = memberService.join("testUsername", "testPass1!", "010-1234-5679", true, Role.ROLE_REGISTRANT, null);
        MockMultipartFile thumbNailImage = new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[0]);
        List<MultipartFile> eventImages = new ArrayList<>();
        eventImages.add(new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[0]));
        eventImages.add(new MockMultipartFile("image2.png", "image2.png", "image/png", new byte[0]));
        Long testEventId = eventService.saveEvent(testUserId, createMockSaveEventRequest(), thumbNailImage, eventImages);

        // when
        ResultActions perform = mockMvc.perform(get("/api/event/{eventId}", testEventId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("mockName"))
                .andExpect(jsonPath("$.data.startEvent").isNotEmpty())
                .andExpect(jsonPath("$.data.endEvent").isNotEmpty())
                .andExpect(jsonPath("$.data.normalPrice").value(10000))
                .andExpect(jsonPath("$.data.premiumPrice").value(15000))
                .andExpect(jsonPath("$.data.discountNormalPrice").value(9000))
                .andExpect(jsonPath("$.data.discountPremiumPrice").value(14000))
                .andExpect(jsonPath("$.data.place").value("mockPlace"))
                .andExpect(jsonPath("$.data.isAdult").value(false))
                .andExpect(jsonPath("$.data.category").value(Category.CONCERT.name()))
                .andExpect(jsonPath("$.data.isSpecialSeatA").value(true))
                .andExpect(jsonPath("$.data.isSpecialSeatB").value(true))
                .andExpect(jsonPath("$.data.isSpecialSeatC").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 상세 데이터 반환 완료"));
        // todo 다대일 관계 andExpect 추가
    }
}
