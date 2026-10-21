package com.sp.app.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityRecipeIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeStepDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityRecipeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/recipes")
public class CommunityRecipeController {
    private final CommunityRecipeService service;
    private final PaginateUtil paginateUtil;

    // 목록·검색·카테고리 선택
    @GetMapping({"", "/"})
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int current_page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "categoryId", defaultValue = "0") long categoryId,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        if (!List.of("all", "title", "summary", "author").contains(schType)) {
            schType = "all";
        }

        kwd = kwd.trim();
        categoryId = Math.max(0, categoryId);
        int size = 6;

        Map<String, Object> map = new HashMap<>();
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("categoryId", categoryId);

        int dataCount = service.dataCount(map);
        int totalPage = Math.max(1, paginateUtil.pageCount(dataCount, size));
        current_page = Math.max(1, Math.min(current_page, totalPage));

        map.put("offset", (current_page - 1) * size);
        map.put("size", size);

        List<CommunityRecipeDto> recipes = service.listRecipe(map);

        // 로그인 회원이 좋아요·찜한 레시피 번호
        Set<Long> likedRecipeIds = new HashSet<>();
        Set<Long> bookmarkedRecipeIds = new HashSet<>();

        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("memberId", info.getMember_id());

        for (CommunityRecipeDto recipe : recipes) {
            memberMap.put("recipeId", recipe.getRecipeId());

            if (service.hasRecipeLike(memberMap) > 0) {
                likedRecipeIds.add(recipe.getRecipeId());
            }

            if (service.hasRecipeBookmark(memberMap) > 0) {
                bookmarkedRecipeIds.add(recipe.getRecipeId());
            }
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("likedRecipeIds", likedRecipeIds);
        model.addAttribute("bookmarkedRecipeIds", bookmarkedRecipeIds);
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("page", current_page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("pageStart", Math.max(1, current_page - 2));
        model.addAttribute("pageEnd", Math.min(totalPage, current_page + 2));
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("pageTitle", "모두의 레시피");
        model.addAttribute("activeMenu", "recipes");

        return "community/communityRecipe-list";
    }

    // 상세보기와 이전글·다음글
    @GetMapping("/{recipeId}")
    public String article(
            @PathVariable("recipeId") long recipeId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "categoryId", defaultValue = "0") long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null || userDetails == null) {
            return "redirect:/member/login";
        }

        CommunityRecipeDto dto = service.findById(recipeId);

        if (dto == null) {
            return "redirect:/community/recipes";
        }

        if (!List.of("all", "title", "summary", "author").contains(schType)) {
            schType = "all";
        }

        page = Math.max(1, page);
        kwd = kwd.trim();
        categoryId = Math.max(0, categoryId);

        boolean owner = Objects.equals(dto.getAuthorId(), info.getMember_id());
        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        dto.setCanEdit(owner);
        dto.setCanDelete(owner);

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("memberId", info.getMember_id());
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("categoryId", categoryId);

        model.addAttribute("recipe", dto);
        model.addAttribute("prevRecipe", service.findByPrev(map));
        model.addAttribute("nextRecipe", service.findByNext(map));
        model.addAttribute("recipeIngredients",
                service.listRecipeIngredient(recipeId));
        model.addAttribute("recipeSteps", service.listRecipeStep(recipeId));
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("tags", service.listTag());
        model.addAttribute("liked", service.hasRecipeLike(map) > 0);
        model.addAttribute("bookmarked", service.hasRecipeBookmark(map) > 0);
        model.addAttribute("owner", owner);
        model.addAttribute("admin", admin);
        model.addAttribute("previewId", recipeId);
        model.addAttribute("pageTitle", dto.getTitle());
        model.addAttribute("activeMenu", "recipes");
        model.addAttribute("page", page);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("categoryId", categoryId);

        return "community/communityRecipe-article";
    }

    // 등록 화면
    @GetMapping("/write")
    public String writeForm(Model model) {
        if (LoginMemberUtil.getSessionInfo() == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("recipe", new CommunityRecipeDto());

        // 변경: 화면에 필요한 데이터를 여기서 직접 전달
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("tags", service.listTag());
        model.addAttribute("ingredientCatalog", service.listIngredient(Map.of()));
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "레시피 작성");
        model.addAttribute("activeMenu", "recipes");

        return "community/communityRecipe-write";
    }

    // 등록 처리
    @PostMapping("/write")
    public String writeSubmit(
            @ModelAttribute("recipe") CommunityRecipeDto dto,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        dto.setAuthorId(info.getMember_id());

        // 변경: 입력 오류로 돌아갈 때도 선택 목록이 필요함
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("tags", service.listTag());
        model.addAttribute("ingredientCatalog", service.listIngredient(Map.of()));
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "레시피 작성");
        model.addAttribute("activeMenu", "recipes");

        // 변경: 입력값을 이 메서드 안에서 직접 확인
        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getTitle().length() > 200) {
            model.addAttribute("error", "제목은 1~200자로 입력하세요.");
            return "community/communityRecipe-write";
        }

        dto.setTitle(dto.getTitle().trim());

        if (dto.getSummary() != null && dto.getSummary().length() > 1000) {
            model.addAttribute("error", "레시피 소개는 1000자 이내로 입력하세요.");
            return "community/communityRecipe-write";
        }

        if (dto.getOtherIngredient() != null
                && dto.getOtherIngredient().length() > 100) {
            model.addAttribute("error", "기타 재료 설명은 100자 이내로 입력하세요.");
            return "community/communityRecipe-write";
        }

        if (dto.getCookMinutes() != null && dto.getCookMinutes() < 0) {
            model.addAttribute("error", "조리 시간은 0 이상으로 입력하세요.");
            return "community/communityRecipe-write";
        }

        if (dto.getServings() != null && dto.getServings() < 1) {
            model.addAttribute("error", "인분은 1 이상으로 입력하세요.");
            return "community/communityRecipe-write";
        }

        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            model.addAttribute("error", "재료를 하나 이상 선택하세요.");
            return "community/communityRecipe-write";
        }

        // 재료 사전에 있는 재료인지, 중복 선택했는지 확인
        Set<Long> ingredientIds = new HashSet<>();
        Set<Long> availableIngredientIds = new HashSet<>();

        service.listIngredient(Map.of()).forEach(
                ingredient -> availableIngredientIds.add(ingredient.getIngredientId()));

        for (CommunityRecipeIngredientDto ingredient : dto.getIngredients()) {
            if (ingredient == null
                    || !availableIngredientIds.contains(ingredient.getIngredientId())) {
                model.addAttribute("error", "재료는 사용 중인 재료 목록에서 선택하세요.");
                return "community/communityRecipe-write";
            }

            if (!ingredientIds.add(ingredient.getIngredientId())) {
                model.addAttribute("error", "같은 재료는 한 번만 선택하세요.");
                return "community/communityRecipe-write";
            }

            if (ingredient.getAmount() != null
                    && ingredient.getAmount().signum() < 0) {
                model.addAttribute("error", "재료 분량은 0 이상으로 입력하세요.");
                return "community/communityRecipe-write";
            }

            if (ingredient.getRequiredYn() != null
                    && !ingredient.getRequiredYn().isBlank()
                    && !List.of("Y", "N").contains(ingredient.getRequiredYn())) {
                model.addAttribute("error", "필수 재료 선택값을 확인하세요.");
                return "community/communityRecipe-write";
            }
        }

        if (dto.getSteps() == null || dto.getSteps().isEmpty()) {
            model.addAttribute("error", "조리 단계를 하나 이상 입력하세요.");
            return "community/communityRecipe-write";
        }

        for (CommunityRecipeStepDto step : dto.getSteps()) {
            if (step == null || step.getDescription() == null
                    || step.getDescription().isBlank()
                    || step.getDescription().length() > 2000) {
                model.addAttribute("error", "각 단계의 조리 방법은 1~2000자로 입력하세요.");
                return "community/communityRecipe-write";
            }

            if (step.getTimerSeconds() != null && step.getTimerSeconds() < 0) {
                model.addAttribute("error", "타이머 시간은 0 이상으로 입력하세요.");
                return "community/communityRecipe-write";
            }
        }

        Set<Long> categoryIds = new HashSet<>();
        service.listCategory().forEach(
                category -> categoryIds.add(category.getCategoryId()));

        if (dto.getCategoryIds() != null) {
            Set<Long> selectedIds = new HashSet<>();

            for (Long categoryId : dto.getCategoryIds()) {
                if (!categoryIds.contains(categoryId)
                        || !selectedIds.add(categoryId)) {
                    model.addAttribute("error", "카테고리 선택을 확인하세요.");
                    return "community/communityRecipe-write";
                }
            }
        }

        Set<Long> tagIds = new HashSet<>();
        service.listTag().forEach(tag -> tagIds.add(tag.getTagId()));

        if (dto.getTagIds() != null) {
            Set<Long> selectedIds = new HashSet<>();

            for (Long tagId : dto.getTagIds()) {
                if (!tagIds.contains(tagId) || !selectedIds.add(tagId)) {
                    model.addAttribute("error", "태그 선택을 확인하세요.");
                    return "community/communityRecipe-write";
                }
            }
        }

        try {
            service.insertRecipe(dto, dto.getIngredients(), dto.getSteps());
        } catch (Exception e) {
            log.info("writeSubmit : ", e);
            model.addAttribute("error", "레시피 등록에 실패했습니다.");
            return "community/communityRecipe-write";
        }

        return "redirect:/community/recipes/" + dto.getRecipeId();
    }

    // 수정 화면
    @GetMapping("/{recipeId}/update")
    public String updateForm(
            @PathVariable("recipeId") long recipeId,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityRecipeDto dto = service.findById(recipeId);

        if (dto == null) {
            return "redirect:/community/recipes";
        }

        if (!Objects.equals(dto.getAuthorId(), info.getMember_id())) {
            return "redirect:/community/recipes";
        }

        dto.setIngredients(service.listRecipeIngredient(recipeId));
        dto.setSteps(service.listRecipeStep(recipeId));

        model.addAttribute("recipe", dto);
        model.addAttribute("previewId", recipeId);

        // 변경: 수정 화면의 선택 목록을 직접 전달
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("tags", service.listTag());
        model.addAttribute("ingredientCatalog", service.listIngredient(Map.of()));
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "레시피 수정");
        model.addAttribute("activeMenu", "recipes");

        return "community/communityRecipe-update";
    }

    // 수정 처리
    @PostMapping("/{recipeId}/update")
    public String updateSubmit(
            @PathVariable("recipeId") long recipeId,
            @ModelAttribute("recipe") CommunityRecipeDto dto,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityRecipeDto original = service.findById(recipeId);

        if (original == null) {
            return "redirect:/community/recipes";
        }

        if (!Objects.equals(original.getAuthorId(), info.getMember_id())) {
            return "redirect:/community/recipes";
        }

        dto.setRecipeId(recipeId);
        dto.setAuthorId(info.getMember_id());
        dto.setThumbnailUrl(original.getThumbnailUrl());

        model.addAttribute("previewId", recipeId);

        // 변경: 수정 실패 시 사용할 화면 데이터
        model.addAttribute("categories", service.listCategory());
        model.addAttribute("tags", service.listTag());
        model.addAttribute("ingredientCatalog", service.listIngredient(Map.of()));
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "레시피 수정");
        model.addAttribute("activeMenu", "recipes");

        // 변경: 수정 입력값도 이 메서드 안에서 직접 확인
        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getTitle().length() > 200) {
            model.addAttribute("error", "제목은 1~200자로 입력하세요.");
            return "community/communityRecipe-update";
        }

        dto.setTitle(dto.getTitle().trim());

        if (dto.getSummary() != null && dto.getSummary().length() > 1000) {
            model.addAttribute("error", "레시피 소개는 1000자 이내로 입력하세요.");
            return "community/communityRecipe-update";
        }

        if (dto.getOtherIngredient() != null
                && dto.getOtherIngredient().length() > 100) {
            model.addAttribute("error", "기타 재료 설명은 100자 이내로 입력하세요.");
            return "community/communityRecipe-update";
        }

        if (dto.getCookMinutes() != null && dto.getCookMinutes() < 0) {
            model.addAttribute("error", "조리 시간은 0 이상으로 입력하세요.");
            return "community/communityRecipe-update";
        }

        if (dto.getServings() != null && dto.getServings() < 1) {
            model.addAttribute("error", "인분은 1 이상으로 입력하세요.");
            return "community/communityRecipe-update";
        }

        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            model.addAttribute("error", "재료를 하나 이상 선택하세요.");
            return "community/communityRecipe-update";
        }

        Set<Long> ingredientIds = new HashSet<>();
        Set<Long> availableIngredientIds = new HashSet<>();

        service.listIngredient(Map.of()).forEach(
                ingredient -> availableIngredientIds.add(ingredient.getIngredientId()));

        for (CommunityRecipeIngredientDto ingredient : dto.getIngredients()) {
            if (ingredient == null
                    || !availableIngredientIds.contains(ingredient.getIngredientId())) {
                model.addAttribute("error", "재료는 사용 중인 재료 목록에서 선택하세요.");
                return "community/communityRecipe-update";
            }

            if (!ingredientIds.add(ingredient.getIngredientId())) {
                model.addAttribute("error", "같은 재료는 한 번만 선택하세요.");
                return "community/communityRecipe-update";
            }

            if (ingredient.getAmount() != null
                    && ingredient.getAmount().signum() < 0) {
                model.addAttribute("error", "재료 분량은 0 이상으로 입력하세요.");
                return "community/communityRecipe-update";
            }

            if (ingredient.getRequiredYn() != null
                    && !ingredient.getRequiredYn().isBlank()
                    && !List.of("Y", "N").contains(ingredient.getRequiredYn())) {
                model.addAttribute("error", "필수 재료 선택값을 확인하세요.");
                return "community/communityRecipe-update";
            }
        }

        if (dto.getSteps() == null || dto.getSteps().isEmpty()) {
            model.addAttribute("error", "조리 단계를 하나 이상 입력하세요.");
            return "community/communityRecipe-update";
        }

        for (CommunityRecipeStepDto step : dto.getSteps()) {
            if (step == null || step.getDescription() == null
                    || step.getDescription().isBlank()
                    || step.getDescription().length() > 2000) {
                model.addAttribute("error", "각 단계의 조리 방법은 1~2000자로 입력하세요.");
                return "community/communityRecipe-update";
            }

            if (step.getTimerSeconds() != null && step.getTimerSeconds() < 0) {
                model.addAttribute("error", "타이머 시간은 0 이상으로 입력하세요.");
                return "community/communityRecipe-update";
            }
        }

        Set<Long> categoryIds = new HashSet<>();
        service.listCategory().forEach(
                category -> categoryIds.add(category.getCategoryId()));

        if (dto.getCategoryIds() != null) {
            Set<Long> selectedIds = new HashSet<>();

            for (Long categoryId : dto.getCategoryIds()) {
                if (!categoryIds.contains(categoryId)
                        || !selectedIds.add(categoryId)) {
                    model.addAttribute("error", "카테고리 선택을 확인하세요.");
                    return "community/communityRecipe-update";
                }
            }
        }

        Set<Long> tagIds = new HashSet<>();
        service.listTag().forEach(tag -> tagIds.add(tag.getTagId()));

        if (dto.getTagIds() != null) {
            Set<Long> selectedIds = new HashSet<>();

            for (Long tagId : dto.getTagIds()) {
                if (!tagIds.contains(tagId) || !selectedIds.add(tagId)) {
                    model.addAttribute("error", "태그 선택을 확인하세요.");
                    return "community/communityRecipe-update";
                }
            }
        }

        try {
            if (service.updateRecipe(dto, dto.getIngredients(), dto.getSteps()) == 0) {
                return "redirect:/community/recipes";
            }
        } catch (Exception e) {
            log.info("updateSubmit : ", e);
            model.addAttribute("error", "레시피 수정에 실패했습니다.");
            return "community/communityRecipe-update";
        }

        return "redirect:/community/recipes/" + recipeId;
    }

    // 본인 레시피 삭제
    @PostMapping("/{recipeId}/delete")
    public String delete(
            @PathVariable("recipeId") long recipeId) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityRecipeDto dto = service.findById(recipeId);

        if (dto == null) {
            return "redirect:/community/recipes";
        }

        if (!Objects.equals(dto.getAuthorId(), info.getMember_id())) {
            return "redirect:/community/recipes";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("authorId", info.getMember_id());

        service.deleteRecipe(map);

        return "redirect:/community/recipes";
    }

    // 관리자 숨김
    @PostMapping("/{recipeId}/hide")
    public String hide(
            @PathVariable("recipeId") long recipeId,
            @RequestParam(name = "blindReason", defaultValue = "") String blindReason,
            @AuthenticationPrincipal CustomUserDetails userDetails)
            throws Exception {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!admin) {
            return "redirect:/community/recipes";
        }

        if (blindReason.length() > 500) {
            return "redirect:/community/recipes";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("blindReason", blindReason.trim());

        service.hideRecipe(map);

        return "redirect:/community/recipes";
    }

    // 요리모드
    @GetMapping("/{recipeId}/mode")
    public String cookingMode(
            @PathVariable("recipeId") long recipeId,
            Model model) {

        if (LoginMemberUtil.getSessionInfo() == null) {
            return "redirect:/member/login";
        }

        CommunityRecipeDto dto = service.findById(recipeId);

        if (dto == null) {
            return "redirect:/community/recipes";
        }

        model.addAttribute("recipe", dto);
        model.addAttribute("recipeSteps", service.listRecipeStep(recipeId));
        model.addAttribute("previewId", recipeId);
        model.addAttribute("pageTitle", "요리모드");
        model.addAttribute("activeMenu", "recipes");

        return "community/communityRecipe-mode";
    }

    // 좋아요 등록 : JSON
    @PostMapping("/like/{recipeId}")
    public ResponseEntity<?> insertRecipeLike(
            @PathVariable("recipeId") long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (service.findById(recipeId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.insertRecipeLike(map);
        } catch (DuplicateKeyException e) {
            // 이미 등록된 좋아요는 유지
        } catch (Exception e) {
            log.info("insertRecipeLike : ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "좋아요 처리에 실패했습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "likeCount", service.recipeLikeCount(recipeId),
                "liked", service.hasRecipeLike(map) > 0));
    }

    // 좋아요 취소 : JSON
    @DeleteMapping("/like/{recipeId}")
    public ResponseEntity<?> deleteRecipeLike(
            @PathVariable("recipeId") long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (service.findById(recipeId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.deleteRecipeLike(map);

            return ResponseEntity.ok(Map.of(
                    "likeCount", service.recipeLikeCount(recipeId),
                    "liked", service.hasRecipeLike(map) > 0));
        } catch (Exception e) {
            log.info("deleteRecipeLike : ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "좋아요 취소에 실패했습니다."));
        }
    }

    // 찜 등록 : JSON
    @PostMapping("/bookmark/{recipeId}")
    public ResponseEntity<?> insertRecipeBookmark(
            @PathVariable("recipeId") long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (service.findById(recipeId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.insertRecipeBookmark(map);
        } catch (DuplicateKeyException e) {
            // 이미 등록된 찜은 유지
        } catch (Exception e) {
            log.info("insertRecipeBookmark : ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "찜 처리에 실패했습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "bookmarkCount", service.recipeBookmarkCount(recipeId),
                "bookmarked", service.hasRecipeBookmark(map) > 0));
    }

    // 찜 취소 : JSON
    @DeleteMapping("/bookmark/{recipeId}")
    public ResponseEntity<?> deleteRecipeBookmark(
            @PathVariable("recipeId") long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (service.findById(recipeId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("recipeId", recipeId);
        map.put("memberId", userDetails.getMember().getMember_id());

        try {
            service.deleteRecipeBookmark(map);

            return ResponseEntity.ok(Map.of(
                    "bookmarkCount", service.recipeBookmarkCount(recipeId),
                    "bookmarked", service.hasRecipeBookmark(map) > 0));
        } catch (Exception e) {
            log.info("deleteRecipeBookmark : ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "찜 취소에 실패했습니다."));
        }
    }
}