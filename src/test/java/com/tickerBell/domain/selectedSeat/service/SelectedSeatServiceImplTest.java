package com.tickerBell.domain.selectedSeat.service;

import com.tickerBell.domain.selectedSeat.entity.SelectedSeat;
import com.tickerBell.domain.selectedSeat.repository.SelectedSeatRepository;
import com.tickerBell.domain.ticketing.entity.Ticketing;
import com.tickerBell.global.exception.CustomException;
import com.tickerBell.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectedSeatServiceImplTest {
    @InjectMocks
    private SelectedSeatServiceImpl selectedSeatService;
    @Mock
    private SelectedSeatRepository selectedSeatRepository;

    @Test
    @DisplayName("선택 좌석 정보 저장")
    public void saveSelectedSeatTest() {
        // given
        List<SelectedSeat> selectedSeatList = createSelectedSeatList();

        // stub
        when(selectedSeatRepository.saveAll(any(List.class))).thenReturn(selectedSeatList);

        // when
        Integer savedCount = selectedSeatService.saveSelectedSeat(selectedSeatList);

        // then
        verify(selectedSeatRepository, times(1)).saveAll(selectedSeatList);
        assertThat(selectedSeatList.size()).isEqualTo(savedCount);
    }

    @Test
    @DisplayName("좌석 선택 시 validation 체크")
    public void validCheckTest() {
        // given
        Long eventId = 1L;
        String seatInfo = "A-1";
        Ticketing ticketing = Ticketing.builder().build();
        SelectedSeat selectedSeat = SelectedSeat.builder().seatInfo("A-1").ticketing(ticketing).build();

        // stub
        when(selectedSeatRepository.findByEventIdAndSeatInfo(any(Long.class), any(String.class)))
                .thenReturn(Optional.empty());

        // when
        selectedSeatService.validCheckSeatInfo(eventId, seatInfo);

        // then
        verify(selectedSeatRepository, times(1)).findByEventIdAndSeatInfo(any(Long.class), any(String.class));

    }

    @Test
    @DisplayName("좌석 선택 시 validation 체크 예외")
    public void validCheckExceptionTest() {
        // given
        Long eventId = 1L;
        String seatInfo = "A-1";
        Ticketing ticketing = Ticketing.builder().build();
        SelectedSeat selectedSeat = SelectedSeat.builder().seatInfo("A-1").ticketing(ticketing).build();

        // stub
        when(selectedSeatRepository.findByEventIdAndSeatInfo(any(Long.class), any(String.class)))
                .thenReturn(Optional.of(selectedSeat));

        // then
        assertThatThrownBy(() -> selectedSeatService.validCheckSeatInfo(eventId, seatInfo))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(selectedSeat.getSeatInfo() + "은 이미 선택된 좌석 입니다.");
    }

    private List<SelectedSeat> createSelectedSeatList() {
        List<SelectedSeat> selectedSeatList = new ArrayList<>();
        Ticketing ticketing = Ticketing.builder().build();

        SelectedSeat selectedSeat1 = SelectedSeat.builder()
                .seatInfo("A1")
                .seatPrice(10.0f)
                .ticketing(ticketing)
                .build();

        SelectedSeat selectedSeat2 = SelectedSeat.builder()
                .seatInfo("B2")
                .seatPrice(15.0f)
                .ticketing(ticketing)
                .build();

        selectedSeatList.add(selectedSeat1);
        selectedSeatList.add(selectedSeat2);

        return selectedSeatList;
    }


}