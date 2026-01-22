package com.amp.domain.festival.service.organizer;

import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.category.service.FestivalCategoryService;
import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.request.ScheduleRequest;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.festival.scheduler.FestivalScheduleService;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.service.StageService;
import com.amp.domain.user.entity.User;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import com.amp.global.security.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @InjectMocks
    private FestivalService festivalService;

    @Mock
    private FestivalRepository festivalRepository;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private StageRepository stageRepository;
    @Mock
    private FestivalScheduleRepository festivalScheduleRepository;
    @Mock
    private FestivalCategoryRepository festivalCategoryRepository;

    @Mock
    private FestivalScheduleService scheduleService;
    @Mock
    private StageService stageService;
    @Mock
    private FestivalCategoryService categoryService;
    @Mock
    private AuthService authService;
    @Mock
    private S3Service s3Service;
    @Mock
    private ObjectMapper objectMapper;

    private User mockUser;
    private Festival mockFestival;
    private ScheduleRequest mockScheduleRequest;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockFestival = mock(Festival.class);
        mockScheduleRequest = mock(ScheduleRequest.class);

        lenient().when(mockScheduleRequest.getFestivalDate()).thenReturn(LocalDate.now());
        lenient().when(mockScheduleRequest.getFestivalTime()).thenReturn(LocalTime.now());
    }

    @Nested
    @DisplayName("축제 생성 테스트")
    class CreateFestival {

        @Test
        @DisplayName("성공: 축제를 생성하고 관련 정보를 동기화한다")
        void createFestivalSuccess() throws Exception {
            // given
            MultipartFile mockImage = mock(MultipartFile.class);
            FestivalCreateRequest request = new FestivalCreateRequest(
                    "제목", "장소", mockImage, "[]", "[]", "[]");

            given(authService.getCurrentUser()).willReturn(mockUser);
            given(objectMapper.readValue(anyString(), any(TypeReference.class)))
                    .willReturn(List.of(mockScheduleRequest))
                    .willReturn(List.of())
                    .willReturn(List.of(1L));

            given(s3Service.upload(any(), anyString())).willReturn("image-key");
            given(s3Service.getPublicUrl(anyString())).willReturn("url");
            given(festivalRepository.save(any(Festival.class))).willReturn(mockFestival);

            // when
            festivalService.createFestival(request);

            // then
            verify(festivalRepository).save(any(Festival.class));
            verify(festivalCategoryRepository, never()).softDeleteByFestivalId(anyLong());
        }

        @Test
        @DisplayName("이미지가 없으면 예외가 발생한다")
        void createFestivalNoImage() throws Exception {
            // given
            FestivalCreateRequest request = new FestivalCreateRequest(
                    "제목", "장소", null, "[]", "[]", "[]"
            );

            given(authService.getCurrentUser()).willReturn(mockUser);
            given(objectMapper.readValue(anyString(), any(TypeReference.class)))
                    .willReturn(List.of(mockScheduleRequest));

            // when & then
            assertThatThrownBy(() -> festivalService.createFestival(request))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("공연 삭제 테스트")
    class DeleteFestival {
        @Test
        @DisplayName("모든 관련 정보를 소프트 딜리트한다")
        void deleteFestivalSuccess() {
            // given
            Long festivalId = 1L;
            given(authService.getCurrentUser()).willReturn(mockUser);
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(mockFestival));
            given(organizerRepository.existsByFestivalAndUser(mockFestival, mockUser)).willReturn(true);

            // when
            festivalService.deleteFestival(festivalId);

            // then
            verify(festivalCategoryRepository).softDeleteByFestivalId(festivalId);
            verify(festivalRepository).softDeleteById(festivalId);
        }
    }
}
