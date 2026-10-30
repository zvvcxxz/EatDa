document.addEventListener('DOMContentLoaded', () => {
    const widget = document.querySelector('.eatda-ai-widget');
    const toggleBtn = document.getElementById('eatda-ai-toggle');
    const menu = document.getElementById('eatda-ai-menu');

    if (!widget || !toggleBtn) return;

    // 1. 메뉴 토글 함수
    const toggleMenu = (open) => {
        const isOpen = open !== undefined ? open : !widget.classList.contains('is-active');
        widget.classList.toggle('is-active', isOpen);
        toggleBtn.setAttribute('aria-expanded', isOpen);
        menu.setAttribute('aria-hidden', !isOpen);
    };

    toggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleMenu();
    });

    // 외부 클릭 시 메뉴 닫기
    document.addEventListener('click', (e) => {
        if (!widget.contains(e.target)) {
            toggleMenu(false);
        }
    });

    // ESC 키 입력 시 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && widget.classList.contains('is-active')) {
            toggleMenu(false);
        }
    });

    // 로그인 체크 헬퍼
    const handleLoginCheck = (button) => {
        const loggedIn = button.getAttribute('data-logged-in') === 'true';
        const loginUrl = button.getAttribute('data-login-url');

        if (!loggedIn && loginUrl) {
            if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
                window.location.href = loginUrl;
            }
            return false;
        }
        return true;
    };

    // 2. [버튼 1] 상담 챗봇
    const openEatdaChatBtn = document.getElementById('open-eatda-chat');
    if (openEatdaChatBtn) {
        openEatdaChatBtn.addEventListener('click', () => {
            if (!handleLoginCheck(openEatdaChatBtn)) return;
            const chatUrl = openEatdaChatBtn.getAttribute('data-chat-url');
            if (chatUrl) {
                window.location.href = chatUrl;
            }
        });
    }

    // 3. [버튼 2] 점심 추천 챗봇 (모달 연동)
    const openLunchChatBtn = document.getElementById('open-lunch-chat');
    const lunchDialog = document.getElementById('lunch-chat-dialog');
    const closeLunchBtn = document.getElementById('close-lunch-chat');
    const lunchFrame = document.getElementById('lunch-chat-frame');

    if (openLunchChatBtn && lunchDialog) {
        openLunchChatBtn.addEventListener('click', () => {
            if (!handleLoginCheck(openLunchChatBtn)) return;

            if (lunchFrame && !lunchFrame.src) {
                const dataSrc = lunchFrame.getAttribute('data-src');
                if (dataSrc) lunchFrame.src = dataSrc;
            }

            if (typeof lunchDialog.showModal === 'function') {
                lunchDialog.showModal();
            } else {
                lunchDialog.setAttribute('open', 'true');
            }
            toggleMenu(false);
        });

        if (closeLunchBtn) {
            closeLunchBtn.addEventListener('click', () => {
                if (typeof lunchDialog.close === 'function') {
                    lunchDialog.close();
                } else {
                    lunchDialog.removeAttribute('open');
                }
            });
        }
    }

    // 4. [버튼 3] 냉장고를 부탁해 (페이지 링크 이동)
    const openFridgeRecipeBtn = document.getElementById('open-fridge-recipe');
    if (openFridgeRecipeBtn) {
        openFridgeRecipeBtn.addEventListener('click', () => {
            if (!handleLoginCheck(openFridgeRecipeBtn)) return;
            
            const recipeAiUrl = openFridgeRecipeBtn.getAttribute('data-recipeai-url');
            if (recipeAiUrl) {
                window.location.href = recipeAiUrl;
            }
        });
    }

    // 5. [버튼 4] AI 괴식 추천
    const openWeirdFoodBtn = document.getElementById('open-weird-food');
    if (openWeirdFoodBtn) {
        openWeirdFoodBtn.addEventListener('click', () => {
            toggleMenu(false);
            alert('🧪 AI 괴식 추천 기능이 곧 찾아옵니다! 민트초코 떡볶이를 기대해 주세요.');
        });
    }
});