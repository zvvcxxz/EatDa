import { openBookRecipe } from './recipe-book-detail.js';

// 레시피북 : 목록 조회 / 필터 / 책장 넘기기
const root = document.getElementById('recipe-library');
if (root) {
    const $ = (id) => document.getElementById(id);
    const book = $('rb-book');
    const spread = $('rb-spread');
    const empty = $('rb-empty');
    const form = $('rb-filters');
    const modes = ['all', 'type', 'method', 'ingredient'];
    const api = root.dataset.api || '/home/recipe-book';
    let mode = modes.includes(root.dataset.mode) ? root.dataset.mode : 'all';
    let state = { items: [], total: 0, page: 1, totalPages: 1, criteria: {} };
    let busy = false;
    let revision = 0;
    let request;
    let flip;
    let layers = [];

    function el(tag, className, text) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        return node;
    }
    function safeImage(value) {
        if (!value) return '';
        try {
            const url = new URL(value, location.origin);
            return ['http:', 'https:'].includes(url.protocol) ? url.href : '';
        } catch {
            return '';
        }
    }
    async function getJson(path, signal) {
        const response = await fetch(path, { headers: { Accept: 'application/json' }, signal });
        if (response.redirected || response.status === 401) throw new Error('로그인 후 다시 이용해 주세요.');
        if (!response.ok) throw new Error('레시피를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
        if (!response.headers.get('content-type')?.includes('application/json'))
            throw new Error('서버 응답 형식을 확인해 주세요.');
        return response.json();
    }

    // 사진 카드 : 클릭하면 실제 상세와 조리 화면 연결
    function card(recipe) {
        const article = el('article', 'rb-card');
        const button = el('button', 'rb-card-open');
        button.type = 'button';
        button.setAttribute('aria-label', recipe.title + ' 상세보기');
        const figure = el('figure', 'rb-photo');
        const url = safeImage(recipe.image);
        if (url) {
            const image = el('img');
            image.src = url;
            image.alt = recipe.title;
            image.loading = 'lazy';
            image.decoding = 'async';
            image.onerror = () => image.replaceWith(el('div', 'rb-photo-missing', '사진 미제공'));
            figure.append(image);
        } else figure.append(el('div', 'rb-photo-missing', '사진 미제공'));
        const overlay = el('span', 'rb-photo-action', '레시피 펼쳐보기');
        overlay.append(el('span', '', '↗'));
        figure.append(overlay);
        button.append(
            figure,
            el('h3', '', recipe.title),
            el('p', '', [recipe.category, recipe.method].filter(Boolean).join(' · '))
        );
        button.addEventListener('click', () => openBookRecipe(String(recipe.id), 'detail', button));
        article.append(button);
        return article;
    }
    function page(recipes, side, number) {
        const sheet = el('section', 'rb-page is-' + side);
        sheet.setAttribute('aria-label', number + '쪽');
        const head = el('div', 'rb-running-head');
        head.append(
            el('span', '', side === 'left' ? 'ITDA · RECIPE COLLECTION' : '오늘의 한 끼를 잇다'),
            el('small', '', '식품안전나라')
        );
        const grid = el('div', 'rb-photo-grid');
        recipes.forEach((recipe) => grid.append(card(recipe)));
        sheet.append(head, grid, el('span', 'rb-page-foot', String(number).padStart(2, '0')));
        return sheet;
    }
    function showSpread(data) {
        spread.replaceChildren(
            page(data.items.slice(0, 4), 'left', data.page * 2 - 1),
            page(data.items.slice(4, 8), 'right', data.page * 2)
        );
        empty.hidden = data.items.length > 0;
        if (!data.items.length)
            empty.textContent = '이 조건에 맞는 레시피가 없어요.\n검색어나 분류를 바꿔 한 번 더 찾아보세요.';
    }

    // 책장 전환 : 종이를 작은 면으로 나누고 휘어진 위치 계산
    function paperCurve(progress, width, count, direction) {
        const points = [];
        const length = width / count;
        const bend = Math.sin(Math.PI * progress) * 1.08;
        let x = 0;
        let z = 0;
        for (let index = 0; index < count; index++) {
            const position = (index + 0.5) / count;
            const angle = -direction * (Math.PI * progress + bend * (position * 2 - 1));
            const dx = direction * Math.cos(angle) * length;
            const dz = -direction * Math.sin(angle) * length;
            points.push({ x: x + dx / 2, z: z + dz / 2, angle });
            x += dx;
            z += dz;
        }
        return points;
    }

    // 종이 애니메이션 : 안쪽은 먼저 들리고 바깥쪽은 곡선을 따라 이동
    function curlPaper(frontPage, backPage, direction, width, height) {
        const count = 24;
        const stripWidth = width / count;
        const moving = el('div', 'rb-curl-sheet');
        moving.inert = true;
        moving.setAttribute('aria-hidden', 'true');
        const strips = [];
        for (let index = 0; index < count; index++) {
            const strip = el('div', 'rb-curl-strip');
            strip.style.width = stripWidth + 0.65 + 'px';
            const shades = [];
            for (const [back, pageNode] of [
                [false, frontPage],
                [true, backPage]
            ]) {
                const face = el('div', 'rb-curl-face' + (back ? ' is-back' : ''));
                const content = el('div', 'rb-curl-content');
                const normal = direction > 0 ? index : count - index - 1;
                const crop = back ? count - normal - 1 : normal;
                content.style.width = width + 'px';
                content.style.height = height + 'px';
                content.style.left = -crop * stripWidth + 'px';
                content.append(pageNode.cloneNode(true));
                const shade = el('span', 'rb-curl-shade');
                face.append(content, shade);
                strip.append(face);
                shades.push(shade);
            }
            moving.append(strip);
            strips.push({ strip, shades });
        }
        const shadow = el('div', 'rb-curl-shadow');
        shadow.setAttribute('aria-hidden', 'true');
        book.append(shadow, moving);
        layers.push(shadow, moving);
        let frame;
        let stopped = false;
        let resolve;
        const finished = new Promise((done) => (resolve = done));
        const paint = (progress) => {
            const curve = paperCurve(progress, width, count, direction);
            curve.forEach((point, index) => {
                const { strip, shades } = strips[index];
                strip.style.transform = `translate3d(${point.x - stripWidth / 2}px,0,${point.z}px) rotateY(${point.angle}rad)`;
                const shade = Math.sin(Math.PI * progress) * (0.06 + 0.19 * (index / count));
                shades.forEach((node) => (node.style.opacity = shade));
            });
            shadow.style.opacity = Math.sin(Math.PI * progress) * 0.32;
            shadow.style.transform = `translateX(${direction * Math.cos(Math.PI * progress) * width * 0.23}px) scaleX(${0.35 + Math.sin(Math.PI * progress) * 0.65})`;
        };
        paint(0);
        const start = performance.now();
        const tick = (now) => {
            if (stopped) return;
            const time = Math.min(1, (now - start) / 1050);
            const progress = (1 - Math.cos(Math.PI * time)) / 2;
            paint(progress);
            if (time < 1) frame = requestAnimationFrame(tick);
            else resolve();
        };
        frame = requestAnimationFrame(tick);
        return {
            finished,
            cancel() {
                stopped = true;
                cancelAnimationFrame(frame);
                resolve();
            }
        };
    }

    function clearFlip() {
        flip?.cancel();
        layers.forEach((layer) => layer.remove());
        layers = [];
    }
    async function turnPage(data, direction) {
        clearFlip();
        const oldPages = [...spread.children].map((node) => node.cloneNode(true));
        showSpread(data);
        if (!direction || !oldPages.length || matchMedia('(prefers-reduced-motion: reduce)').matches) return;
        if (matchMedia('(max-width: 640px)').matches) {
            flip = spread.animate(
                [
                    { opacity: 0.25, transform: `translateX(${direction * 12}px)` },
                    { opacity: 1, transform: 'translateX(0)' }
                ],
                { duration: 280, easing: 'ease-out' }
            );
            await flip.finished.catch(() => {});
            return;
        }
        const next = direction > 0;
        const stationary = el('div', 'rb-stationary ' + (next ? 'is-left' : 'is-right'));
        stationary.append(oldPages[next ? 0 : 1]);
        stationary.inert = true;
        stationary.setAttribute('aria-hidden', 'true');
        book.append(stationary);
        layers = [stationary];
        const width = spread.children[0].getBoundingClientRect().width;
        const height = spread.getBoundingClientRect().height;
        flip = curlPaper(oldPages[next ? 1 : 0], spread.children[next ? 0 : 1], direction, width, height);
        const currentLayers = [...layers];
        await flip.finished.catch(() => {});
        currentLayers.forEach((layer) => layer.remove());
    }

    // 조회 조건 : 요리 이름 검색과 재료 설명 검색 구분
    function criteria() {
        const query = $('rb-query').value.trim();
        return {
            keyword: mode === 'ingredient' ? '' : query,
            ingredient: mode === 'ingredient' ? query : '',
            category: $('rb-category').value,
            method: $('rb-method').value
        };
    }
    function updateControls() {
        $('rb-prev').disabled = busy || state.page <= 1;
        $('rb-next').disabled = busy || state.page >= state.totalPages;
        $('rb-page-number').textContent =
            `${String(state.page).padStart(2, '0')} / ${String(state.totalPages).padStart(2, '0')}`;
        $('rb-range').textContent = state.total
            ? `${(state.page - 1) * 8 + 1}–${Math.min(state.page * 8, state.total)} / ${state.total}`
            : '0개';
        $('rb-summary').textContent = '레시피 ' + state.total.toLocaleString('ko-KR') + '개';
    }
    function rememberPage() {
        const url = new URL(location.href);
        ['mode', 'page', 'keyword', 'ingredient', 'category', 'method'].forEach((key) =>
            url.searchParams.delete(key)
        );
        url.searchParams.set('mode', mode);
        if (state.page > 1) url.searchParams.set('page', state.page);
        Object.entries(state.criteria).forEach(([key, value]) => {
            if (value) url.searchParams.set(key, value);
        });
        history.replaceState(null, '', url);
    }
    async function load(pageNumber = 1, selected = criteria(), direction = 0) {
        const version = ++revision;
        request?.abort();
        clearFlip();
        request = new AbortController();
        busy = true;
        book.setAttribute('aria-busy', 'true');
        updateControls();
        $('rb-status').textContent = '다음 레시피를 준비하고 있어요.';
        try {
            const url = new URL(api + '/items', location.origin);
            url.searchParams.set('page', pageNumber);
            Object.entries(selected).forEach(([key, value]) => {
                if (value) url.searchParams.set(key, value);
            });
            const data = await getJson(url, request.signal);
            if (version !== revision) return;
            if (
                !Array.isArray(data.items) ||
                !Number.isSafeInteger(data.page) ||
                data.page < 1 ||
                !Number.isSafeInteger(data.total) ||
                data.total < 0 ||
                !Number.isSafeInteger(data.totalPages) ||
                data.totalPages < 1
            )
                throw new Error('목록 응답 형식을 확인해 주세요.');
            state = { ...data, criteria: { ...selected } };
            await turnPage(data, direction);
            if (version !== revision) return;
            rememberPage();
            $('rb-status').textContent = data.total
                ? `${data.page}번째 장을 펼쳤어요.`
                : '검색 결과가 없습니다.';
        } catch (error) {
            if (error.name === 'AbortError' || version !== revision) return;
            $('rb-status').textContent = error.message;
            if (!state.items.length) {
                empty.hidden = false;
                empty.textContent = error.message + '\n검색 버튼을 눌러 다시 시도해 주세요.';
            }
        } finally {
            if (version === revision) {
                busy = false;
                book.setAttribute('aria-busy', 'false');
                updateControls();
            }
        }
    }

    // 탐색 메뉴 : 선택한 방식에 맞게 입력 안내와 필터 표시
    function setMode(value) {
        mode = modes.includes(value) ? value : 'all';
        root.querySelectorAll('[data-book-mode]').forEach((button) =>
            button.setAttribute('aria-pressed', String(button.dataset.bookMode === mode))
        );
        $('rb-query').placeholder =
            mode === 'ingredient' ? '예: 김치, 두부처럼 재료 하나를 입력하세요' : '요리 이름을 찾아보세요';
        $('rb-query-label').textContent = mode === 'ingredient' ? '재료 설명 검색' : '요리 이름 검색';
        $('rb-filter-note').textContent =
            mode === 'ingredient' ? '재료 설명에 포함된 단어 검색 · 이름순' : '요리 이름순';
    }
    root.querySelectorAll('[data-book-mode]').forEach((button) =>
        button.addEventListener('click', () => {
            setMode(button.dataset.bookMode);
            form.reset();
            load();
            if (mode === 'type') $('rb-category').focus();
            else if (mode === 'method') $('rb-method').focus();
            else if (mode === 'ingredient') $('rb-query').focus();
        })
    );
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        load();
    });
    for (const id of ['rb-category', 'rb-method']) $(id).addEventListener('change', () => load());
    $('rb-reset').addEventListener('click', () => {
        form.reset();
        load();
    });
    $('rb-prev').addEventListener('click', () => {
        if (!busy) load(state.page - 1, state.criteria, -1);
    });
    $('rb-next').addEventListener('click', () => {
        if (!busy) load(state.page + 1, state.criteria, 1);
    });
    book.addEventListener('keydown', (event) => {
        if (busy || event.target.closest('input,select,textarea')) return;
        if (event.key === 'ArrowRight' && state.page < state.totalPages) {
            event.preventDefault();
            $('rb-next').click();
        }
        if (event.key === 'ArrowLeft' && state.page > 1) {
            event.preventDefault();
            $('rb-prev').click();
        }
    });

    // 최초 표시 : URL 조건 복원 후 DB 필터와 목록 조회
    async function initialize() {
        showSpread(state);
        empty.hidden = false;
        empty.textContent = '첫 페이지를 펼치고 있어요.';
        const params = new URL(location.href).searchParams;
        setMode(params.get('mode') || mode);
        $('rb-query').value = params.get(mode === 'ingredient' ? 'ingredient' : 'keyword') || '';
        try {
            const filters = await getJson(api + '/filters');
            if (!Array.isArray(filters.categories) || !Array.isArray(filters.methods))
                throw new Error('분류 형식을 확인해 주세요.');
            for (const [id, values, key] of [
                ['rb-category', filters.categories, 'category'],
                ['rb-method', filters.methods, 'method']
            ]) {
                values
                    .filter((value) => typeof value === 'string')
                    .forEach((value) => {
                        const option = el('option', '', value);
                        option.value = value;
                        $(id).append(option);
                    });
                $(id).value = params.get(key) || '';
            }
        } catch (error) {
            $('rb-status').textContent = '분류를 불러오지 못했습니다. 전체 목록은 검색할 수 있어요.';
        }
        const initialPage = Number(params.get('page') || 1);
        load(Number.isSafeInteger(initialPage) && initialPage > 0 ? initialPage : 1);
    }
    initialize();
}
