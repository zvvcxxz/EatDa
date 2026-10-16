// 레시피 검색
function searchList() {
    const f = document.searchForm;
    if(f.page) f.page.value = "1";
    f.submit();
}

// 레시피 탭
function searchTab(el) {
    const sourceType = el.getAttribute("data-source");
    const f = document.searchForm;
    
    f.sourceType.value = sourceType;
    f.submit();
}

// 초기화
function resetList() {
    location.href = location.pathname;
}

// 페이지 이동
function goPage(page) {
    const f = document.searchForm;
    f.page.value = page;
    f.submit();
}