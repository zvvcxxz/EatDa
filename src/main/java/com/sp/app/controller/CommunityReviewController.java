package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.common.PaginateUtil;
import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityReviewDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.CommunityRecipeService;
import com.sp.app.service.CommunityReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/community/cooking")
public class CommunityReviewController {
    private final CommunityReviewService service;
    private final CommunityRecipeService recipeService;
    private final PaginateUtil paginateUtil;

    // 후기 목록과 검색
    @GetMapping({"", "/"})
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int current_page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "recipeId", defaultValue = "0") long recipeId,
            Model model) {

        if (LoginMemberUtil.getSessionInfo() == null) {
            return "redirect:/member/login";
        }

        if (!List.of("all", "title", "content", "recipe", "author").contains(schType)) {
            schType = "all";
        }

        kwd = kwd.trim();
        recipeId = Math.max(0, recipeId);
        int size = 6;

        Map<String, Object> map = new HashMap<>();
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("recipeId", recipeId);

        int dataCount = service.dataCount(map);
        int totalPage = Math.max(1, paginateUtil.pageCount(dataCount, size));
        current_page = Math.max(1, Math.min(current_page, totalPage));

        map.put("offset", (current_page - 1) * size);
        map.put("size", size);

        List<CommunityReviewDto> reviews = service.listReview(map);

        model.addAttribute("reviews", reviews);
        model.addAttribute("dataCount", dataCount);
        model.addAttribute("page", current_page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("pageStart", Math.max(1, current_page - 2));
        model.addAttribute("pageEnd", Math.min(totalPage, current_page + 2));
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("recipeId", recipeId);
        model.addAttribute("pageTitle", "따라 만든 요리");
        model.addAttribute("activeMenu", "cooking");

        return "community/communityReview-list";
    }

    // 상세와 검색 조건에 맞는 이전글·다음글
    @GetMapping("/{reviewId}")
    public String article(
            @PathVariable("reviewId") long reviewId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "schType", defaultValue = "all") String schType,
            @RequestParam(name = "kwd", defaultValue = "") String kwd,
            @RequestParam(name = "recipeId", defaultValue = "0") long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null || userDetails == null) {
            return "redirect:/member/login";
        }

        CommunityReviewDto dto = service.findById(reviewId);

        if (dto == null) {
            return "redirect:/community/cooking";
        }

        if (!List.of("all", "title", "content", "recipe", "author").contains(schType)) {
            schType = "all";
        }

        page = Math.max(1, page);
        kwd = kwd.trim();
        recipeId = Math.max(0, recipeId);

        boolean owner = dto.getMemberId() == info.getMember_id();
        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        dto.setCanEdit(owner);
        dto.setCanDelete(owner);

        Map<String, Object> map = new HashMap<>();
        map.put("reviewId", reviewId);
        map.put("schType", schType);
        map.put("kwd", kwd);
        map.put("recipeId", recipeId);

        model.addAttribute("review", dto);
        model.addAttribute("prevReview", service.findByPrev(map));
        model.addAttribute("nextReview", service.findByNext(map));
        model.addAttribute("owner", owner);
        model.addAttribute("admin", admin);
        model.addAttribute("page", page);
        model.addAttribute("schType", schType);
        model.addAttribute("kwd", kwd);
        model.addAttribute("recipeId", recipeId);
        model.addAttribute("previewId", reviewId);
        model.addAttribute("pageTitle", dto.getTitle());
        model.addAttribute("activeMenu", "cooking");

        return "community/communityReview-detail";
    }

    // 등록 화면
    @GetMapping("/write")
    public String writeForm(
            @RequestParam(name = "recipeId", defaultValue = "0") long recipeId,
            Model model) {

        if (LoginMemberUtil.getSessionInfo() == null) {
            return "redirect:/member/login";
        }

        CommunityReviewDto dto = new CommunityReviewDto();
        dto.setRecipeId(Math.max(0, recipeId));

        // 변경: 원본 레시피 정보와 화면 데이터를 직접 전달
        CommunityRecipeDto selectedRecipe = null;

        if (dto.getRecipeId() > 0) {
            selectedRecipe = recipeService.findById(dto.getRecipeId());
        }

        model.addAttribute("review", dto);
        model.addAttribute("selectedRecipe", selectedRecipe);
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "후기 작성");
        model.addAttribute("activeMenu", "cooking");

        return "community/communityReview-write";
    }

    // 등록 처리
    @PostMapping("/write")
    public String writeSubmit(
            @ModelAttribute("review") CommunityReviewDto dto,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        dto.setMemberId(info.getMember_id());

        // 변경: 등록 실패 시 사용할 화면 데이터
        CommunityRecipeDto selectedRecipe = null;

        if (dto.getRecipeId() > 0) {
            selectedRecipe = recipeService.findById(dto.getRecipeId());
        }

        model.addAttribute("selectedRecipe", selectedRecipe);
        model.addAttribute("mode", "write");
        model.addAttribute("pageTitle", "후기 작성");
        model.addAttribute("activeMenu", "cooking");

        // 변경: 입력값을 여기서 직접 확인
        if (dto.getRecipeId() <= 0) {
            model.addAttribute("error", "따라 만든 원본 레시피를 선택하세요.");
            return "community/communityReview-write";
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            model.addAttribute("error", "제목을 입력하세요.");
            return "community/communityReview-write";
        }

        dto.setTitle(dto.getTitle().trim());

        if (dto.getTitle().length() > 200) {
            model.addAttribute("error", "제목은 200자 이내로 입력하세요.");
            return "community/communityReview-write";
        }

        if (dto.getContent() == null || dto.getContent().isBlank()) {
            model.addAttribute("error", "요리 후기를 입력하세요.");
            return "community/communityReview-write";
        }

        if (dto.getContent().length() > 2000) {
            model.addAttribute("error", "요리 후기는 2000자 이내로 입력하세요.");
            return "community/communityReview-write";
        }

        if (selectedRecipe == null) {
            model.addAttribute("error", "공개 중인 원본 레시피를 선택하세요.");
            return "community/communityReview-write";
        }

        try {
            service.insertReview(dto);
        } catch (Exception e) {
            log.info("writeSubmit : ", e);
            model.addAttribute("error", "후기 등록에 실패했습니다.");
            return "community/communityReview-write";
        }

        return "redirect:/community/cooking/" + dto.getReviewId();
    }

    // 본인 후기 수정 화면
    @GetMapping("/{reviewId}/update")
    public String updateForm(
            @PathVariable("reviewId") long reviewId,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityReviewDto dto = service.findById(reviewId);

        if (dto == null) {
            return "redirect:/community/cooking";
        }

        if (dto.getMemberId() != info.getMember_id()) {
            return "redirect:/community/cooking";
        }

        // 변경: 수정 화면에 필요한 데이터를 직접 전달
        CommunityRecipeDto selectedRecipe = null;

        if (dto.getRecipeId() > 0) {
            selectedRecipe = recipeService.findById(dto.getRecipeId());
        }

        model.addAttribute("review", dto);
        model.addAttribute("selectedRecipe", selectedRecipe);
        model.addAttribute("previewId", reviewId);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "후기 수정");
        model.addAttribute("activeMenu", "cooking");

        return "community/communityReview-update";
    }

    // 원본 레시피 번호는 유지하고 후기만 수정
    @PostMapping("/{reviewId}/update")
    public String updateSubmit(
            @PathVariable("reviewId") long reviewId,
            @ModelAttribute("review") CommunityReviewDto dto,
            Model model) {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityReviewDto original = service.findById(reviewId);

        if (original == null) {
            return "redirect:/community/cooking";
        }

        if (original.getMemberId() != info.getMember_id()) {
            return "redirect:/community/cooking";
        }

        dto.setReviewId(reviewId);
        dto.setMemberId(info.getMember_id());
        dto.setRecipeId(original.getRecipeId());
        dto.setImageUrl(original.getImageUrl());
        dto.setRecipeTitle(original.getRecipeTitle());
        dto.setRecipeThumbnailUrl(original.getRecipeThumbnailUrl());
        dto.setOriginalActive(original.isOriginalActive());

        // 변경: 수정 실패 시 사용할 화면 데이터
        CommunityRecipeDto selectedRecipe = null;

        if (dto.getRecipeId() > 0) {
            selectedRecipe = recipeService.findById(dto.getRecipeId());
        }

        model.addAttribute("selectedRecipe", selectedRecipe);
        model.addAttribute("previewId", reviewId);
        model.addAttribute("mode", "update");
        model.addAttribute("pageTitle", "후기 수정");
        model.addAttribute("activeMenu", "cooking");

        // 변경: 수정 입력값도 여기서 직접 확인
        if (dto.getRecipeId() <= 0) {
            model.addAttribute("error", "따라 만든 원본 레시피를 확인하세요.");
            return "community/communityReview-update";
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            model.addAttribute("error", "제목을 입력하세요.");
            return "community/communityReview-update";
        }

        dto.setTitle(dto.getTitle().trim());

        if (dto.getTitle().length() > 200) {
            model.addAttribute("error", "제목은 200자 이내로 입력하세요.");
            return "community/communityReview-update";
        }

        if (dto.getContent() == null || dto.getContent().isBlank()) {
            model.addAttribute("error", "요리 후기를 입력하세요.");
            return "community/communityReview-update";
        }

        if (dto.getContent().length() > 2000) {
            model.addAttribute("error", "요리 후기는 2000자 이내로 입력하세요.");
            return "community/communityReview-update";
        }

        // 원본이 숨김·삭제되어도 본인 후기 수정은 가능
        try {
            if (service.updateReview(dto) == 0) {
                return "redirect:/community/cooking";
            }
        } catch (Exception e) {
            log.info("updateSubmit : ", e);
            model.addAttribute("error", "후기 수정에 실패했습니다.");
            return "community/communityReview-update";
        }

        return "redirect:/community/cooking/" + reviewId;
    }

    // 본인 후기 삭제. XML에서 STATUS = 2로 변경
    @PostMapping("/{reviewId}/delete")
    public String delete(
            @PathVariable("reviewId") long reviewId) throws Exception {

        SessionInfo info = LoginMemberUtil.getSessionInfo();

        if (info == null) {
            return "redirect:/member/login";
        }

        CommunityReviewDto dto = service.findById(reviewId);

        if (dto == null) {
            return "redirect:/community/cooking";
        }

        if (dto.getMemberId() != info.getMember_id()) {
            return "redirect:/community/cooking";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("reviewId", reviewId);
        map.put("memberId", info.getMember_id());

        service.deleteReview(map);

        return "redirect:/community/cooking";
    }

    // 관리자 숨김
    @PostMapping("/{reviewId}/hide")
    public String hide(
            @PathVariable("reviewId") long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails)
            throws Exception {

        if (userDetails == null) {
            return "redirect:/member/login";
        }

        boolean admin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!admin) {
            return "redirect:/community/cooking";
        }

        service.hideReview(reviewId);

        return "redirect:/community/cooking";
    }

    // 후기 작성 화면에서 원본 레시피 검색 : JSON
    @GetMapping("/recipes")
    public ResponseEntity<?> listRecipes(
            @RequestParam(name = "page", defaultValue = "1") int current_page,
            @RequestParam(name = "kwd", defaultValue = "") String kwd) {

        if (LoginMemberUtil.getSessionInfo() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        kwd = kwd.trim();
        int size = 8;

        Map<String, Object> map = new HashMap<>();
        map.put("schType", "all");
        map.put("kwd", kwd);
        map.put("categoryId", 0L);

        int dataCount = recipeService.dataCount(map);
        int totalPage = Math.max(1, paginateUtil.pageCount(dataCount, size));
        current_page = Math.max(1, Math.min(current_page, totalPage));

        map.put("offset", (current_page - 1) * size);
        map.put("size", size);

        List<CommunityRecipeDto> recipes = recipeService.listRecipe(map);

        return ResponseEntity.ok(Map.of(
                "recipes", recipes,
                "page", current_page,
                "totalPage", totalPage,
                "dataCount", dataCount));
    }
}