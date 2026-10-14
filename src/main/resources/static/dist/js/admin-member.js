document.addEventListener('DOMContentLoaded', function () {
    listMember(1);
    
    document.querySelectorAll('.custom-tabs button').forEach(button => {
        button.addEventListener('click', (e) => {
            document.querySelectorAll('.custom-tabs button').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');
            
            const roleId = e.target.getAttribute('data-role');
            document.memberSearchForm.roleId.value = roleId;
            resetList();
        });
    });
});

window.listMember = function(page) {
    const form = document.memberSearchForm;
    const params = new URLSearchParams(new FormData(form));
    params.set('page', page || 1);
    
    fetch(`/admin/member/list?${params.toString()}`)
        .then(res => res.text())
        .then(html => { 
            document.getElementById('nav-content').innerHTML = html; 
        })
        .catch(err => console.error(err));
};

window.resetList = function() {
    const f = document.memberSearchForm;
    f.schType.value = 'loginId';
    f.kwd.value = '';
    f.status.value = '';
    listMember(1);
};

window.searchList = function() {
    const searchType = document.getElementById('searchType').value;
    const keyword = document.getElementById('keyword').value;
    const statusFilter = document.getElementById('statusFilter') ? document.getElementById('statusFilter').value : '';
    
    const f = document.memberSearchForm;
    f.schType.value = searchType;
    f.kwd.value = keyword;
    f.status.value = statusFilter;
    
    listMember(1);
};

window.profile = function(memberId, page) {
    fetch(`/admin/member/profile?memberId=${memberId}&page=${page}`)
        .then(res => res.text())
        .then(html => { 
            document.getElementById('nav-content').innerHTML = html; 
        })
        .catch(err => console.error(err));
};

function getCsrfToken() {
    const tokenMeta = document.querySelector("meta[name='_csrf']");
    const headerMeta = document.querySelector("meta[name='_csrf_header']");
    return {
        token: tokenMeta ? tokenMeta.getAttribute("content") : '',
        header: headerMeta ? headerMeta.getAttribute("content") : ''
    };
}

window.updateMemberOk = function(page) {
    const f = document.getElementById('memberUpdateForm');
    if(!f.name.value.trim()) { alert('이름을 입력하세요.'); return; }
    if(!confirm('회원 정보를 수정하시겠습니까?')) return;
    
    const params = new URLSearchParams(new FormData(f));
    const csrf = getCsrfToken(); // 토큰 가져오기
    
    fetch('/admin/member/update', {
        method: 'PUT',
        body: params,
        headers: { 
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrf.header]: csrf.token // 💡 헤더에 CSRF 토큰 추가!
        }
    })
    .then(res => {
        if (!res.ok) {
            if (res.status === 403) throw new Error('보안 정책(CSRF 토큰 누락 등)에 의해 차단되었습니다.');
            throw new Error('서버와 통신 중 오류가 발생했습니다.');
        }
        return res.json();
    })
    .then(data => {
        if(data.state === "true" || data.success) {
            closeDialog(document.getElementById('memberUpdateDialog'));
            if(typeof window.notify === 'function') window.notify('회원 정보가 수정되었습니다.');
            else alert('회원 정보가 수정되었습니다.');
            
            profile(f.memberId.value, page);
        } else {
            alert('수정 실패: ' + (data.message || data.error || '알 수 없는 오류'));
        }
    })
    .catch(err => alert(err.message));
};

window.updateStatusOk = function(page) {
    const f = document.getElementById('memberStatusForm');
    if(!f.afterStatus.value) { alert('변경할 상태를 선택하세요.'); return; }
    if(!f.reason.value.trim()) { alert('상태 변경 사유를 입력하세요.'); return; }
    if(!confirm('회원 상태를 변경하시겠습니까?')) return;
    
    const params = new URLSearchParams(new FormData(f));
    const csrf = getCsrfToken();
    
    fetch('/admin/member/status', {
        method: 'POST',
        body: params,
        headers: { 
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrf.header]: csrf.token // 💡 헤더에 CSRF 토큰 추가!
        }
    })
    .then(res => {
        if (!res.ok) {
            if (res.status === 403) throw new Error('보안 정책(CSRF 토큰 누락 등)에 의해 차단되었습니다.');
            throw new Error('서버와 통신 중 오류가 발생했습니다.');
        }
        return res.json();
    })
    .then(data => {
        if(data.state === "true" || data.success) {
            closeDialog(document.getElementById('memberStatusDialog'));
            if(typeof window.notify === 'function') window.notify('회원 상태가 변경되었습니다.');
            else alert('회원 상태가 변경되었습니다.');
            
            profile(f.memberId.value, page);
        } else {
            alert('변경 실패: ' + (data.message || data.error || '알 수 없는 오류'));
        }
    })
    .catch(err => alert(err.message));
};