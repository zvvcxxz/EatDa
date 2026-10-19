// 검색 처리
window.searchList = function() {
    const f = document.searchForm;
    if(!f.kwd.value.trim()) return;
    
    const params = new URLSearchParams(new FormData(f)).toString();
    location.href = '/admin/tag/list?' + params;
};

// 열기 제어
window.openTagDialog = function(mode, tagId = 0, tagName = '', tagGroup = 'TIME', activeYn = 'Y') {
    const form = document.getElementById('tag-form');
    
    if (mode === 'write') {
        document.getElementById('dialog-title').innerText = "태그 등록";
        document.getElementById('dialog-submit-btn').innerText = "새 태그 등록";
        document.getElementById('wrap-activeYn').style.display = "none";
        form.action = '/admin/tag/write';
        form.reset();
    } else {
        document.getElementById('dialog-title').innerText = "태그 수정";
        document.getElementById('dialog-submit-btn').innerText = "수정 내용 저장";
        document.getElementById('wrap-activeYn').style.display = "grid";
        form.action = '/admin/tag/update';
        
        document.getElementById('form-tagId').value = tagId;
        document.getElementById('form-tagName').value = tagName;
        document.getElementById('form-tagGroup').value = tagGroup;
        document.getElementById('form-activeYn').value = activeYn;
    }
    
    if (typeof window.openDialog === 'function') {
        window.openDialog('tag-dialog');
    } else {
        document.getElementById('tag-dialog').showModal();
    }
};

// 페이징 렌더링
document.addEventListener("DOMContentLoaded", function() {
    if (window.tagData && typeof renderPaging === 'function') {
        let totalPage = Math.ceil(Number(window.tagData.dataCount) / Number(window.tagData.size)); 
        let url = '/admin/tag/list';
        
        if (window.tagData.kwd.trim() !== '') {
            url += `?schType=${window.tagData.schType}&kwd=${encodeURIComponent(window.tagData.kwd)}`;
        }
        
        let pagingHtml = renderPaging(Number(window.tagData.page), totalPage, url);
        const pageNav = document.querySelector('.page-navigation');
        if (pageNav) {
            pageNav.innerHTML = pagingHtml;
        }
    }

});