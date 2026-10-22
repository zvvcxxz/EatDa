// 검색 기능
window.searchList = function() {
    const f = document.searchForm;
    if(!f.kwd.value.trim()) return;
    
    const params = new URLSearchParams(new FormData(f)).toString();
    location.href = '/admin/ingredient/list?' + params;
};

// 모달 열기
window.openIngredientDialog = function(mode, ingredientId = 0, ingredientName = '', ingredientType = 'FOOD', defaultUnit = '', activeYn = 'Y') {
    const form = document.getElementById('ingredient-form');
    
    if (mode === 'write') {
        document.getElementById('dialog-title').innerText = "재료 등록";
        document.getElementById('dialog-submit-btn').innerText = "새 재료 등록";
        document.getElementById('wrap-activeYn').style.display = "none";
        form.action = '/admin/ingredient/write';
        form.reset();
    } else {
        document.getElementById('dialog-title').innerText = "재료 수정";
        document.getElementById('dialog-submit-btn').innerText = "수정 내용 저장";
        document.getElementById('wrap-activeYn').style.display = "grid";
        form.action = '/admin/ingredient/update';
        
        // 폼 데이터
        document.getElementById('form-ingredientId').value = ingredientId;
        document.getElementById('form-ingredientName').value = ingredientName;
        document.getElementById('form-ingredientType').value = ingredientType;
        document.getElementById('form-defaultUnit').value = defaultUnit;
        document.getElementById('form-activeYn').value = activeYn;
    }
    
    // 모달
    if (typeof window.openDialog === 'function') {
        window.openDialog('ingredient-dialog');
    } else {
        document.getElementById('ingredient-dialog').showModal();
    }
};

// 페이징
document.addEventListener("DOMContentLoaded", function() {
    if (window.ingredientData && typeof renderPaging === 'function') {
        let totalPage = Math.ceil(Number(window.ingredientData.dataCount) / Number(window.ingredientData.size)); 
        let url = '/admin/ingredient/list';
        
        if (window.ingredientData.kwd.trim() !== '') {
            url += `?schType=${window.ingredientData.schType}&kwd=${encodeURIComponent(window.ingredientData.kwd)}`;
        }
        
        let pagingHtml = renderPaging(Number(window.ingredientData.page), totalPage, url);
        const pageNav = document.querySelector('.page-navigation');
        if (pageNav) {
            pageNav.innerHTML = pagingHtml;
        }
    }
});