const replySessionEL = document.querySelector('div.reply-session');
const postsUrl = replySessionEL?.getAttribute('data-posts-url');

// CSRF가 설정되어 있으면 요청에 토큰 추가
function communityRequestHeaders() {
    const headers = { AJAX: 'true' };
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (token && header) {
        headers[header] = token;
    }

    return headers;
}

function initReplySystem() {
    if (!postsUrl) {
        alert('댓글 주소를 확인하세요.');
        return false;
    }

    return true;
}

window.addEventListener('DOMContentLoaded', () => loadContent(1));

// 댓글 목록 조회. 페이징 버튼도 이 함수 사용
function loadContent(page) {
    if (!initReplySystem()) return;

    return fetchRequest(postsUrl, 'get', { pageNo: page },
            'form', 'json', communityRequestHeaders())
        .then(data => {
            if (data) addNewReply(data);
        })
        .catch(error => alert(error.message));
}

function addNewReply(data) {
    const listReply = data.listComment;
    const totalCount = Number(data.totalCount) || 0;
    const pageNo = Number(data.pageNo) || 1;
    const totalPage = Number(data.totalPage) || 0;

    const listContentEL = document.querySelector('#listReply .list-content');
    const replyCountEL = document.querySelector('#listReply .reply-count');
    const replyPageEL = document.querySelector('#listReply .reply-page');
    const pageNavigationEL = document.querySelector('#listReply .page-navigation');

    replyCountEL.textContent = `댓글 ${totalCount}개`;
    replyPageEL.textContent = totalCount
            ? `${pageNo}/${totalPage} 페이지` : '';

    listContentEL.dataset.pageNo = pageNo;
    // 한 페이지뿐이면 페이지 번호를 숨김
    pageNavigationEL.innerHTML = totalPage > 1 ? data.paging : '';

    if (totalCount === 0) {
        listContentEL.innerHTML =
                '<p class="reply-empty">첫 댓글을 남겨보세요.</p>';
        return;
    }

    listContentEL.innerHTML = renderReplies(listReply, pageNo, data.admin);
}

// 선택한 댓글의 답글 조회
function listReplyAnswer(parentId) {
    if (!initReplySystem()) return;

    const url = `${postsUrl}/${parentId}/answers`;

    return fetchRequest(url, 'get', null,
            'form', 'json', communityRequestHeaders())
        .then(data => {
            if (data) addNewAnswer(data, parentId);
        })
        .catch(error => alert(error.message));
}

function addNewAnswer(data, parentId) {
    const answerListEL = document.getElementById('listReplyAnswer' + parentId);
    const answerCountEL = document.getElementById('answerCount' + parentId);

    if (!answerListEL || !answerCountEL) return;

    answerListEL.innerHTML = data.listAnswers.length
            ? renderReplyAnswers(data.listAnswers, data.admin)
            : '<p class="reply-empty">등록된 답글이 없습니다.</p>';

    answerCountEL.textContent = data.answerCount;
}

// 댓글 등록 후 첫 페이지 재조회
window.addEventListener('DOMContentLoaded', () => {
    const btnSendEL = replySessionEL.querySelector('.btnSendReply');

    btnSendEL.addEventListener('click', function() {
        if (!initReplySystem()) return;

        const taEL = this.closest('.reply-form').querySelector('textarea');
        const content = taEL.value.trim();

        if (!content) {
            taEL.focus();
            return;
        }

        this.disabled = true;

        fetchRequest(postsUrl, 'post', { content },
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (!data?.success) return;

                taEL.value = '';
                return loadContent(1);
            })
            .catch(error => alert(error.message))
            .finally(() => this.disabled = false);
    });
});

// 답글 영역 열기와 답글 등록
window.addEventListener('DOMContentLoaded', () => {
    const listReplyEL = document.getElementById('listReply');

    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest('.btnReplyAnswerLayout');
        if (!btnEL) return;

        const itemEL = btnEL.closest('.reply-item');
        const answerEL = itemEL.querySelector('.reply-answer');

        if (answerEL.classList.contains('d-none')) {
            listReplyAnswer(itemEL.dataset.id);
        }

        answerEL.classList.toggle('d-none');
    });

    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest('.btnSendReplyAnswer');
        if (!btnEL || !initReplySystem()) return;

        const itemEL = btnEL.closest('.reply-item');
        const taEL = itemEL.querySelector('.answer-content');
        const content = taEL.value.trim();

        if (!content) {
            taEL.focus();
            return;
        }

        const parentId = itemEL.dataset.id;
        const params = { content, parentCommentId: parentId };
        btnEL.disabled = true;

        fetchRequest(postsUrl, 'post', params,
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (!data?.success) return;

                taEL.value = '';
                return listReplyAnswer(parentId);
            })
            .catch(error => alert(error.message))
            .finally(() => btnEL.disabled = false);
    });
});

// 수정 입력창 열기와 취소
window.addEventListener('DOMContentLoaded', () => {
    const listReplyEL = document.getElementById('listReply');

    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest(
                '.editReply, .editReplyAnswer, .btnCancelReply');
        if (!btnEL) return;

        const itemEL = btnEL.closest('.answer-item, .reply-item');
        const editEL = itemEL.querySelector('.reply-edit');
        const contentEL = itemEL.querySelector('.reply-item-content');

        if (btnEL.classList.contains('btnCancelReply')) {
            editEL.classList.add('d-none');
            contentEL.classList.remove('d-none');
            return;
        }

        editEL.querySelector('textarea').value = contentEL.textContent;
        editEL.classList.remove('d-none');
        contentEL.classList.add('d-none');
        editEL.querySelector('textarea').focus();
    });

    // 수정 내용 저장
    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest('.btnSaveReply');
        if (!btnEL || !initReplySystem()) return;

        const itemEL = btnEL.closest('.answer-item, .reply-item');
        const taEL = itemEL.querySelector('.reply-edit textarea');
        const content = taEL.value.trim();

        if (!content) {
            taEL.focus();
            return;
        }

        const url = `${postsUrl}/${itemEL.dataset.id}/update`;
        btnEL.disabled = true;

        fetchRequest(url, 'post', { content },
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (data?.success) return reloadReplyList(itemEL);
            })
            .catch(error => alert(error.message))
            .finally(() => btnEL.disabled = false);
    });
});

// 본인 댓글·답글 삭제
window.addEventListener('DOMContentLoaded', () => {
    const listReplyEL = document.getElementById('listReply');

    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest('.deleteReply, .deleteReplyAnswer');
        if (!btnEL || !initReplySystem()) return;
        if (!confirm('댓글을 삭제할까요?')) return;

        const itemEL = btnEL.closest('.answer-item, .reply-item');
        const url = `${postsUrl}/${itemEL.dataset.id}`;
        btnEL.disabled = true;

        fetchRequest(url, 'delete', null,
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (data?.success) return reloadReplyList(itemEL);
            })
            .catch(error => alert(error.message))
            .finally(() => btnEL.disabled = false);
    });
});

// 관리자 댓글·답글 숨김
window.addEventListener('DOMContentLoaded', () => {
    const listReplyEL = document.getElementById('listReply');

    listReplyEL.addEventListener('click', ev => {
        const btnEL = ev.target.closest('.hideReply, .hideReplyAnswer');
        if (!btnEL || !initReplySystem()) return;
        if (!confirm('댓글을 숨길까요?')) return;

        const itemEL = btnEL.closest('.answer-item, .reply-item');
        const url = `${postsUrl}/${itemEL.dataset.id}/hide`;
        btnEL.disabled = true;

        fetchRequest(url, 'post', null,
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (data?.success) return reloadReplyList(itemEL);
            })
            .catch(error => alert(error.message))
            .finally(() => btnEL.disabled = false);
    });
});

// 답글 변경은 답글 목록만, 댓글 변경은 현재 페이지 재조회
function reloadReplyList(itemEL) {
    if (itemEL.classList.contains('answer-item')) {
        return listReplyAnswer(itemEL.dataset.parentId);
    }

    const page = document.querySelector('#listReply .list-content').dataset.pageNo;
    return loadContent(page);
}