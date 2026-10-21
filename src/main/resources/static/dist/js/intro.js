// 문장 단위 커튼 등장, 종료와 포커스 복귀
const dialog = document.getElementById('intro-dialog');
const first = document.getElementById('intro-line-one');
const second = document.getElementById('intro-line-two');
const skipButton = document.getElementById('intro-skip');
const reduced = matchMedia('(prefers-reduced-motion: reduce)').matches;
const lines = ['냉장고를 열고,', '한 끼를 잇다.'];
const introSessionKey = 'eatda.intro.seen';

// 같은 탭의 새로고침과 홈 재방문에서는 인트로 생략
function hasSeenIntro() {
    try {
        return sessionStorage.getItem(introSessionKey) === '1';
    } catch {
        return false;
    }
}

function markIntroSeen() {
    try {
        sessionStorage.setItem(introSessionKey, '1');
    } catch {
        // 저장소를 사용할 수 없어도 홈 진입은 막지 않기
    }
}
let closed = false;
let reveal;
let hold;
let safety;
let fade;
let previousOverflow;
let previousFocus;

function cleanup() {
    clearTimeout(reveal);
    clearTimeout(hold);
    clearTimeout(safety);
    clearTimeout(fade);
    document.removeEventListener('keydown', onKey);
    document.documentElement.style.overflow = previousOverflow;
}

function finish() {
    if (dialog.open) dialog.close();
    cleanup();
    const target = previousFocus instanceof HTMLElement && previousFocus !== document.body
        ? previousFocus : document.getElementById('open-search');
    target?.focus({ preventScroll: true });
}

function skip() {
    if (closed) return;
    markIntroSeen();
    closed = true;
    clearTimeout(reveal);
    clearTimeout(hold);
    clearTimeout(safety);
    document.removeEventListener('keydown', onKey);
    dialog.classList.add('intro-leaving');
    // transitionend가 발생하지 않아도 반드시 닫기
    fade = setTimeout(finish, reduced ? 0 : 380);
}

function onKey(event) {
    // Tab은 포커스 이동, 한글 조합과 보조 키는 건너뛰기로 처리하지 않기
    if (event.key === 'Tab' || event.isComposing || event.ctrlKey || event.metaKey
        || event.altKey || ['Shift', 'Control', 'Alt', 'Meta'].includes(event.key)) return;
    event.preventDefault();
    skip();
}

if (dialog && first && second && skipButton && !hasSeenIntro()) {
    previousFocus = document.activeElement;
    previousOverflow = document.documentElement.style.overflow;
    try {
        dialog.showModal();
        // 등장 도중 새로고침해도 다시 재생되지 않도록 즉시 기록
        markIntroSeen();
        document.documentElement.style.overflow = 'hidden';
        skipButton.addEventListener('click', skip);
        document.addEventListener('keydown', onKey);
        dialog.addEventListener('cancel', event => { event.preventDefault(); skip(); });
        dialog.addEventListener('transitionend', event => {
            if (closed && event.target === dialog && event.propertyName === 'opacity') finish();
        });
        dialog.addEventListener('close', () => { closed = true; cleanup(); });
        first.textContent = lines[0];
        second.textContent = lines[1];
        dialog.classList.add('intro-curtain');
        // 폰트 준비를 잠깐 기다리되 로딩 실패로 인트로를 막지 않기
        const ready = document.fonts?.ready || Promise.resolve();
        let started = false;
        function begin() {
            if (closed || started) return;
            started = true;
            clearTimeout(reveal);
            dialog.classList.add('intro-curtain-playing');
            // 0.8초 등장 → 0.15초 간격 → 0.8초 등장 → 0.8초 유지
            hold = setTimeout(skip, reduced ? 800 : 2550);
        }
        ready.then(begin, begin);
        reveal = setTimeout(begin, 500);
        safety = setTimeout(skip, 6500);
    } catch (error) {
        if (dialog.open) dialog.close();
        cleanup();
        console.warn('인트로를 생략하고 홈을 표시합니다.', error);
    }
}
