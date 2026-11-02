// 괴식 조합기 : 문장 입력과 Gemini 이미지 생성
(() => {
    const dialog = document.querySelector('#weird-food-dialog');
    const openButton = document.querySelector('#open-weird-food');
    if (!dialog || !openButton || dialog.dataset.bound) return;
    dialog.dataset.bound = 'true';
    const $ = selector => dialog.querySelector(selector);
    const input = $('#wfg-prompt');
    const api = dialog.dataset.api || '/home/weird-food';
    let loading = false;
    let judgeRequest = null;
    let imageRequest = null;
    let version = 0;
    let closeTimer = null;
    let retryTimer = null;
    let nextJudgeAt = 0;
    let imageTicket = null;
    let imageUrl = null;
    let imageAttempts = 0;
    let previousOverflow = '';
    let lastFocus = null;

    function message(text = '') { $('[data-wfg-status]').textContent = text; }
    function view(name) {
        for (const section of ['setup', 'loading', 'result']) $(`[data-wfg-${section}]`).hidden = section !== name;
        dialog.scrollTop = 0;
    }
    function headers() {
        const values = { Accept: 'application/json', 'Content-Type': 'application/json' };
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        if (token && header) values[header] = token;
        return values;
    }
    function updateControls() {
        const seconds = Math.max(0, Math.ceil((nextJudgeAt - Date.now()) / 1000));
        const button = $('[data-wfg-submit]');
        button.disabled = loading || input.value.trim().length < 2 || seconds > 0;
        button.textContent = seconds ? `${seconds}초 후 다시 만들기` : '상상 속 요리 만들기 ↗';
        $('[data-wfg-count]').textContent = `${input.value.length} / 300`;
    }
    function cooldown(seconds) {
        nextJudgeAt = Date.now() + seconds * 1000;
        clearInterval(retryTimer);
        updateControls();
        if (!dialog.open || seconds <= 0) return;
        retryTimer = setInterval(() => {
            updateControls();
            if (Date.now() >= nextJudgeAt) clearInterval(retryTimer);
        }, 1000);
    }
    async function readJson(response) {
        if (response.redirected || response.status === 401) throw new Error('로그인 상태를 확인해주세요.');
        if (!response.headers.get('content-type')?.includes('application/json')) {
            throw new Error('서버 응답을 확인하지 못했어요. 잠시 후 다시 시도해주세요.');
        }
        const data = await response.json();
        if (response.status === 429 && data.retryAfter) cooldown(Number(data.retryAfter) || 15);
        if (!response.ok) throw new Error(data.message || '생성에 연결하지 못했어요. 다시 시도해주세요.');
        return data;
    }
    function releaseImage() {
        $('[data-wfg-image]').removeAttribute('src');
        if (imageUrl) URL.revokeObjectURL(imageUrl);
        imageUrl = null;
    }
    function open() {
        clearTimeout(closeTimer);
        dialog.classList.remove('wfg-closing');
        if (dialog.open) return;
        lastFocus = document.activeElement;
        previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        dialog.showModal();
        message(); view('setup');
        cooldown(Math.max(0, (nextJudgeAt - Date.now()) / 1000));
        input.focus({ preventScroll: true });
    }
    function close() {
        if (!dialog.open || dialog.classList.contains('wfg-closing')) return;
        ++version;
        judgeRequest?.abort(); imageRequest?.abort();
        loading = false;
        clearInterval(retryTimer);
        dialog.classList.add('wfg-closing');
        closeTimer = setTimeout(() => dialog.close(), matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 200);
    }

    // 기존 준비 중 안내창 대신 이 모달만 열기
    openButton.addEventListener('click', event => {
        event.preventDefault(); event.stopImmediatePropagation();
        const widget = openButton.closest('.eatda-ai-widget');
        widget?.classList.remove('is-active');
        widget?.querySelector('#eatda-ai-toggle')?.setAttribute('aria-expanded', 'false');
        widget?.querySelector('#eatda-ai-menu')?.setAttribute('aria-hidden', 'true');
        open();
    }, true);
    $('[data-wfg-close]').addEventListener('click', close);
    dialog.addEventListener('cancel', event => { event.preventDefault(); close(); });
    dialog.addEventListener('click', event => {
        if (event.target !== dialog) return;
        const r = dialog.getBoundingClientRect();
        if (event.clientX < r.left || event.clientX > r.right || event.clientY < r.top || event.clientY > r.bottom) close();
    });
    dialog.addEventListener('close', () => {
        ++version; judgeRequest?.abort(); imageRequest?.abort(); loading = false;
        document.body.style.overflow = previousOverflow;
        clearInterval(retryTimer); releaseImage();
        dialog.classList.remove('wfg-closing');
        lastFocus?.focus({ preventScroll: true });
    });
    input.addEventListener('input', () => { message(); updateControls(); });
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            if (!$('[data-wfg-submit]').disabled) $('[data-wfg-setup]').requestSubmit();
        }
    });
    for (const button of dialog.querySelectorAll('[data-wfg-example]')) {
        button.addEventListener('click', () => {
            input.value = button.dataset.wfgExample;
            message(); updateControls(); input.focus({ preventScroll: true });
        });
    }
    $('[data-wfg-again]').addEventListener('click', () => {
        ++version; imageRequest?.abort(); releaseImage();
        imageTicket = null;
        message(); view('setup'); updateControls(); input.focus({ preventScroll: true });
    });
    $('[data-wfg-image-retry]').addEventListener('click', () => {
        if (imageTicket && imageAttempts < 2) createImage(imageTicket, version);
    });

    // 심사 결과를 받은 다음 이미지만 별도로 요청
    async function createImage(ticket, current) {
        imageRequest?.abort();
        const controller = new AbortController();
        imageRequest = controller;
        imageAttempts++;
        releaseImage();
        $('[data-wfg-image]').hidden = true;
        $('[data-wfg-picture-placeholder]').hidden = false;
        $('[data-wfg-image-retry]').hidden = true;
        $('[data-wfg-picture-stage]').setAttribute('aria-busy', 'true');
        $('[data-wfg-image-status]').textContent = 'Gemini가 이 요리의 이미지를 그리고 있어요.';
        const timeout = setTimeout(() => controller.abort(), 70_000);
        try {
            const values = headers();
            values.Accept = 'image/png, image/jpeg, image/webp, application/json';
            const response = await fetch(`${api}/image`, {
                method: 'POST', headers: values, body: JSON.stringify({ ticket }), signal: controller.signal
            });
            if (response.redirected || response.status === 401) throw new Error('로그인 상태를 확인해주세요.');
            if (!response.ok) { await readJson(response); return; }
            const type = response.headers.get('content-type')?.split(';')[0];
            if (!['image/png', 'image/jpeg', 'image/webp'].includes(type)) throw new Error('이미지 응답을 확인하지 못했어요.');
            const blob = await response.blob();
            if (current !== version || !dialog.open) return;
            imageUrl = URL.createObjectURL(blob);
            const img = $('[data-wfg-image]');
            img.alt = `${$('[data-wfg-name]').textContent} — Gemini가 생성한 상상 속 요리 이미지`;
            img.src = imageUrl;
            await img.decode();
            if (current !== version || !dialog.open) return;
            img.hidden = false;
            $('[data-wfg-picture-placeholder]').hidden = true;
        } catch (error) {
            if (current !== version || !dialog.open) return;
            $('[data-wfg-image-status]').textContent = error.name === 'AbortError'
                ? '이미지 생성이 길어지고 있어요. 심사 결과는 아래에서 확인할 수 있어요.' : error.message;
            $('[data-wfg-image-retry]').hidden = imageAttempts >= 2;
        } finally {
            clearTimeout(timeout);
            if (current === version) $('[data-wfg-picture-stage]').setAttribute('aria-busy', 'false');
        }
    }

    $('[data-wfg-setup]').addEventListener('submit', async event => {
        event.preventDefault();
        const prompt = input.value.trim();
        if (loading || Date.now() < nextJudgeAt) return;
        if (prompt.length < 2 || prompt.length > 300) return message('원하는 재료와 조합을 2~300자로 입력해주세요.');
        loading = true; message();
        const current = ++version;
        judgeRequest?.abort(); imageRequest?.abort(); releaseImage();
        const controller = new AbortController();
        judgeRequest = controller;
        $('[data-wfg-loading-prompt]').textContent = prompt;
        view('loading'); cooldown(15);
        const timeout = setTimeout(() => controller.abort(), 40_000);
        try {
            const response = await fetch(`${api}/judge`, {
                method: 'POST', headers: headers(), body: JSON.stringify({ prompt }), signal: controller.signal
            });
            const result = await readJson(response);
            if (current !== version || !dialog.open) return;
            if (!Number.isInteger(result.score) || result.score < 0 || result.score > 100
                    || !['name', 'grade', 'flavor', 'comment', 'imageTicket'].every(key => typeof result[key] === 'string' && result[key])
                    || !Array.isArray(result.ingredients) || !result.ingredients.every(item => typeof item === 'string')) {
                throw new Error('심사 결과를 표시하지 못했어요. 다시 시도해주세요.');
            }
            for (const key of ['name', 'grade', 'flavor', 'comment']) $(`[data-wfg-${key}]`).textContent = result[key];
            $('[data-wfg-result-mix]').textContent = `AI가 파악한 재료 · ${result.ingredients.join(' + ')}`;
            $('[data-wfg-score]').textContent = result.score;
            $('.wfg-meter').setAttribute('aria-valuenow', String(result.score));
            $('[data-wfg-meter]').style.width = '0%';
            imageTicket = result.imageTicket;
            imageAttempts = 0;
            view('result');
            $('[data-wfg-again]').focus({ preventScroll: true });
            requestAnimationFrame(() => {
                if (current === version && dialog.open) $('[data-wfg-meter]').style.width = `${result.score}%`;
            });
            createImage(imageTicket, current);
        } catch (error) {
            if (current !== version || !dialog.open) return;
            view('setup');
            message(error.name === 'AbortError' ? '심사가 길어지고 있어요. 잠시 후 다시 시도해주세요.' : error.message);
        } finally {
            clearTimeout(timeout);
            if (current === version) { loading = false; updateControls(); }
        }
    });
    updateControls();
})();
