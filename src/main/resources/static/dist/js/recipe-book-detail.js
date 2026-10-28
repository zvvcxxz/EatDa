// 상세 조회 : AI를 호출하지 않는 레시피북 전용 주소
const bookApi = document.getElementById('recipe-library')?.dataset.api || '/home/recipe-book';
const reduced = () => matchMedia('(prefers-reduced-motion: reduce)').matches;
async function requestJson(url, payload, signal) {
    const response = await fetch(url, { headers: { Accept: 'application/json' }, signal });
    if (response.redirected || response.status === 401)
        throw new Error('로그인 후 레시피 상세를 확인해 주세요.');
    if (!response.ok)
        throw new Error(
            response.status === 404 ? '레시피를 찾을 수 없습니다.' : '상세 정보를 불러오지 못했습니다.'
        );
    if (!response.headers.get('content-type')?.includes('application/json'))
        throw new Error('서버 응답 형식을 확인해 주세요.');
    return response.json();
}
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
        description: [value('rcp_pat2', 'rcpPat2'), value('rcp_way2', 'rcpWay2')].filter(Boolean).join(' · '),
        amounts: [value('rcp_parts_dtls', 'rcpPartsDtls')].filter(Boolean),
        have: Array.isArray(raw.haveIngredients) ? raw.haveIngredients : [],
        missing: Array.isArray(raw.missingIngredients) ? raw.missingIngredients : [],
        compared: Array.isArray(raw.haveIngredients) && Array.isArray(raw.missingIngredients),
        steps,
        stepImages
    };
}

function imageUrl(value) {
    if (!value) return '';
    try {
        const url = new URL(value, location.origin);
        return ['http:', 'https:'].includes(url.protocol) ? url.href : '';
    } catch {
        return '';
    }
}

function photo(recipe) {
    const figure = el('figure', 'ai-recipe-photo');
    const src = imageUrl(recipe.image);
    if (!src) figure.append(el('div', 'ai-photo-fallback', '사진 미제공'));
    else {
        const image = el('img');
        image.src = src;
        image.alt = recipe.title;
        image.loading = 'lazy';
        image.onerror = () => image.replaceWith(el('div', 'ai-photo-fallback', '사진을 불러오지 못했어요.'));
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
                bookApi + '/detail/' + encodeURIComponent(id),
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
        const dot = button('', index === step ? 'is-current' : completed.has(index) ? 'is-done' : '', () =>
            moveStep(index)
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

export { openRecipe as openBookRecipe };
