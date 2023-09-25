package com.tickerBell.domain.event.entity;

import com.tickerBell.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "event_id")
    private Long id;

    private String name; // 이벤트 이름
    private LocalDateTime startEvent; // 이벤트 시작 시간
    private LocalDateTime endEvent; // 이벤트 시작 시간
    private Integer normalPrice; // 일반 좌석 가격
    private Integer premiumPrice; // 앞열 좌석 가격
    private Boolean isSale; // 세일 여부
    private Float saleDegree; // 1.0 이상: n 원 할인  |  1.0 미만: n 퍼센트 할인
    @Column(columnDefinition = "TEXT")
    private String casting; // 출연진 정보 (, 로 구분해서 저장)
    private Integer totalSeat; // 전체 좌석 수
    private Integer remainSeat; // 남은 좌석 수
    private String host; // 주최자, 스폰서 (, 로 구분해서 저장)
    private String place; // 주소
    private Integer seatXBound; // x 좌표 최대 값
    private Integer seatYBound; // y 좌표 최대 값

}