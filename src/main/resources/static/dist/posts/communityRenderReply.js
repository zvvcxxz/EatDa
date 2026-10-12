// 입력한 글이 HTML로 실행되지 않도록 처리
function escapeReplyText(value) {
    const el = document.createElement('div');
    el.textContent = value ?? '';
    return el.innerHTML;
}

// 댓글 목록 HTML
function renderReplies(listReply, pageNo, admin) {
    return listReply.map(vo => {
        let menuHTML = '';

        if (vo.owner) {
            menuHTML += `
                <button type="button" class="community-button editReply">수정</button>
                <button type="button" class="community-button deleteReply">삭제</button>
            `;
        }

        if (admin) {
            menuHTML += `
                <button type="button" class="community-button hideReply">숨김</button>
            `;
        }

        return `
            <div class="reply-item" data-id="${vo.commentId}" data-page-no="${pageNo}">
                <div class="reply-item-header">
                    <strong>${escapeReplyText(vo.memberName)}</strong>
                    <span>${escapeReplyText(vo.createdAt)}</span>
                </div>

                <div class="reply-item-content">${escapeReplyText(vo.content)}</div>

                <div class="reply-edit d-none">
                    <textarea aria-label="댓글 수정"></textarea>
                    <div class="reply-form-buttons">
                        <button type="button" class="community-button btnSaveReply">
                            저장
                        </button>
                        <button type="button" class="community-button btnCancelReply">
                            취소
                        </button>
                    </div>
                </div>

                <div class="reply-item-footer">
                    <button type="button" class="community-button btnReplyAnswerLayout">
                        답글 <span id="answerCount${vo.commentId}">${vo.answerCount}</span>
                    </button>
                    ${menuHTML}
                </div>

                <div class="reply-answer d-none">
                    <div id="listReplyAnswer${vo.commentId}" class="answer-list"></div>

                    <label for="answerContent${vo.commentId}">답글 작성</label>
                    <textarea id="answerContent${vo.commentId}"
                              class="answer-content"
                              placeholder="답글을 입력하세요."></textarea>

                    <div class="reply-form-buttons">
                        <button type="button" class="community-button btnSendReplyAnswer">
                            답글 등록
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// 답글 목록 HTML
function renderReplyAnswers(listAnswers, admin) {
    return listAnswers.map(vo => {
        let menuHTML = '';

        if (vo.owner) {
            menuHTML += `
                <button type="button" class="community-button editReplyAnswer">수정</button>
                <button type="button" class="community-button deleteReplyAnswer">삭제</button>
            `;
        }

        if (admin) {
            menuHTML += `
                <button type="button" class="community-button hideReplyAnswer">숨김</button>
            `;
        }

        return `
            <div class="answer-item" data-id="${vo.commentId}"
                 data-parent-id="${vo.parentCommentId}">
                <div class="reply-item-header">
                    <strong>${escapeReplyText(vo.memberName)}</strong>
                    <span>${escapeReplyText(vo.createdAt)}</span>
                </div>

                <div class="reply-item-content">${escapeReplyText(vo.content)}</div>

                <div class="reply-edit d-none">
                    <textarea aria-label="답글 수정"></textarea>
                    <div class="reply-form-buttons">
                        <button type="button" class="community-button btnSaveReply">
                            저장
                        </button>
                        <button type="button" class="community-button btnCancelReply">
                            취소
                        </button>
                    </div>
                </div>

                <div class="reply-item-footer">${menuHTML}</div>
            </div>
        `;
    }).join('');
}