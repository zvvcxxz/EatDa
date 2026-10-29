// 함께하는 주방 : 좋아요, 작성자 메뉴, 나눔 신청과 승인
(() => {
    'use strict';
    const url = location.pathname;
    const type = /\/(share)/.test(url) ? 'share' : 'meet';
    const detail = url.match(/\/detail\/(\d+)/);
    const seen = new WeakMap();
    const make = (tag, text, className = '') => {
        const node = document.createElement(tag);
        node.textContent = text;
        node.className = className;
        return node;
    };
    const notice = make('p', '', 'tg-action-status');
    notice.setAttribute('role', 'status');
    document.querySelector('main')?.append(notice);
    const say = (message) => { notice.textContent = message; };

    // 요청 : 기존 로그인과 CSRF 토큰 사용
    async function request(path, post = false) {
        const headers = { Accept: 'application/json' };
        if (post) {
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const name = document.querySelector('meta[name="_csrf_header"]')?.content;
            if (token && name) headers[name] = token;
        }
        const response = await fetch('/together/actions/' + path, { method: post ? 'POST' : 'GET', headers });
        if (response.redirected || response.status === 401) throw new Error('로그인 후 이용해주세요.');
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(data.message || '요청에 실패했습니다. 다시 시도해주세요.');
        return data;
    }

    function likeButton(id, container, existing) {
        const button = existing || make('button', '좋아요', 'tg-card-action is-like');
        button.type = 'button';
        button.disabled = false;
        button.removeAttribute('aria-disabled');
        button.removeAttribute('data-pending');
        button.removeAttribute('title');
        if (!existing) container.prepend(button);
        const update = (state) => {
            button.textContent = (state.liked ? '♥ ' : '♡ ') + '좋아요 ' + state.likes;
            button.setAttribute('aria-pressed', String(state.liked));
        };
        button.onclick = async () => {
            button.disabled = true;
            try { update(await request(`${type}/${id}/like`, true)); }
            catch (error) { say(error.message); }
            finally { button.disabled = false; }
        };
        return update;
    }

    // 목록 : 처음 목록과 더보기 목록 모두 같은 버튼 적용
    function enhanceCards() {
        document.querySelectorAll('.tg-card').forEach((card) => {
            const link = card.querySelector('.tg-title');
            const id = link?.getAttribute('href')?.match(/\/detail\/(\d+)/)?.[1];
            if (!id || seen.get(card) === id) return;
            seen.set(card, id);
            card.querySelector('[data-meet-menu]')?.remove();
            let menu = card.querySelector('.tg-card-menu');
            if (type === 'meet') {
                menu = make('div', '', 'tg-card-menu tg-meet-menu');
                menu.dataset.meetMenu = '';
                const overlay = make('div', '', 'tg-card-overlay');
                overlay.dataset.meetMenu = '';
                overlay.append(menu);
                card.append(overlay);
                const detailLink = make('a', '상세보기', 'tg-card-action is-detail');
                detailLink.href = link.href;
                menu.append(detailLink);
            }
            if (!menu) return;
            const update = likeButton(id, menu, menu.querySelector('.is-like'));
            request(`${type}/${id}`).then((state) => {
                update(state);
                if (type !== 'meet' || !state.owner) return;
                const edit = make('a', '수정', 'tg-card-action is-edit');
                edit.href = `/together/meet/${id}/edit`;
                const source = document.querySelector('#meet-delete-form-template');
                if (source) {
                    const form = source.content.querySelector('form').cloneNode(true);
                    form.action = `/together/meet/${id}/delete`;
                    form.onsubmit = () => confirm('이 글을 삭제하시겠습니까?');
                    menu.insertBefore(edit, menu.querySelector('.is-detail'));
                    menu.insertBefore(form, menu.querySelector('.is-detail'));
                }
            }).catch((error) => say(error.message));
        });
    }
    const list = document.querySelector('.tg-list');
    if (list) {
        enhanceCards();
        new MutationObserver(enhanceCards).observe(list, { childList: true });
    }

    // 상세 : 기존 신청이 있으면 새로고침해도 상태 유지
    if (detail) {
        const id = detail[1];
        const container = type === 'meet' ? document.querySelector('.action-group-left') : null;
        const updateLike = container && likeButton(id, container, container.querySelector('.btn-like'));
        request(`${type}/${id}`).then((state) => {
            updateLike?.(state);
            if (type === 'share') renderShare(id, state);
            else {
                const apply = document.querySelector('form[action$="/apply"] button');
                if (apply && state.application != null) {
                    apply.disabled = true;
                    apply.textContent = ['신청됨', '참여 승인됨', '신청 거절됨'][state.application] || '신청됨';
                }
            }
        }).catch((error) => say(error.message));
    }

    function renderShare(id, state) {
        const panel = document.querySelector('#share-application-panel');
        if (!panel) return;
        const heading = make('div', '', 'tg-application-heading');
        heading.append(make('span', '함께 나누는 이웃', 'tg-application-eyebrow'),
            make('h2', state.owner ? '신청 내역' : '이 나눔에 함께하기'));
        const capacity = make('div', '', 'tg-application-capacity');
        capacity.append(make('span', '수령 승인'), make('strong', `${state.accepted} / ${state.capacity}명`));
        panel.replaceChildren(heading, capacity);
        if (!state.owner && state.application != null) {
            const status = make('div', '', 'tg-my-application');
            status.append(make('span', ['승인 대기', '수령 승인', '신청 거절'][state.application], 'tg-application-badge'),
                make('p', ['신청을 보냈어요. 작성자가 확인하면 이곳에서 알려드려요.',
                    '나눔이 연결되었어요. 작성자와 수령 약속을 정해주세요.',
                    '이번 신청은 연결되지 않았어요. 다른 나눔도 둘러보세요.'][state.application]));
            panel.append(status);
        }
        if (!state.owner && state.application == null) {
            const label = state.application == null ? '나눔 신청' : ['신청됨 · 승인 대기', '수령 승인됨', '신청 거절됨'][state.application];
            const apply = make('button', label, 'tg-apply-button');
            apply.type = 'button';
            apply.disabled = state.application != null;
            apply.onclick = async () => {
                apply.disabled = true;
                try { renderShare(id, await request(`share/${id}/apply`, true)); say('신청되었습니다. 작성자의 승인을 기다려주세요.'); }
                catch (error) { say(error.message); apply.disabled = false; }
            };
            panel.append(apply);
        }
        for (const applicant of state.owner ? state.applicants : []) {
            const row = make('div', '', 'tg-applicant');
            row.append(make('strong', applicant.name), make('span', ['승인 대기', '수령 연결됨', '거절됨'][applicant.status]));
            if (state.owner && applicant.status === 0) {
                for (const [decision, label] of [['accept', '수락'], ['reject', '거절']]) {
                    const button = make('button', label, 'tg-decision is-' + decision);
                    button.type = 'button';
                    button.onclick = async () => {
                        if (!confirm(`${applicant.name}님의 신청을 ${label}하시겠습니까?`)) return;
                        row.querySelectorAll('button').forEach((b) => b.disabled = true);
                        try { renderShare(id, await request(`share/${id}/applicants/${applicant.memberId}/${decision}`, true)); }
                        catch (error) { say(error.message); row.querySelectorAll('button').forEach((b) => b.disabled = false); }
                    };
                    row.append(button);
                }
            }
            panel.append(row);
        }
        if (state.owner && !state.applicants.length) panel.append(make('p', '아직 신청한 이웃이 없습니다.'));
        if (state.applicants.some((a) => a.status === 1)) {
            panel.append(make('p', '승인된 이웃과 수령 장소와 시간을 확인해주세요.'));
            const messages = make('a', '쪽지함에서 약속 정하기 ↗', 'tg-message-link');
            messages.href = '/my/messages';
            panel.append(messages);
        }
    }

    // 교환 글 : 작성과 수정 모두 조건 입력 필수
    document.querySelectorAll('form').forEach((form) => {
        const type = form.querySelector('[name="share_type"]');
        const note = form.querySelector('[name="exchange_note"]');
        if (!type || !note) return;
        function validate() {
            note.required = type.value === '1';
            note.setCustomValidity(note.required && !note.value.trim() ? '교환 조건을 입력해주세요.' : '');
        }
        type.addEventListener('change', validate);
        note.addEventListener('input', validate);
        form.addEventListener('submit', (event) => {
            validate();
            if (!note.checkValidity()) { event.preventDefault(); note.reportValidity(); }
        });
        validate();
    });
})();
