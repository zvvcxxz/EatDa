package com.sp.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.CommunityCategoryDto;
import com.sp.app.domain.dto.CommunityIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityRecipeIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeStepDto;
import com.sp.app.domain.dto.CommunityTagDto;
import com.sp.app.mapper.CommunityRecipeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityRecipeServiceImpl implements CommunityRecipeService {
    private final CommunityRecipeMapper mapper;
    private final StorageService storageService;

    @Value("${file.upload-root}/community/recipes")
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
    public List<CommunityRecipeDto> listRecipe(Map<String, Object> map) {
        List<CommunityRecipeDto> list = List.of();

        try {
            list = mapper.listRecipe(map);
        } catch (Exception e) {
            log.info("listRecipe : ", e);
        }

        return list;
    }

    @Override
    public CommunityRecipeDto findById(long recipeId) {
        CommunityRecipeDto dto = null;

        try {
            dto = mapper.findById(recipeId);

            if (dto != null) {
                dto.setCategoryIds(mapper.listCategoryIds(recipeId));
                dto.setTagIds(mapper.listTagIds(recipeId));
            }
        } catch (Exception e) {
            log.info("findById : ", e);
            dto = null;
        }

        return dto;
    }

    // 기본 정보와 재료·단계를 함께 저장
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertRecipe(
            CommunityRecipeDto dto,
            List<CommunityRecipeIngredientDto> ingredients,
            List<CommunityRecipeStepDto> steps) throws Exception {

        List<String> uploadedFiles = new ArrayList<>();

        try {
            if (dto.getAuthorId() == null) {
                throw new IllegalArgumentException("작성자 정보가 없습니다.");
            }

            if (steps == null || steps.isEmpty()) {
                throw new IllegalArgumentException("조리 단계를 입력하세요.");
            }

            dto.setSourceType("MEMBER");
            dto.setExternalKey(null);
            dto.setThumbnailUrl(null);

            if (dto.getSelectFile() != null
                    && !dto.getSelectFile().isEmpty()) {

                String filename = storageService.uploadFileToServer(
                        dto.getSelectFile(), uploadPath);

                uploadedFiles.add(filename);
                dto.setThumbnailUrl("/uploads/community/recipes/" + filename);
            }

            mapper.insertRecipe(dto);

            for (CommunityRecipeStepDto step : steps) {
                step.setImageUrl(null);
            }

            saveRecipeDetails(dto, ingredients, steps, uploadedFiles);
        } catch (Exception e) {
            // 등록 실패 시 이번에 올린 사진 정리
            for (String filename : uploadedFiles) {
                deletePhoto("/uploads/community/recipes/" + filename);
            }

            log.info("insertRecipe : ", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRecipe(
            CommunityRecipeDto dto,
            List<CommunityRecipeIngredientDto> ingredients,
            List<CommunityRecipeStepDto> steps) throws Exception {

        List<String> uploadedFiles = new ArrayList<>();

        try {
            CommunityRecipeDto original = mapper.findById(dto.getRecipeId());

            if (original == null) {
                return 0;
            }

            // 본인이 작성한 회원 레시피만 수정
            if (!"MEMBER".equals(original.getSourceType())
                    || dto.getAuthorId() == null
                    || !Objects.equals(original.getAuthorId(), dto.getAuthorId())) {
                throw new IllegalArgumentException("수정 권한이 없습니다.");
            }

            if (steps == null || steps.isEmpty()) {
                throw new IllegalArgumentException("조리 단계를 입력하세요.");
            }

            dto.setSourceType(original.getSourceType());
            dto.setExternalKey(original.getExternalKey());

            dto.setThumbnailUrl(dto.isDeleteThumbnail()
                    ? null : original.getThumbnailUrl());

            if (dto.getSelectFile() != null
                    && !dto.getSelectFile().isEmpty()) {

                String filename = storageService.uploadFileToServer(
                        dto.getSelectFile(), uploadPath);

                uploadedFiles.add(filename);
                dto.setThumbnailUrl("/uploads/community/recipes/" + filename);
            }

            List<CommunityRecipeStepDto> originalSteps =
                    mapper.listRecipeStep(dto.getRecipeId());

            Map<Long, String> imageMap = new HashMap<>();

            for (CommunityRecipeStepDto step : originalSteps) {
                imageMap.put(step.getStepId(), step.getImageUrl());
            }

            // 새 사진이 없으면 기존 단계 사진 유지
            for (CommunityRecipeStepDto step : steps) {
                step.setImageUrl(step.isDeleteImage()
                        ? null : imageMap.get(step.getStepId()));
            }

            int result = mapper.updateRecipe(dto);

            if (result == 0) {
                throw new IllegalStateException("레시피를 수정하지 못했습니다.");
            }

            mapper.deleteRecipeCategory(dto.getRecipeId());
            mapper.deleteRecipeTag(dto.getRecipeId());
            mapper.deleteRecipeIngredient(dto.getRecipeId());
            mapper.deleteRecipeStep(dto.getRecipeId());

            saveRecipeDetails(dto, ingredients, steps, uploadedFiles);

            // 수정 후에도 사용하는 사진 주소
            List<String> keepImages = new ArrayList<>();
            keepImages.add(dto.getThumbnailUrl());

            for (CommunityRecipeStepDto step : steps) {
                keepImages.add(step.getImageUrl());
            }

            // 더 이상 사용하지 않는 대표·단계 사진 삭제
            if (!keepImages.contains(original.getThumbnailUrl())) {
                deletePhoto(original.getThumbnailUrl());
            }

            for (CommunityRecipeStepDto step : originalSteps) {
                if (!keepImages.contains(step.getImageUrl())) {
                    deletePhoto(step.getImageUrl());
                }
            }

            return result;
        } catch (Exception e) {
            for (String filename : uploadedFiles) {
                deletePhoto("/uploads/community/recipes/" + filename);
            }

            log.info("updateRecipe : ", e);
            throw e;
        }
    }

    // 등록·수정에서 공통으로 사용하는 연결 데이터 저장
    private void saveRecipeDetails(
            CommunityRecipeDto dto,
            List<CommunityRecipeIngredientDto> ingredients,
            List<CommunityRecipeStepDto> steps,
            List<String> uploadedFiles) throws Exception {

        long recipeId = dto.getRecipeId();

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);

        if (dto.getCategoryIds() != null) {
            for (Long categoryId : dto.getCategoryIds()) {
                map.put("categoryId", categoryId);
                mapper.insertRecipeCategory(map);
            }
        }

        if (dto.getTagIds() != null) {
            for (Long tagId : dto.getTagIds()) {
                map.put("tagId", tagId);
                mapper.insertRecipeTag(map);
            }
        }

        if (ingredients != null) {
            int sortOrder = 1;

            for (CommunityRecipeIngredientDto ingredient : ingredients) {
                ingredient.setRecipeId(recipeId);
                ingredient.setSortOrder(sortOrder++);

                if (ingredient.getRequiredYn() == null
                        || ingredient.getRequiredYn().isBlank()) {
                    ingredient.setRequiredYn("Y");
                }

                mapper.insertRecipeIngredient(ingredient);
            }
        }

        int stepNo = 1;

        for (CommunityRecipeStepDto step : steps) {
            step.setRecipeId(recipeId);
            step.setStepNo(stepNo++);

            if (step.getSelectFile() != null
                    && !step.getSelectFile().isEmpty()) {

                String filename = storageService.uploadFileToServer(
                        step.getSelectFile(), uploadPath);

                uploadedFiles.add(filename);
                step.setImageUrl("/uploads/community/recipes/" + filename);
            }

            mapper.insertRecipeStep(step);
        }
    }

    @Override
    public int deleteRecipe(Map<String, Object> map) throws Exception {
        try {
            long recipeId = Long.parseLong(
                    String.valueOf(map.get("recipeId")));

            long authorId = Long.parseLong(
                    String.valueOf(map.get("authorId")));

            CommunityRecipeDto original = mapper.findById(recipeId);

            if (original == null) {
                return 0;
            }

            if (!Objects.equals(original.getAuthorId(), authorId)) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }

            // DB에서 삭제 처리하기 전에 사진 주소 조회
            List<CommunityRecipeStepDto> steps =
                    mapper.listRecipeStep(recipeId);

            int result = mapper.deleteRecipe(map);

            if (result > 0) {
                deletePhoto(original.getThumbnailUrl());

                for (CommunityRecipeStepDto step : steps) {
                    deletePhoto(step.getImageUrl());
                }
            }

            return result;
        } catch (Exception e) {
            log.info("deleteRecipe : ", e);
            throw e;
        }
    }

    @Override
    public int hideRecipe(Map<String, Object> map) throws Exception {
        try {
            // 숨김은 사진을 삭제하지 않음
            return mapper.hideRecipe(map);
        } catch (Exception e) {
            log.info("hideRecipe : ", e);
            throw e;
        }
    }

    // 사진 주소에서 파일명을 꺼내 실제 파일 삭제
    private void deletePhoto(String imageUrl) {
        String prefix = "/uploads/community/recipes/";

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
    public List<CommunityCategoryDto> listCategory() {
        List<CommunityCategoryDto> list = List.of();

        try {
            list = mapper.listCategory();
        } catch (Exception e) {
            log.info("listCategory : ", e);
        }

        return list;
    }

    @Override
    public List<CommunityTagDto> listTag() {
        List<CommunityTagDto> list = List.of();

        try {
            list = mapper.listTag();
        } catch (Exception e) {
            log.info("listTag : ", e);
        }

        return list;
    }

    @Override
    public List<CommunityIngredientDto> listIngredient(Map<String, Object> map) {
        List<CommunityIngredientDto> list = List.of();

        try {
            list = mapper.listIngredient(map);
        } catch (Exception e) {
            log.info("listIngredient : ", e);
        }

        return list;
    }

    @Override
    public List<CommunityRecipeIngredientDto> listRecipeIngredient(long recipeId) {
        List<CommunityRecipeIngredientDto> list = List.of();

        try {
            list = mapper.listRecipeIngredient(recipeId);
        } catch (Exception e) {
            log.info("listRecipeIngredient : ", e);
        }

        return list;
    }

    @Override
    public List<CommunityRecipeStepDto> listRecipeStep(long recipeId) {
        List<CommunityRecipeStepDto> list = List.of();

        try {
            list = mapper.listRecipeStep(recipeId);
        } catch (Exception e) {
            log.info("listRecipeStep : ", e);
        }

        return list;
    }

    @Override
    public void insertRecipeLike(Map<String, Object> map) throws Exception {
        try {
            mapper.insertRecipeLike(map);
        } catch (Exception e) {
            log.info("insertRecipeLike : ", e);
            throw e;
        }
    }

    @Override
    public int deleteRecipeLike(Map<String, Object> map) throws Exception {
        try {
            return mapper.deleteRecipeLike(map);
        } catch (Exception e) {
            log.info("deleteRecipeLike : ", e);
            throw e;
        }
    }

    @Override
    public int recipeLikeCount(long recipeId) {
        int result = 0;

        try {
            result = mapper.recipeLikeCount(recipeId);
        } catch (Exception e) {
            log.info("recipeLikeCount : ", e);
        }

        return result;
    }

    @Override
    public int hasRecipeLike(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.hasRecipeLike(map);
        } catch (Exception e) {
            log.info("hasRecipeLike : ", e);
        }

        return result;
    }

    @Override
    public void insertRecipeBookmark(Map<String, Object> map) throws Exception {
        try {
            mapper.insertRecipeBookmark(map);
        } catch (Exception e) {
            log.info("insertRecipeBookmark : ", e);
            throw e;
        }
    }

    @Override
    public int deleteRecipeBookmark(Map<String, Object> map) throws Exception {
        try {
            return mapper.deleteRecipeBookmark(map);
        } catch (Exception e) {
            log.info("deleteRecipeBookmark : ", e);
            throw e;
        }
    }

    @Override
    public int recipeBookmarkCount(long recipeId) {
        int result = 0;

        try {
            result = mapper.recipeBookmarkCount(recipeId);
        } catch (Exception e) {
            log.info("recipeBookmarkCount : ", e);
        }

        return result;
    }

    @Override
    public int hasRecipeBookmark(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.hasRecipeBookmark(map);
        } catch (Exception e) {
            log.info("hasRecipeBookmark : ", e);
        }

        return result;
    }

    @Override
    public CommunityRecipeDto findByPrev(Map<String, Object> map) {
        CommunityRecipeDto dto = null;

        try {
            dto = mapper.findByPrev(map);
        } catch (Exception e) {
            log.info("findByPrev : ", e);
        }

        return dto;
    }

    @Override
    public CommunityRecipeDto findByNext(Map<String, Object> map) {
        CommunityRecipeDto dto = null;

        try {
            dto = mapper.findByNext(map);
        } catch (Exception e) {
            log.info("findByNext : ", e);
        }

        return dto;
    }
}