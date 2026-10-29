// 메인 AI 검색 : 검색 요청 / 결과 목록 / 상세보기 / 단계별 조리
// 메인 레시피 검색
(() => {
    'use strict';
    const island = document.querySelector('.ai-island');
    if (!island) return;
    const $ = (id) => document.getElementById(id);
    const toggle = $('ai-island-toggle');
    const panel = island.querySelector('.ai-results-panel');
    const launcher = island.querySelector('.ai-launcher');
    $('ai-recipe-submit').setAttribute('aria-controls', 'ai-island-body');
    $('ai-recipe-submit').setAttribute('aria-expanded', 'false');
    const body = $('ai-island-body');
    const input = $('ai-ingredient-input');
    const options = $('ai-ingredient-options');
    const picked = $('ai-picked');
    const status = $('ai-island-status');
    const resultsSection = $('ai-results');
    const list = $('ai-results-list');
    const more = $('ai-results-more');
    const reduced = () => matchMedia('(prefers-reduced-motion: reduce)').matches;
    // 화면 요소 : 태그와 문구 생성
    function el(tag, className, value) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (value !== undefined) node.textContent = value;
        return node;
    }
    // 화면 버튼 : 문구와 클릭 동작 연결
    function button(label, className, handler) {
        const node = el('button', className, label);
        node.type = 'button';
        if (handler) node.addEventListener('click', handler);
        return node;
    }
    // 닫기 버튼 : 아이콘과 접근성 이름 설정
    function closeIcon() {
        const node = button('', 'ai-icon-close');
        node.setAttribute('aria-label', '닫기');
        node.innerHTML =
            '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18"/></svg>';
        return node;
    }

    // 검색 결과창 : 열기 / 닫기 애니메이션 처리
    let open = false,
        panelAnimation,
        contentAnimation,
        launcherAnimation,
        panelTimer,
        panelVersion = 0;
    let savedOverflow = '',
        savedScroll = 0;
    function setOpen(value) {
        const version = ++panelVersion;
        const before = (island.classList.contains('is-open') ? panel : launcher).getBoundingClientRect();
        panelAnimation?.cancel();
        contentAnimation?.cancel();
        launcherAnimation?.cancel();
        clearTimeout(panelTimer);
        if (value) {
            const wasFloating = island.classList.contains('is-open');
            if (!wasFloating) {
                savedOverflow = document.documentElement.style.overflow;
                document.documentElement.style.overflow = 'hidden';
            }
            const headerBottom = Math.max(
                0,
                document.querySelector('.site-header')?.getBoundingClientRect().bottom || 0
            );
            island.style.setProperty(
                '--ai-panel-top',
                Math.max(16, Math.min(headerBottom + 12, window.innerHeight - 240)) + 'px'
            );
            open = true;
            island.classList.add('is-open');
            island.classList.remove('is-closing');
            body.inert = false;
            body.scrollTop = savedScroll;
            launcher.inert = true;
            panel.focus({ preventScroll: true });
            $('ai-recipe-submit').setAttribute('aria-expanded', 'true');
            const after = panel.getBoundingClientRect();
            const launcherRadius = getComputedStyle(launcher).borderTopLeftRadius;
            const panelRadius = getComputedStyle(panel).borderTopLeftRadius;
            if (!reduced()) {
                panelAnimation = panel.animate([
                    { ...box(before), borderRadius: launcherRadius },
                    { ...box(after), borderRadius: panelRadius }
                ], {
                    duration: 520,
                    easing: 'cubic-bezier(.22,1,.36,1)'
                });
                contentAnimation = body.animate([{ opacity: 0 }, { opacity: 1 }], {
                    duration: 300,
                    delay: 100,
                    fill: 'both',
                    easing: 'ease-out'
                });
            }
            rotatePlaceholder();
        } else {
            if (!island.classList.contains('is-open')) return;
            open = false;
            savedScroll = body.scrollTop;
            body.inert = true;
            stopPlaceholder();
            hideOptions();
            island.classList.add('is-closing');
            $('ai-recipe-submit').setAttribute('aria-expanded', 'false');
            const anchor = launcher.getBoundingClientRect();
            function finish() {
                if (version !== panelVersion) return;
                clearTimeout(panelTimer);
                island.classList.remove('is-open', 'is-closing');
                panelAnimation?.cancel();
                contentAnimation?.cancel();
                launcherAnimation?.cancel();
                document.documentElement.style.overflow = savedOverflow;
                launcher.inert = false;
                input.focus({ preventScroll: true });
            }
            if (reduced()) finish();
            else {
                contentAnimation = body.animate([{ opacity: 1 }, { opacity: 0 }], {
                    duration: 170,
                    fill: 'forwards'
                });
                // 닫기 : 원래 입력창을 겹쳐 보여주며 결과창을 접음
                launcherAnimation = launcher.animate([{ opacity: 0 }, { opacity: 1 }], {
                    duration: 200,
                    delay: 260,
                    fill: 'both',
                    easing: 'ease-out'
                });
                panelAnimation = panel.animate([
                    { ...box(before), opacity: 1, borderRadius: getComputedStyle(panel).borderTopLeftRadius },
                    { ...box(anchor), opacity: 0, borderRadius: getComputedStyle(launcher).borderTopLeftRadius }
                ], {
                    duration: 460,
                    easing: 'cubic-bezier(.4,0,.2,1)',
                    fill: 'forwards'
                });
                panelAnimation.onfinish = finish;
                panelTimer = setTimeout(finish, 560);
            }
        }
    }
    // 검색 결과창 : 전환에 필요한 위치와 크기 반환
    function box(rect) {
        return {
            left: rect.left + 'px',
            top: rect.top + 'px',
            width: rect.width + 'px',
            height: rect.height + 'px'
        };
    }
    toggle.addEventListener('click', () => setOpen(false));
    $('ai-edit-ingredients').addEventListener('click', () => setOpen(false));
    let placeholderTimer, placeholderFade;
    const examples = [
        '예: 돼지고기와 김치로 간단한 저녁 추천해줘',
        '예: 계란, 두부, 대파',
        '예: 김치를 활용한 국물 요리 알려줘',
        '예: 두부로 만들 수 있는 한식 추천해줘',
        '예: 돼지고기, 김치',
        '예: 계란으로 간단하게 먹을 요리 알려줘'
    ];
    let exampleIndex = Math.floor(Math.random() * examples.length);
    input.placeholder = examples[exampleIndex];
    // 검색 안내 : 글자별 등장 / 퇴장 효과
    const splitHint = el('span', 'ai-split-hint');
    splitHint.setAttribute('aria-hidden', 'true');
    splitHint.hidden = true;
    input.parentElement.append(splitHint);
    function drawHint() {
        splitHint.replaceChildren();
        Array.from(examples[exampleIndex]).forEach((letter, index) => {
            const character = el('span', 'ai-split-letter', letter === ' ' ? '\u00a0' : letter);
            character.style.setProperty('--letter-delay', Math.min(index * 22, 700) + 'ms');
            splitHint.append(character);
        });
        splitHint.classList.remove('is-leaving');
        splitHint.hidden = false;
        input.classList.add('ai-has-split-hint');
    }
    function stopPlaceholder() {
        clearTimeout(placeholderTimer);
        clearTimeout(placeholderFade);
        splitHint.hidden = true;
        splitHint.classList.remove('is-leaving');
        input.classList.remove('ai-placeholder-changing', 'ai-has-split-hint');
    }
    function rotatePlaceholder() {
        stopPlaceholder();
        if (document.hidden || input.value || document.activeElement === input || reduced()) return;
        drawHint();
        placeholderTimer = setTimeout(() => {
            splitHint.classList.add('is-leaving');
            placeholderFade = setTimeout(() => {
                exampleIndex =
                    (exampleIndex + 1 + Math.floor(Math.random() * (examples.length - 1))) % examples.length;
                input.placeholder = examples[exampleIndex];
                input.classList.remove('ai-placeholder-changing');
                rotatePlaceholder();
            }, 1000);
        }, 4500);
    }
    input.addEventListener('focus', stopPlaceholder);
    input.addEventListener('blur', rotatePlaceholder);
    document.addEventListener('visibilitychange', () =>
        document.hidden ? stopPlaceholder() : rotatePlaceholder()
    );
    matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', rotatePlaceholder);

    // 서버 요청 : 로그인 상태와 JSON 응답 확인
    async function requestJson(url, payload, signal) {
        const headers = { Accept: 'application/json' };
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        if (token && header) headers[header] = token;
        if (payload) headers['Content-Type'] = 'application/json';
        const response = await fetch(url, {
            method: payload ? 'POST' : 'GET',
            headers,
            ...(payload ? { body: JSON.stringify(payload) } : {}),
            signal
        });
        if (response.redirected || response.status === 401) {
            throw new Error('로그인 후 다시 검색해 주세요.');
        }
        if (response.status === 403) throw new Error('검색 권한을 확인해 주세요.');
        if (!response.headers.get('content-type')?.includes('application/json')) {
            throw new Error('서버가 JSON 형식으로 응답하지 않았습니다.');
        }
        const data = await response.json();
        if (!response.ok) throw new Error(data?.message || '레시피 요청에 실패했습니다.');
        return data;
    }

    // 레시피 정보 : 서버 필드명을 화면에서 사용할 이름으로 변경
    function normalizeRecipe(raw) {
        const value = (snake, camel) => raw[snake] ?? raw[camel] ?? '';
        const steps = [];
        const stepImages = [];
        for (let i = 1; i <= 20; i++) {
            const suffix = String(i).padStart(2, '0');
            const text = String(raw['manual' + suffix] ?? raw['manual_' + suffix] ?? '').trim();
            if (!text) continue;
            steps.push(text.replace(/^\s*\d+\.\s*/, ''));
            stepImages.push(value('manual_img' + suffix, 'manualImg' + suffix));
        }
        return {
            id: String(value('rcp_seq', 'rcpSeq')),
            title: String(value('rcp_nm', 'rcpNm') || '이름 없는 레시피'),
            image: value('att_file_no_main', 'attFileNoMain'),
            description: [value('rcp_pat2', 'rcpPat2'), value('rcp_way2', 'rcpWay2')]
                .filter(Boolean)
                .join(' · '),
            amounts: [value('rcp_parts_dtls', 'rcpPartsDtls')].filter(Boolean),
            have: Array.isArray(raw.haveIngredients) ? raw.haveIngredients : [],
            missing: Array.isArray(raw.missingIngredients) ? raw.missingIngredients : [],
            compared: Array.isArray(raw.haveIngredients) && Array.isArray(raw.missingIngredients),
            steps,
            stepImages
        };
    }

    // 이미지 주소 : http / https 주소만 사용
    function imageUrl(value) {
        if (!value) return '';
        try {
            const url = new URL(value, location.origin);
            return ['http:', 'https:'].includes(url.protocol) ? url.href : '';
        } catch {
            return '';
        }
    }

    // 검색 요청 : 이전 검색을 취소하고 최신 결과만 표시
    let requestVersion = 0;
    let searchRequest;
    let results = [];
    let shown = 0;
    let searchMode = 'text';
    // 검색 입력 : 기존 자동완성 목록 숨김
    function hideOptions() {
        options.hidden = true;
    }
    async function runSearch(ingredients) {
        const names = Array.isArray(ingredients)
            ? [...new Set(ingredients.map((item) => String(item.name ?? '').trim()).filter(Boolean))]
            : [];
        const keyword = input.value.trim();
        if (!names.length && keyword.length < 2) {
            status.textContent = '검색어를 두 글자 이상 입력해 주세요.';
            input.focus();
            return;
        }
        const version = ++requestVersion;
        searchRequest?.abort();
        searchRequest = new AbortController();
        searchMode = names.length ? 'fridge' : 'text';
        results = [];
        shown = 0;
        $('ai-availability').value = 'all';
        $('ai-availability').disabled = true;
        resultsSection.hidden = false;
        list.setAttribute('aria-busy', 'true');
        list.replaceChildren(el('p', 'ai-empty-state', '어울리는 레시피를 찾고 있어요.'));
        more.hidden = true;
        $('ai-result-total').textContent = '검색 중';
        $('ai-result-count').textContent = '';
        $('ai-results-query').textContent = names.length ? '선택한 재료 · ' + names.join(' · ') : keyword;
        status.textContent = '';
        if (!open) setOpen(true);
        try {
            const data = await requestJson(
                names.length ? '/home/recipe-ingredients' : '/home/recipe-search',
                names.length ? { ingredients: names } : { keyword },
                searchRequest.signal
            );
            if (version !== requestVersion) return;
            if (!Array.isArray(data.recipes)) throw new Error('레시피 응답 형식이 올바르지 않습니다.');
            const ids = new Set();
            results = data.recipes.map(normalizeRecipe).filter((recipe) => {
                if (!recipe.id || ids.has(recipe.id)) return false;
                ids.add(recipe.id);
                return true;
            });
            const compared = results.some((recipe) => recipe.compared);
            $('ai-availability').disabled = !compared;
            $('ai-availability').options[1].textContent = '비교 재료 모두 있음';
            $('ai-availability').options[2].textContent = '추가 재료 필요';
            const note = island.querySelector('.ai-match-note');
            if (note) note.textContent = compared
                ? 'AI 검색은 입력한 문장의 재료와 비교합니다. 소비기한 임박재료 검색은 선택한 냉장고 재료와 비교합니다. 양념도 포함하며 분량과 대체 가능 여부는 판단하지 않습니다.'
                : '레시피에 비교할 재료 정보가 있어야 재료 상태 필터를 사용할 수 있습니다.';
            renderResults();
        } catch (error) {
            if (error.name === 'AbortError' || version !== requestVersion) return;
            list.replaceChildren(el('p', 'ai-empty-state', error.message));
            $('ai-result-total').textContent = '검색 실패';
            status.textContent = error.message;
        } finally {
            if (version === requestVersion) list.setAttribute('aria-busy', 'false');
        }
    }

    // 결과 목록 : 재료 상태 필터와 더보기
    function filteredResults() {
        const filter = $('ai-availability').value;
        return results.filter(
            (recipe) =>
                filter === 'all' ||
                (recipe.compared &&
                    (filter === 'complete' ? !recipe.missing.length : recipe.missing.length > 0))
        );
    }
    // 검색 결과 : 목록과 더보기 위치 초기화
    function renderResults() {
        shown = 0;
        list.replaceChildren();
        appendResults();
    }
    function appendResults() {
        const recipes = filteredResults();
        recipes.slice(shown, shown + 4).forEach((recipe) => {
            const row = el('article', 'ai-recipe-card');
            const title = button(recipe.title, 'ai-recipe-title', () =>
                openRecipe(recipe.id, 'detail', title)
            );
            const detail = button('상세보기', '', () => openRecipe(recipe.id, 'detail', detail));
            const cook = button('요리 시작', 'ai-cook-button', () => openRecipe(recipe.id, 'cook', cook));
            const actions = el('div', 'ai-recipe-actions');
            actions.append(detail, cook);
            row.append(
                photo(recipe),
                el('span', 'ai-recipe-source', '식품안전나라'),
                title,
                el('p', 'ai-recipe-description', recipe.description),
                el('div', 'ai-recipe-facts')
            );
            if (recipe.compared) {
                const match = el('div', 'ai-recipe-match');
                match.append(
                    el('span', 'ai-match-number', '일치 재료 ' + recipe.have.length + '개'),
                    el('span', '', recipe.have.join(' · ')),
                    el(
                        'span',
                        'ai-missing-state',
                        recipe.missing.length
                            ? '추가 재료 ' + recipe.missing.length + '개'
                            : '비교한 재료 모두 있음'
                    ),
                    el('span', '', recipe.missing.join(' · '))
                );
                row.append(match);
            }
            row.append(actions);
            list.append(row);
        });
        shown = Math.min(shown + 4, recipes.length);
        $('ai-result-total').textContent = '레시피 ' + recipes.length + '개';
        $('ai-result-count').textContent = shown + ' / ' + recipes.length + '개 표시';
        more.hidden = shown >= recipes.length;
        if (!recipes.length)
            list.append(el('p', 'ai-empty-state', '검색 결과가 없습니다. 검색어나 재료 상태를 바꿔보세요.'));
    }
    // 레시피 사진 : 이미지와 대체 문구 표시
    function photo(recipe) {
        const figure = el('figure', 'ai-recipe-photo');
        const src = imageUrl(recipe.image);
        if (!src) figure.append(el('div', 'ai-photo-fallback', '사진 미제공'));
        else {
            const image = el('img');
            image.src = src;
            image.alt = recipe.title;
            image.loading = 'lazy';
            image.onerror = () =>
                image.replaceWith(el('div', 'ai-photo-fallback', '사진을 불러오지 못했어요.'));
            figure.append(image);
        }
        return figure;
    }

    // 상세 조회 : 한 번 받은 레시피는 다시 요청하지 않음
    const detailCache = new Map();
    let detailVersion = 0;
    let detailRequest;
    async function openRecipe(id, view, opener) {
        const version = ++detailVersion;
        detailRequest?.abort();
        detailRequest = new AbortController();
        originFocus = opener;
        closing = false;
        modal.replaceChildren();
        const close = closeIcon();
        close.addEventListener('click', closeModal);
        const loadingTitle = el('h2', '', '레시피를 불러오는 중입니다.');
        loadingTitle.id = 'ai-modal-title';
        modal.append(close, loadingTitle);
        if (!modal.open) modal.showModal();
        try {
            if (!detailCache.has(id)) {
                const data = await requestJson(
                    '/recipes/' + encodeURIComponent(id),
                    null,
                    detailRequest.signal
                );
                if (!data.recipe || typeof data.recipe !== 'object')
                    throw new Error('레시피 정보를 찾을 수 없습니다.');
                detailCache.set(id, normalizeRecipe(data.recipe));
            }
            if (version !== detailVersion || !modal.open || closing) return;
            currentRecipe = detailCache.get(id);
            currentView = view === 'cook' && !currentRecipe.steps.length ? 'detail' : view;
            step = 0;
            completed = new Set();
            prepared = new Set();
            renderModal();
        } catch (error) {
            if (error.name === 'AbortError' || version !== detailVersion || closing || !modal.open) return;
            loadingTitle.textContent = error.message;
        }
    }

    // 검색창 : 자연어 입력과 냉장고 재료 검색 연결
    let composing = false;
    $('ai-island-form').addEventListener('submit', (event) => {
        event.preventDefault();
        if (!composing) runSearch();
    });
    input.addEventListener('input', stopPlaceholder);
    input.addEventListener('compositionstart', () => {
        composing = true;
        stopPlaceholder();
    });
    input.addEventListener('compositionend', () => {
        composing = false;
        stopPlaceholder();
    });
    document.addEventListener('ai-fridge-search', (event) => {
        if (Array.isArray(event.detail) && event.detail.length) runSearch(event.detail);
    });
    more.addEventListener('click', appendResults);
    $('ai-availability').addEventListener('change', renderResults);

    // 기존 예시 UI : 자동완성 / 예시 버튼 / 출처 필터 제거
    options.hidden = true;
    picked.hidden = true;
    ['role', 'aria-autocomplete', 'aria-activedescendant', 'aria-expanded', 'aria-controls'].forEach((attr) =>
        input.removeAttribute(attr)
    );
    input.maxLength = 200;
    $('ai-input-help').textContent = '재료 이름이나 원하는 요리를 문장으로 입력해 주세요.';
    island.querySelector('.ai-field-label').textContent = '어떤 요리를 찾고 있나요?';
    island.querySelector('.ai-scope-line > span').textContent = '식품안전나라 레시피 검색';
    island.querySelector('.ai-island-copy small').textContent = '원하는 요리를 찾아보세요.';
    island.querySelector('.ai-sort-line > span:last-child').textContent = '검색 유사도순';
    island.querySelector('.ai-match-note').textContent = '재료 비교는 냉장고 재료로 검색할 때 표시합니다.';
    $('ai-edit-ingredients').textContent = '다시 검색';
    const submit = $('ai-recipe-submit');
    submit.setAttribute('aria-label', '레시피 검색');
    submit.title = '레시피 검색';
    submit.dataset.mode = 'search';
    submit.innerHTML =
        '<svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></svg>';

    // 레시피 대화상자 : 상세보기 / 단계별 조리
    const modal = el('dialog', 'ai-work-dialog recipe-room');
    modal.id = 'ai-recipe-detail';
    modal.setAttribute('aria-labelledby', 'ai-modal-title');
    document.body.append(modal);
    let currentRecipe;
    let currentView = 'detail';
    let step = 0;
    let completed = new Set();
    let prepared = new Set();
    let originFocus;
    let closing = false;
    let direction = 1;
    let enlarged = false;

    // 대화상자 : 닫기와 포커스 복귀
    function closeModal() {
        if (closing || !modal.open) return;
        if (enlarged) {
            toggleImage(false);
            return;
        }
        closing = true;
        ++detailVersion;
        detailRequest?.abort();
        const finish = () => {
            if (!closing) return;
            modal.close();
            closing = false;
            originFocus?.focus({ preventScroll: true });
        };
        if (reduced()) finish();
        else {
            const motion = modal.animate(
                [
                    { opacity: 1, transform: 'translateY(0) scale(1)' },
                    { opacity: 0, transform: 'translateY(12px) scale(.985)' }
                ],
                { duration: 190, easing: 'ease-in' }
            );
            motion.onfinish = finish;
            setTimeout(finish, 230);
        }
    }
    modal.addEventListener('cancel', (event) => {
        event.preventDefault();
        closeModal();
    });
    modal.addEventListener('click', (event) => {
        if (event.target !== modal) return;
        const rect = modal.getBoundingClientRect();
        if (
            event.clientX < rect.left ||
            event.clientX > rect.right ||
            event.clientY < rect.top ||
            event.clientY > rect.bottom
        )
            closeModal();
    });
    modal.addEventListener('keydown', (event) => {
        if (currentView !== 'cook' || enlarged || closing || event.altKey || event.ctrlKey || event.metaKey)
            return;
        if (event.target.matches('input,textarea,select')) return;
        if (event.key === 'ArrowRight' && step < currentRecipe.steps.length - 1) {
            event.preventDefault();
            nextStep();
        } else if (event.key === 'ArrowLeft' && step > 0) {
            event.preventDefault();
            moveStep(step - 1);
        }
    });

    // 화면 전환 : 내용만 바꾸고 짧게 등장
    function renderModal() {
        modal.dataset.view = currentView;
        enlarged = false;
        modal.classList.remove('rr-enlarged');
        modal.replaceChildren();
        const heading = el('div', 'ai-modal-heading rr-heading');
        const identity = el('div', 'rr-identity');
        identity.append(
            el('span', 'rr-wordmark', '잇다.'),
            el('span', 'rr-header-label', currentView === 'cook' ? '지금, 요리하는 시간' : '오늘의 레시피')
        );
        const close = closeIcon();
        close.addEventListener('click', closeModal);
        heading.append(identity, close);
        const content = el('div', 'ai-modal-body rr-body');
        modal.append(heading, content);
        if (currentView === 'detail') renderDetail(content);
        else if (currentView === 'cook') renderCook(content);
        else renderDone(content);
        modal.scrollTop = 0;
        if (modal.open) {
            const focus = modal.querySelector(currentView === 'cook' ? '.rr-step-copy' : '#ai-modal-title');
            focus?.focus({ preventScroll: true });
        }
        if (!reduced()) {
            content.animate(
                [
                    {
                        opacity: 0,
                        transform: `translateX(${currentView === 'cook' ? direction * 18 : 0}px) translateY(6px)`
                    },
                    { opacity: 1, transform: 'translateX(0) translateY(0)' }
                ],
                { duration: 300, easing: 'cubic-bezier(.2,.75,.25,1)' }
            );
        }
    }
    // 레시피 대화상자 : 상세 / 조리 화면 선택
    function viewMode(view) {
        currentView = view;
        direction = 1;
        renderModal();
    }
    // 레시피 섹션 : 번호와 제목 표시
    function sectionHeading(number, title, note) {
        const header = el('div', 'rr-section-heading');
        header.append(el('span', 'rr-section-number', number), el('h3', '', title));
        if (note) header.append(el('span', 'rr-section-note', note));
        return header;
    }

    // 재료 목록 : 원문을 유지하고 쉼표 / 줄바꿈만 구분
    function ingredientLines(amounts) {
        const text = amounts.join('\n');
        const lines = [];
        let buffer = '';
        let depth = 0;
        for (const letter of text) {
            if ('(（'.includes(letter)) depth++;
            if (')）'.includes(letter)) depth = Math.max(0, depth - 1);
            if ((letter === ',' || letter === '\n' || letter === '[') && depth === 0) {
                if (buffer.trim()) lines.push(buffer.trim());
                buffer = letter === '[' ? '[' : '';
            } else {
                buffer += letter;
                if (letter === ']' && depth === 0) {
                    lines.push(buffer.trim());
                    buffer = '';
                }
            }
        }
        if (buffer.trim()) lines.push(buffer.trim());
        return lines;
    }

    // 상세보기 : 대표 사진 / 준비 재료 / 조리 순서
    function renderDetail(content) {
        const recipe = currentRecipe;
        const hero = el('section', 'rr-hero');
        const info = el('div', 'rr-hero-copy');
        const title = el('h2', '', recipe.title);
        title.id = 'ai-modal-title';
        title.tabIndex = -1;
        info.append(
            el('p', 'rr-overline', 'RECIPE NOTE'),
            title,
            el('p', 'rr-category', recipe.description),
            el('p', 'rr-description', '재료를 준비하고, 순서대로 함께 만들어보세요.')
        );
        if (recipe.steps.length)
            info.append(button('요리 시작  ↗', 'ai-modal-primary rr-start', () => viewMode('cook')));
        info.append(el('small', 'rr-credit', '레시피 · 사진 출처  식품안전나라'));
        hero.append(photo(recipe), info);
        content.append(hero);
        const sections = el('div', 'rr-detail-columns');
        const prep = el('section', 'rr-prep');
        prep.append(sectionHeading('01', '재료 준비', '준비한 재료는 눌러서 체크'));
        const count = el('p', 'rr-prep-count');
        const ingredients = ingredientLines(recipe.amounts);
        const actual = ingredients.filter((line) => !/^\[.*\]$/.test(line));
        const updateCount = () =>
            (count.textContent = `${actual.filter((_, i) => prepared.has(i)).length} / ${actual.length} 준비`);
        updateCount();
        prep.append(count);
        const amounts = el('ul', 'rr-ingredients');
        let itemIndex = 0;
        ingredients.forEach((line) => {
            if (/^\[.*\]$/.test(line)) {
                amounts.append(el('li', 'rr-ingredient-group', line.slice(1, -1)));
                return;
            }
            const index = itemIndex++;
            const row = el('li');
            const check = button('', 'rr-ingredient');
            check.append(el('span', 'rr-check'), el('span', 'rr-ingredient-text', line));
            check.setAttribute('aria-pressed', String(prepared.has(index)));
            check.addEventListener('click', () => {
                if (prepared.has(index)) prepared.delete(index);
                else prepared.add(index);
                check.setAttribute('aria-pressed', String(prepared.has(index)));
                updateCount();
            });
            row.append(check);
            amounts.append(row);
        });
        if (!ingredients.length) prep.append(el('p', 'rr-muted', '등록된 재료 정보가 없습니다.'));
        prep.append(amounts);
        const process = el('section', 'rr-process');
        process.append(
            sectionHeading('02', '이렇게 만들어요', recipe.steps.length ? recipe.steps.length + '단계' : '')
        );
        const steps = el('ol', 'rr-method');
        recipe.steps.forEach((text, index) => {
            const row = el('li');
            const jump = button('', 'rr-method-link', () => {
                step = index;
                viewMode('cook');
            });
            jump.append(
                el('span', 'rr-method-index', String(index + 1).padStart(2, '0')),
                el('span', '', text),
                el('span', 'rr-method-arrow', '↗')
            );
            jump.setAttribute('aria-label', `${index + 1}단계 조리 화면 보기: ${text}`);
            row.append(jump);
            steps.append(row);
        });
        process.append(steps);
        if (!recipe.steps.length)
            process.append(el('p', 'rr-muted', '제공된 조리 단계가 없어 요리 시작을 사용할 수 없습니다.'));
        sections.append(prep, process);
        content.append(sections);
    }

    // 조리 화면 : 사진 확대와 단계 이동
    function toggleImage(value) {
        enlarged = value;
        modal.classList.toggle('rr-enlarged', value);
        const zoom = modal.querySelector('.rr-step-photo');
        zoom?.setAttribute('aria-pressed', String(value));
        zoom?.setAttribute('aria-label', value ? '사진 축소' : '조리 사진 확대');
    }
    function moveStep(index) {
        if (index < 0 || index >= currentRecipe.steps.length) return;
        direction = index >= step ? 1 : -1;
        step = index;
        renderModal();
    }
    function nextStep() {
        completed.add(step);
        if (step < currentRecipe.steps.length - 1) moveStep(step + 1);
        else viewMode('done');
    }
    function renderCook(content) {
        const recipe = currentRecipe;
        if (!recipe.steps.length) {
            viewMode('detail');
            return;
        }
        const header = el('div', 'rr-cook-header');
        const title = el('h2', '', recipe.title);
        title.id = 'ai-modal-title';
        header.append(
            button('← 레시피 전체 보기', 'rr-back', () => viewMode('detail')),
            title,
            el(
                'span',
                'rr-page-counter',
                `${String(step + 1).padStart(2, '0')} / ${String(recipe.steps.length).padStart(2, '0')}`
            )
        );
        const progress = el('div', 'rr-progress');
        progress.setAttribute('aria-label', '현재 ' + (step + 1) + '단계');
        const fill = el('span');
        fill.style.width = ((step + 1) / recipe.steps.length) * 100 + '%';
        progress.append(fill);
        const stage = el('section', 'rr-stage');
        const src = imageUrl(recipe.stepImages[step]);
        if (src) {
            const zoom = button('', 'rr-step-photo', () => toggleImage(!enlarged));
            zoom.setAttribute('aria-label', '조리 사진 확대');
            zoom.setAttribute('aria-pressed', 'false');
            const image = el('img');
            image.src = src;
            image.alt = step + 1 + '단계 조리 사진';
            image.onerror = () => {
                zoom.replaceWith(el('div', 'rr-step-no-image', '조리 사진을 불러오지 못했어요.'));
                toggleImage(false);
            };
            zoom.append(image, el('span', 'rr-zoom-caption', '사진 크게 보기  ⤢'));
            stage.append(zoom);
        } else {
            stage.classList.add('rr-text-only');
        }
        const copy = el('div', 'rr-step-copy');
        copy.tabIndex = -1;
        const instruction = el('p', 'rr-instruction', recipe.steps[step]);
        copy.append(el('span', 'rr-step-label', 'STEP ' + String(step + 1).padStart(2, '0')), instruction);
        if (step < recipe.steps.length - 1) {
            const upcoming = el('div', 'rr-up-next');
            upcoming.append(el('small', '', '다음 순서'), el('p', '', recipe.steps[step + 1]));
            copy.append(upcoming);
        }
        stage.append(copy);
        let touch;
        stage.addEventListener('pointerdown', (event) => {
            if (event.pointerType === 'touch') touch = { x: event.clientX, y: event.clientY };
        });
        stage.addEventListener('pointercancel', () => {
            touch = null;
        });
        stage.addEventListener('pointerup', (event) => {
            if (!touch || event.pointerType !== 'touch' || enlarged) return;
            const dx = event.clientX - touch.x,
                dy = event.clientY - touch.y;
            touch = null;
            if (Math.abs(dx) < 65 || Math.abs(dx) < Math.abs(dy) * 1.5) return;
            if (dx < 0 && step < recipe.steps.length - 1) nextStep();
            else if (dx > 0) moveStep(step - 1);
        });
        const nav = el('div', 'rr-cook-nav');
        const previous = button('← 이전 단계', 'ai-modal-secondary', () => moveStep(step - 1));
        previous.disabled = step === 0;
        const marker = el('div', 'rr-step-markers');
        recipe.steps.forEach((_, index) => {
            const dot = button(
                '',
                index === step ? 'is-current' : completed.has(index) ? 'is-done' : '',
                () => moveStep(index)
            );
            dot.setAttribute('aria-label', index + 1 + '단계' + (completed.has(index) ? ' 완료' : ''));
            if (index === step) dot.setAttribute('aria-current', 'step');
            marker.append(dot);
        });
        nav.append(
            previous,
            marker,
            button(
                step === recipe.steps.length - 1 ? '요리 완료  ✓' : '완료하고 다음  →',
                'ai-modal-primary',
                nextStep
            )
        );
        content.append(
            header,
            progress,
            stage,
            nav,
            el('p', 'rr-keyboard-help', '좌우 방향키 또는 스와이프로도 이동할 수 있어요.')
        );
    }

    // 요리 완료 : 결과 목록으로 돌아가기
    function renderDone(content) {
        const view = el('section', 'rr-done');
        const title = el('h2', '', '오늘의 한 끼, 완성.');
        title.id = 'ai-modal-title';
        title.tabIndex = -1;
        view.append(
            el('p', 'rr-overline', '잘 먹겠습니다'),
            title,
            photo(currentRecipe),
            el('p', 'rr-done-name', currentRecipe.title),
            button('검색 결과로 돌아가기  →', 'ai-modal-primary', closeModal),
            el('small', 'rr-muted', '완료 기록은 별도로 저장하지 않습니다.')
        );
        content.append(view);
    }

    document.addEventListener('pointerdown', (event) => {
        if (modal.open) return;
        if (!event.target.closest('.ai-input-wrap')) hideOptions();
        if (open && !island.contains(event.target)) setOpen(false);
    });
    island.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !modal.open) {
            event.preventDefault();
            setOpen(false);
        }
    });
    rotatePlaceholder();
})();
