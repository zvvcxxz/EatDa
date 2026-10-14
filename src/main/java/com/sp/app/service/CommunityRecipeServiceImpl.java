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

    // 기본 정보와 연결 데이터를 하나의 작업으로 저장
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

            // XML에서 생성한 레시피 번호가 dto에 들어옴
            mapper.insertRecipe(dto);

            for (CommunityRecipeStepDto step : steps) {
                step.setImageUrl(null);
            }

            saveRecipeDetails(dto, ingredients, steps, uploadedFiles);
        } catch (Exception e) {
            // 파일은 DB 롤백으로 지워지지 않으므로 따로 정리
            for (String filename : uploadedFiles) {
                storageService.deleteFile(uploadPath, filename);
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

            // 삭제를 선택했으면 사진 주소를 비움
            dto.setThumbnailUrl(dto.isDeleteThumbnail()
                    ? null : original.getThumbnailUrl());

            // 새 사진을 선택했으면 새 사진으로 변경
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

            // 단계 사진도 삭제 여부에 따라 유지하거나 비움
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

            return result;
        } catch (Exception e) {
            for (String filename : uploadedFiles) {
                storageService.deleteFile(uploadPath, filename);
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

    // 삭제·숨김은 XML에서 상태값 변경
    @Override
    public int deleteRecipe(Map<String, Object> map) throws Exception {
        try {
            return mapper.deleteRecipe(map);
        } catch (Exception e) {
            log.info("deleteRecipe : ", e);
            throw e;
        }
    }

    @Override
    public int hideRecipe(Map<String, Object> map) throws Exception {
        try {
            return mapper.hideRecipe(map);
        } catch (Exception e) {
            log.info("hideRecipe : ", e);
            throw e;
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