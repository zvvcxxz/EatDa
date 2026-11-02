// 레시피 검색
function searchList() {
    const f = document.searchForm;
    if(f.page) f.page.value = "1";
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