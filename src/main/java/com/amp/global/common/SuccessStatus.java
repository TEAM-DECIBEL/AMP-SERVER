package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements SuccessCode {
    // COMMON
    OK(HttpStatus.OK, "COM", "001", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COM", "002", "리소스가 성공적으로 생성되었습니다."),

    // FESTIVAL
    FESTIVAL_CREATE_SUCCESS(HttpStatus.CREATED, "FES","001", "공연 등록이 완료되었습니다."),

    // UserFestival
    USER_FESTIVAL_RECENT_FOUND(HttpStatus.OK, "UFE", "001", "관람 예정 최근에 보는 공연 정보가 조회되었습니다."),
    USER_FESTIVAL_RECENT_NOT_FOUND(HttpStatus.OK,"UFE","002","관람 예정 정보가 없습니다."),
    GET_FESTIVAL_DETAIL_INFO(HttpStatus.OK, "FES", "002", "공연 상세 정보가 조회되었습니다."),
    FESTIVAL_UPDATE_SUCCESS(HttpStatus.OK, "FES", "003", "공연 정보 수정이 완료되었습니다."),
    FESTIVAL_DELETE_SUCCESS(HttpStatus.OK, "FES", "004", "공연 삭제가 완료되었습니다."),
    FESTIVAL_LIST_EMPTY(HttpStatus.OK, "FES", "005", "등록된 공연이 없습니다."),
    FESTIVAL_LIST_FOUND(HttpStatus.OK, "FES", "006", "전체 공연 정보가 조회되었습니다."),

    // ORGANIZER
    GET_MY_ALL_FESTIVALS(HttpStatus.OK, "ORG", "001", "나의 진행한 모든 공연 조회가 완료되었습니다."),
    GET_MY_ALL_ACTIVE_FESTIVALS(HttpStatus.OK, "ORG", "002", "진행 중 및 진행 예정 공연 조회가 완료되었습니다."),

    //UserBookMark
    SAVED_ANNOUNCEMENTS_RETRIEVED(HttpStatus.OK,"USE","001", "저장한 공지 조회가 완료되었습니다."),

    // NOTICE
    NOTICE_DETAIL_GET_SUCCESS(HttpStatus.OK, "NOT", "001", "공지 상세 조회되었습니다."),
    NOTICE_LIST_GET_SUCCESS(HttpStatus.OK,  "NOT", "002", "공지 목록 조회되었습니다."),
    NOTICE_CREATE_SUCCESS(HttpStatus.CREATED, "NTC", "001", "공지가 등록되었습니다."),
    NOTICE_DETAIL_GET_SUCCESS(HttpStatus.OK, "NTC", "002", "공지 상세 조회되었습니다."),

    // NOTICE SAVE
    BOOKMARK_UPDATE_SUCCESS(HttpStatus.CREATED, "BOK", "001", "북마크 요청이 정상적으로 처리되었습니다."),

    // NOTICE DELETE
    NOTICE_DELETE_SUCCESS(HttpStatus.OK, "NOT", "003", "공지 삭제가 완료되었습니다."),;

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }

}
