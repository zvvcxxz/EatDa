// CSRF가 설정된 경우 요청에 토큰 전달
function recipeRequestHeaders() {
    const headers = { AJAX: 'true' };
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (token && header) {
        headers[header] = token;
    }

    return headers;
}

function showRecipeToast(message) {
    const el = document.getElementById('recipeToast');

    if (!el) {
        alert(message);
        return;
    }

    el.textContent = message;
    el.hidden = false;

    clearTimeout(showRecipeToast.timeout);
    showRecipeToast.timeout = setTimeout(() => {
        el.hidden = true;
    }, 2500);
}

window.addEventListener('DOMContentLoaded', () => {
    const root = document.querySelector('.community-design');

    if (!root) return;

    // 좋아요와 찜 등록·취소
    root.addEventListener('click', ev => {
        const btnEL = ev.target.closest('button[data-recipe-action]');

        if (!btnEL || btnEL.disabled) return;

        const action = btnEL.dataset.recipeAction;
        const selected = btnEL.dataset.selected === 'true';
        const method = selected ? 'delete' : 'post';

        btnEL.disabled = true;

        fetchRequest(btnEL.dataset.url, method, null,
                'form', 'json', recipeRequestHeaders())
            .then(data => {
                if (!data) return;

                const isLike = action === 'like';
                const active = isLike ? data.liked : data.bookmarked;
                const count = isLike ? data.likeCount : data.bookmarkCount;

                // 서버에서 반환한 실제 선택 여부와 개수 반영
                btnEL.dataset.selected = String(active);
                btnEL.setAttribute('aria-pressed', String(active));
                btnEL.classList.toggle('active', active);

                btnEL.querySelector('.action-label').textContent = isLike
                        ? (active ? '♥ 좋아요' : '♡ 좋아요')
                        : (active ? '✓ 찜' : '＋ 찜');

                btnEL.querySelector('.action-count').textContent = count;

                showRecipeToast(isLike
                        ? (active ? '좋아요를 눌렀어요.' : '좋아요를 취소했어요.')
                        : (active ? '레시피를 찜했어요.' : '찜을 취소했어요.'));
            })
            .catch(error => {
                console.log(error);
                showRecipeToast('처리하지 못했습니다. 다시 시도해 주세요.');
            })
            .finally(() => {
                btnEL.disabled = false;
            });
    });
});