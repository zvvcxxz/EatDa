package com.sp.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;
import com.sp.app.mapper.TogetherShareMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TogetherShareServiceImpl implements TogetherShareService {

    private final TogetherShareMapper mapper;

    // 재료나눔 글 등록
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertShare(TogetherShareDto dto) throws Exception {
        try {
            // 작성자 확인
            if (dto == null || dto.getAuthor_id() == null) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }

            // 제목 / 내용 확인
            if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getContent() == null
                    || dto.getContent().isBlank()) {
                throw new IllegalArgumentException("제목과 내용을 입력해주세요.");
            }

            // 나눔 장소 확인
            if (dto.getLocation_text() == null || dto.getLocation_text().isBlank()) {
                throw new IllegalArgumentException("나눔 장소를 입력해주세요.");
            }

            // 수령 인원 확인
            if (dto.getMax_recipients() == null || dto.getMax_recipients() < 1) {
                throw new IllegalArgumentException("최대 수령 인원은 1명 이상이어야 합니다.");
            }

            // 시작일 / 종료일 입력 확인
            if (dto.getAvailable_from() == null || dto.getAvailable_from().isBlank()
                    || dto.getAvailable_until() == null || dto.getAvailable_until().isBlank()) {
                throw new IllegalArgumentException("나눔 시작일과 종료일을 입력해주세요.");
            }

            LocalDateTime start;
            LocalDateTime end;

            // 날짜 형식 확인
            try {
                start = LocalDateTime.parse(dto.getAvailable_from());
                end = LocalDateTime.parse(dto.getAvailable_until());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("나눔 날짜와 시간을 올바르게 입력해주세요.");
            }

            // 종료일이 시작일보다 늦은지 확인
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("나눔 종료일은 시작일보다 이후여야 합니다.");
            }

            // 재료가 추가됐는지 확인
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new IllegalArgumentException("나눔할 재료를 추가해주세요.");
            }

            for (TogetherShareItemDto item : dto.getItems()) {
                // 재료 선택 확인
                if (item == null || item.getIngredient_id() == null) {
                    throw new IllegalArgumentException("재료를 선택해주세요.");
                }

                // 재료 수량 확인
                if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                    throw new IllegalArgumentException("재료 수량은 0보다 커야 합니다.");
                }

                // 재료 단위 확인
                if (item.getUnit() == null || item.getUnit().isBlank()) {
                    throw new IllegalArgumentException("재료 단위를 입력해주세요.");
                }
            }

            // 게시글 저장
            int count = mapper.insertShare(dto);

            if (count != 1) {
                throw new IllegalStateException("게시글을 등록하지 못했습니다.");
            }

            // 등록된 글 번호로 나눔 재료 저장
            for (TogetherShareItemDto item : dto.getItems()) {
                item.setShare_id(dto.getShare_id());

                int itemCount = mapper.insertShareItem(item);

                if (itemCount != 1) {
                    throw new IllegalStateException("나눔 재료를 등록하지 못했습니다.");
                }
            }

            return count;

        } catch (Exception e) {
            log.error("재료나눔 글 등록 실패", e);
            throw e;
        }
    }

    // 재료명 / 별칭 검색
    @Override
    @Transactional(readOnly = true)
    public List<TogetherShareItemDto> searchIngredients(String keyword) throws Exception {

        try {
            if (keyword == null || keyword.isBlank()) {
                return List.of();
            }

            return mapper.searchIngredients(keyword.trim());

        } catch (Exception e) {
            log.error("재료나눔 재료 검색 실패", e);
            throw e;
        }
    }

    // 분류별 재료 목록
    @Override
    @Transactional(readOnly = true)
    public List<TogetherShareItemDto> listIngredientsByType(String type) throws Exception {

        try {
            if (type == null || type.isBlank()) {
                return List.of();
            }

            return mapper.listIngredientsByType(type.trim());

        } catch (Exception e) {
            log.error("재료나눔 분류별 조회 실패", e);
            throw e;
        }
    }

    // 재료나눔 글 목록
    @Override
    @Transactional(readOnly = true)
    public List<TogetherShareDto> listShare(int offset, int size) throws Exception {

        try {
            if (offset < 0 || size < 1 || size > 100) {
                throw new IllegalArgumentException("목록 조회 범위가 올바르지 않습니다.");
            }

            return mapper.listShare(offset, size);

        } catch (Exception e) {
            log.error("재료나눔 글 목록 조회 실패", e);
            throw e;
        }
    }

    // 재료나눔 글 수정
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShare(TogetherShareDto dto) throws Exception {

        try {
            // 글 번호와 로그인 정보 확인
            if (dto == null || dto.getShare_id() == null || dto.getShare_id() < 1) {
                throw new IllegalArgumentException("게시글 번호가 올바르지 않습니다.");
            }

            if (dto.getAuthor_id() == null) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }

            // 수정할 글이 있는지 확인
            TogetherShareDto share = mapper.findShareById(dto.getShare_id());

            if (share == null) {
                throw new IllegalArgumentException("게시글이 없거나 삭제되었습니다.");
            }

            // 작성자 확인
            if (!Objects.equals(share.getAuthor_id(), dto.getAuthor_id())) {
                throw new IllegalArgumentException("글 작성자만 수정할 수 있습니다.");
            }

            // 제목과 내용 확인
            if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getContent() == null
                    || dto.getContent().isBlank()) {
                throw new IllegalArgumentException("제목과 내용을 입력해주세요.");
            }

            // 나눔 장소 확인
            if (dto.getLocation_text() == null || dto.getLocation_text().isBlank()) {
                throw new IllegalArgumentException("나눔 장소를 입력해주세요.");
            }

            // 나눔 유형 확인
            if (dto.getShare_type() == null || (dto.getShare_type() != 0 && dto.getShare_type() != 1)) {
                throw new IllegalArgumentException("나눔 유형을 선택해주세요.");
            }

            // 교환이면 조건 입력 확인
            if (dto.getShare_type() == 1) {
                if (dto.getExchange_note() == null || dto.getExchange_note().isBlank()) {
                    throw new IllegalArgumentException("교환 조건을 입력해주세요.");
                }
            } else {
                dto.setExchange_note(null);
            }

            // 최대 수령인원 확인
            if (dto.getMax_recipients() == null || dto.getMax_recipients() < 1) {
                throw new IllegalArgumentException("최대 수령인원은 1명 이상이어야 합니다.");
            }

            // 시작일과 종료일 입력 확인
            if (dto.getAvailable_from() == null || dto.getAvailable_from().isBlank()
                    || dto.getAvailable_until() == null || dto.getAvailable_until().isBlank()) {
                throw new IllegalArgumentException("나눔 시작일과 종료일을 입력해주세요.");
            }

            LocalDateTime start;
            LocalDateTime end;

            // 날짜 형식 확인
            try {
                start = LocalDateTime.parse(dto.getAvailable_from());
                end = LocalDateTime.parse(dto.getAvailable_until());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("나눔 날짜와 시간을 올바르게 입력해주세요.");
            }

            // 종료일이 시작일 이후인지 확인
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("나눔 종료일은 시작일보다 이후여야 합니다.");
            }

            // 게시글 수정
            int count = mapper.updateShare(dto);

            if (count != 1) {
                throw new IllegalStateException("게시글을 수정하지 못했습니다.");
            }

            // 재료 목록도 함께 수정
            updateShareItem(dto);

            return count;
        } catch (Exception e) {
            log.error("재료나눔 글 수정 실패", e);
            throw e;
        }
    }

    // 재료나눔 글 삭제
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShare(Long share_id, Long author_id) throws Exception {
        try {
            // 글 번호 확인
            if (share_id == null || share_id < 1) {
                throw new IllegalArgumentException("게시글 번호가 올바르지 않습니다.");
            }

            // 로그인 정보 확인
            if (author_id == null) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }

            // 삭제할 글 확인
            TogetherShareDto share = mapper.findShareById(share_id);

            if (share == null) {
                throw new IllegalArgumentException("게시글이 없거나 삭제되었습니다.");
            }

            // 작성자 확인
            if (!Objects.equals(share.getAuthor_id(), author_id)) {
                throw new IllegalStateException("글 작성자만 삭제할 수 있습니다.");
            }

            // 삭제일 기록
            int count = mapper.deleteShare(share_id, author_id);

            if (count != 1) {
                throw new IllegalStateException("게시글을 삭제하지 못했습니다.");
            }

            return count;

        } catch (Exception e) {
            log.error("재료나눔 글 삭제 실패", e);
            throw e;
        }
    }

    // 재료 목록 비교해서 수정 / 추가 / 삭제
    private void updateShareItem(TogetherShareDto dto) throws Exception {

        List<TogetherShareItemDto> items = dto.getItems();

        // 재료를 전부 지우고 저장하지 못하게 확인
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("나눔할 재료를 하나 이상 추가해주세요.");
        }

        // 현재 글에 등록된 재료 번호
        List<TogetherShareItemDto> originalItems = mapper.listShareItems(dto.getShare_id());

        Set<Long> originalIds = new HashSet<>();

        for (TogetherShareItemDto item : originalItems) {
            originalIds.add(item.getShare_item_id());
        }

        Set<Long> requestIds = new HashSet<>();

        // 저장하기 전에 입력값 확인
        for (TogetherShareItemDto item : items) {

            if (item == null || item.getIngredient_id() == null || item.getIngredient_id() < 1) {
                throw new IllegalArgumentException("재료를 검색해서 선택해주세요.");
            }

            // 기존 재료 번호가 현재 글에 속하는지 확인
            Long itemId = item.getShare_item_id();

            if (itemId != null) {
                if (!originalIds.contains(itemId)) {
                    throw new IllegalArgumentException("이 게시글에 등록된 재료가 아닙니다.");
                }

                if (!requestIds.add(itemId)) {
                    throw new IllegalArgumentException("같은 나눔 재료가 중복 전달되었습니다.");
                }
            }

            // 수량 확인
            BigDecimal quantity = item.getQuantity();

            if (quantity == null || quantity.signum() <= 0) {
                throw new IllegalArgumentException("재료 수량은 0보다 커야 합니다.");
            }

            // 단위 확인
            if (item.getUnit() == null || item.getUnit().isBlank()) {
                throw new IllegalArgumentException("재료 단위를 입력해주세요.");
            }

            // 소비기한은 입력한 경우에만 확인
            if (item.getExpires_on() == null || item.getExpires_on().isBlank()) {
                item.setExpires_on(null);
            } else {
                try {
                    LocalDate.parse(item.getExpires_on());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("소비기한을 올바른 날짜로 입력해주세요.");
                }
            }
        }

        // 기존 목록에는 있지만 수정폼에서 빠진 재료 삭제
        for (TogetherShareItemDto original : originalItems) {
            Long itemId = original.getShare_item_id();

            if (!requestIds.contains(itemId)) {
                int count = mapper.deleteShareItem(dto.getShare_id(), itemId);

                if (count != 1) {
                    throw new IllegalStateException("나눔 재료를 삭제하지 못했습니다.");
                }
            }
        }

        // 기존 재료는 수정하고 새 재료는 추가
        for (TogetherShareItemDto item : items) {

            // 폼에서 넘어온 게시글 번호 대신에 서버 값 사용
            item.setShare_id(dto.getShare_id());

            int count;

            if (item.getShare_item_id() == null) {
                count = mapper.insertShareItem(item);
            } else {
                count = mapper.updateShareItem(item);
            }

            if (count != 1) {
                throw new IllegalStateException("나눔 재료를 저장하지 못했습니다.");
            }
        }
    }

    // 재료 나눔 : 나눔 글 상세 조회
    @Override
    @Transactional(readOnly = true)
    public TogetherShareDto findShareById(Long share_id) throws Exception {

        try {
            if (share_id == null || share_id < 1) {
                throw new IllegalArgumentException("게시글 번호가 올바르지 않습니다.");
            }

            return mapper.findShareById(share_id);

        } catch (Exception e) {
            log.error("재료나눔 글 상세조회 실패", e);
            throw e;
        }
    }

    // 재료 나눔 : 게시글의 나눔 재료 조회
    @Override
    @Transactional(readOnly = true)
    public List<TogetherShareItemDto> listShareItems(Long share_id) throws Exception {

        try {
            if (share_id == null || share_id < 1) {
                throw new IllegalArgumentException("게시글 번호가 올바르지 않습니다.");
            }

            return mapper.listShareItems(share_id);

        } catch (Exception e) {
            log.error("재료나눔 글 재료 목록 조회 실패", e);
            throw e;
        }
    }

}
