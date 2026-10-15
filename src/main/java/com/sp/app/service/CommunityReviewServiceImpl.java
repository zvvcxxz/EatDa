package com.sp.app.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.CommunityReviewDto;
import com.sp.app.mapper.CommunityRecipeMapper;
import com.sp.app.mapper.CommunityReviewMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityReviewServiceImpl implements CommunityReviewService {
    private final CommunityReviewMapper mapper;
    private final CommunityRecipeMapper recipeMapper;
    private final StorageService storageService;

    @Value("${file.upload-root}/community/cooking")
    private String uploadPath;

    @Override
    public int dataCount(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.dataCount(map);
        } catch (Exception e) {
            log.info("dataCount : ", e);
        }

        return result;
    }

    @Override
    public List<CommunityReviewDto> listReview(Map<String, Object> map) {
        List<CommunityReviewDto> list = List.of();

        try {
            list = mapper.listReview(map);
        } catch (Exception e) {
            log.info("listReview : ", e);
        }

        return list;
    }

    @Override
    public CommunityReviewDto findById(long reviewId) {
        CommunityReviewDto dto = null;

        try {
            dto = mapper.findById(reviewId);
        } catch (Exception e) {
            log.info("findById : ", e);
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertReview(CommunityReviewDto dto) throws Exception {
        String uploadedFile = null;

        try {
            if (dto.getMemberId() <= 0) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }

            // 공개된 회원 레시피에만 새 후기 작성
            if (recipeMapper.findById(dto.getRecipeId()) == null) {
                throw new IllegalArgumentException(
                        "후기를 작성할 수 없는 레시피입니다.");
            }

            dto.setImageUrl(null);
            dto.setStatus(0);

            if (dto.getSelectFile() != null
                    && !dto.getSelectFile().isEmpty()) {

                uploadedFile = storageService.uploadFileToServer(
                        dto.getSelectFile(), uploadPath);

                dto.setImageUrl(
                        "/uploads/community/cooking/" + uploadedFile);
            }

            mapper.insertReview(dto);
        } catch (Exception e) {
            // 등록 실패 시 이번에 올린 사진 정리
            if (uploadedFile != null) {
                deletePhoto("/uploads/community/cooking/" + uploadedFile);
            }

            log.info("insertReview : ", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReview(CommunityReviewDto dto) throws Exception {
        String uploadedFile = null;

        try {
            CommunityReviewDto original = mapper.findById(dto.getReviewId());

            if (original == null) {
                return 0;
            }

            if (original.getMemberId() != dto.getMemberId()) {
                throw new IllegalArgumentException("수정 권한이 없습니다.");
            }

            if (!Objects.equals(original.getStatus(), 0)) {
                throw new IllegalArgumentException(
                        "수정할 수 없는 후기입니다.");
            }

            // 원본 레시피 번호는 수정하지 않음
            dto.setRecipeId(original.getRecipeId());

            dto.setImageUrl(dto.isDeleteImage()
                    ? null : original.getImageUrl());

            if (dto.getSelectFile() != null
                    && !dto.getSelectFile().isEmpty()) {

                uploadedFile = storageService.uploadFileToServer(
                        dto.getSelectFile(), uploadPath);

                dto.setImageUrl(
                        "/uploads/community/cooking/" + uploadedFile);
            }

            int result = mapper.updateReview(dto);

            if (result == 0) {
                throw new IllegalStateException(
                        "후기를 수정하지 못했습니다.");
            }

            // 사진을 교체하거나 제거했으면 이전 파일 삭제
            if (!Objects.equals(original.getImageUrl(), dto.getImageUrl())) {
                deletePhoto(original.getImageUrl());
            }

            return result;
        } catch (Exception e) {
            if (uploadedFile != null) {
                deletePhoto("/uploads/community/cooking/" + uploadedFile);
            }

            log.info("updateReview : ", e);
            throw e;
        }
    }

    @Override
    public int deleteReview(Map<String, Object> map) throws Exception {
        try {
            long reviewId = Long.parseLong(
                    String.valueOf(map.get("reviewId")));

            long memberId = Long.parseLong(
                    String.valueOf(map.get("memberId")));

            CommunityReviewDto original = mapper.findById(reviewId);

            if (original == null) {
                return 0;
            }

            if (original.getMemberId() != memberId) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }

            int result = mapper.deleteReview(map);

            if (result > 0) {
                deletePhoto(original.getImageUrl());
            }

            return result;
        } catch (Exception e) {
            log.info("deleteReview : ", e);
            throw e;
        }
    }

    @Override
    public int hideReview(long reviewId) throws Exception {
        try {
            // 숨김은 사진을 삭제하지 않음
            return mapper.hideReview(reviewId);
        } catch (Exception e) {
            log.info("hideReview : ", e);
            throw e;
        }
    }

    // 사진 주소에서 파일명을 꺼내 실제 파일 삭제
    private void deletePhoto(String imageUrl) {
        String prefix = "/uploads/community/cooking/";

        if (imageUrl == null || !imageUrl.startsWith(prefix)) {
            return;
        }

        String filename = imageUrl.substring(prefix.length());

        if (filename.isBlank() || filename.contains("/")
                || filename.contains("\\") || filename.contains("..")) {
            return;
        }

        try {
            boolean deleted = storageService.deleteFile(uploadPath, filename);

            if (!deleted) {
                log.info("사진 삭제 확인 필요 : {}", filename);
            }
        } catch (Exception e) {
            log.info("deletePhoto : ", e);
        }
    }

    @Override
    public CommunityReviewDto findByPrev(Map<String, Object> map) {
        CommunityReviewDto dto = null;

        try {
            dto = mapper.findByPrev(map);
        } catch (Exception e) {
            log.info("findByPrev : ", e);
        }

        return dto;
    }

    @Override
    public CommunityReviewDto findByNext(Map<String, Object> map) {
        CommunityReviewDto dto = null;

        try {
            dto = mapper.findByNext(map);
        } catch (Exception e) {
            log.info("findByNext : ", e);
        }

        return dto;
    }
}