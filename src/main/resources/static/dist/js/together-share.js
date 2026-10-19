const dialog = document.getElementById('shareWriteDialog');

// 글쓰기 모달 열기
function openShareModal() {
    if (dialog) {
        dialog.showModal();
        document.body.style.overflow = 'hidden';
    }
}

// 글쓰기 모달 닫기
function closeShareModal() {
    if (dialog) {
        dialog.close();
        document.body.style.overflow = '';
    }
}

// 모달 바깥 클릭하면 닫기
dialog.addEventListener('click', (e) => {
    const rect = dialog.getBoundingClientRect();
    if (e.clientX < rect.left || e.clientX > rect.right || e.clientY < rect.top || e.clientY > rect.bottom) {
        closeShareModal();
    }
});

// 나눔 재료 번호 정리
function updateIngredientCardNumbers() {
    const container = document.getElementById('ingredientsContainer');
    const cards = container.querySelectorAll('.tg-ingredient-card');
    
    cards.forEach((card, index) => {
        const badge = card.querySelector('.tg-item-badge');
        if (badge) {
            badge.innerHTML = `<i class="fa-solid fa-leaf"></i> 나눔 재료 ${index + 1}`;
        }

        // 재료 입력 순서 다시 맞추기
        card.querySelectorAll('[name]').forEach(input => {
            const nameAttr = input.getAttribute('name');
            if (nameAttr) {
                const newName = nameAttr.replace(/items\[\d+\]/, `items[${index}]`);
                input.setAttribute('name', newName);
            }
        });

        // 재료가 하나면 삭제 버튼 숨김
        const removeBtn = card.querySelector('.tg-btn-remove-item');
        if (removeBtn) {
            removeBtn.hidden = cards.length === 1;
        }
    });
}

// 나눔 재료 추가
function addIngredientItem() {
    const container = document.getElementById('ingredientsContainer');
    const firstCard = container.querySelector('.tg-ingredient-card');
    if (!firstCard) return;

    const newCard = firstCard.cloneNode(true);

    // 추가한 재료 입력값 비우기
    newCard.querySelectorAll('input, select').forEach(input => {
        if (input.tagName === 'SELECT') {
            input.selectedIndex = 0;
        } else {
            input.value = '';
        }
    });

    newCard.querySelector('[data-search-results]').replaceChildren();
    newCard.querySelector('[data-search-results]').hidden = true;
    newCard.querySelector('[data-search-status]').textContent = '재료명이나 별칭으로 검색한 뒤 결과를 선택해 주세요.';
    container.appendChild(newCard);
    updateIngredientCardNumbers();
}

// 나눔 재료 삭제
function removeIngredientItem(button) {
    const container = document.getElementById('ingredientsContainer');
    if (container.children.length > 1) {
        button.closest('.tg-ingredient-card').remove();
        updateIngredientCardNumbers();
    }
}

// 입력칸마다 검색 대기와 요청 따로 관리
const searchStates = new WeakMap();
const ingredientContainer = document.getElementById('ingredientsContainer');

ingredientContainer.addEventListener('input', (event) => {
    const input = event.target.closest('[data-ingredient-search]');
    if (!input) return;
    const card = input.closest('[data-share-item]');
    const results = card.querySelector('[data-search-results]');
    const status = card.querySelector('[data-search-status]');
    const previous = searchStates.get(input);
    if (previous) {
        clearTimeout(previous.timer);
        previous.controller.abort();
    }
    const state = { controller: new AbortController(), timer: null };
    searchStates.set(input, state);
    card.querySelector('[name$=".ingredient_id"]').value = '';
    card.querySelector('[name$=".unit"]').value = '';
    input.setCustomValidity('검색 결과에서 재료를 선택해 주세요.');
    results.replaceChildren();
    results.hidden = true;
    const keyword = input.value.trim();
    if (!keyword) {
        status.textContent = '재료명이나 별칭으로 검색한 뒤 결과를 선택해 주세요.';
        return;
    }
    status.textContent = '재료를 찾고 있어요…';
    state.timer = setTimeout(async () => {
        try {
            const url = new URL(input.dataset.searchUrl, location.origin);
            url.searchParams.set('keyword', keyword);
            const response = await fetch(url, {
                signal: state.controller.signal,
                headers: { Accept: 'application/json' }
            });
            if (!response.ok) throw new Error('검색 요청 실패');
            const ingredients = await response.json();
            if (!Array.isArray(ingredients)) throw new Error('검색 응답 오류');
            if (searchStates.get(input) !== state || !card.isConnected) return;
            results.replaceChildren();
            for (const ingredient of ingredients) {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'tg-search-option';
                const name = document.createElement('span');
                name.textContent = ingredient.ingredient_name;
                const unit = document.createElement('small');
                unit.textContent = ingredient.unit ? `기본 단위 · ${ingredient.unit}` : '단위 직접 입력';
                button.append(name, unit);
                button.addEventListener('click', () => {
                    card.querySelector('[name$=".ingredient_id"]').value = ingredient.ingredient_id;
                    card.querySelector('[name$=".unit"]').value = ingredient.unit || '';
                    input.value = ingredient.ingredient_name;
                    input.setCustomValidity('');
                    results.replaceChildren();
                    results.hidden = true;
                    status.textContent = `${ingredient.ingredient_name} 선택됨 · 수량과 단위를 확인해 주세요.`;
                });
                results.append(button);
            }
            results.hidden = ingredients.length === 0;
            status.textContent = ingredients.length
                ? `${ingredients.length}개의 재료를 찾았어요. 사용할 재료를 선택해 주세요.`
                : '검색된 재료가 없어요. 다른 이름으로 검색해 주세요. 직접 등록은 준비 중이에요.';
        } catch (error) {
            if (error.name === 'AbortError' || searchStates.get(input) !== state) return;
            status.textContent = '검색하지 못했어요. 잠시 후 다시 입력해 주세요.';
        }
    }, 250);
});

// 이름만 적고 등록하지 않게 선택 여부 확인
dialog.querySelector('[data-write-form]').addEventListener('submit', (event) => {
    for (const card of ingredientContainer.querySelectorAll('[data-share-item]')) {
        if (!card.querySelector('[name$=".ingredient_id"]').value) {
            event.preventDefault();
            const input = card.querySelector('[data-ingredient-search]');
            input.setCustomValidity('검색 결과에서 재료를 선택해 주세요.');
            input.reportValidity();
            return;
        }
    }
});

// ESC로 닫아도 스크롤 다시 사용
dialog.addEventListener('close', () => {
    document.body.style.overflow = '';
});

// 처음에는 목록을 보여주고 글쓰기 버튼으로 열기
updateIngredientCardNumbers();

