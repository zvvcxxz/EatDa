// 재료 나눔 수정 : 재료 검색과 입력값 확인
(() => {
    'use strict';
    const form = document.querySelector('[data-share-edit]');
    if (!form) return;

    const content = form.elements.namedItem('content');
    const count = form.querySelector('[data-content-count]');
    const type = form.elements.namedItem('share_type');
    const exchange = form.elements.namedItem('exchange_note');
    const start = form.elements.namedItem('available_from');
    const end = form.elements.namedItem('available_until');

    // 본문 글자 수
    const updateCount = () => {
        count.textContent = `${content.value.length.toLocaleString()}자`;
    };
    content.addEventListener('input', updateCount);
    updateCount();

    // 교환일 때만 조건 필수
    const updateType = () => {
        exchange.required = type.value === '1';
        exchange.setCustomValidity('');
    };
    type.addEventListener('change', updateType);
    exchange.addEventListener('input', () => exchange.setCustomValidity(''));
    updateType();

    // 종료일은 시작일 이후
    const validateDates = () => {
        end.setCustomValidity('');
        if (start.value && end.value && end.value <= start.value) {
            end.setCustomValidity('나눔 종료일은 시작일보다 이후여야 합니다.');
        }
    };
    start.addEventListener('input', validateDates);
    end.addEventListener('input', validateDates);
    validateDates();

    // 수정 후 다시 입력하면 이전 안내 초기화
    const textFields = ['title', 'content', 'location_text'];
    textFields.forEach((name) => {
        form.elements.namedItem(name).addEventListener('input', (event) => {
            event.target.setCustomValidity('');
        });
    });

    // 저장 전 공백과 날짜 확인
    form.addEventListener('submit', (event) => {
        updateType();
        validateDates();
        textFields.forEach((name) => {
            const field = form.elements.namedItem(name);
            field.setCustomValidity(field.value.trim() ? '' : '내용을 입력해주세요.');
        });
        if (type.value === '1' && !exchange.value.trim()) {
            exchange.setCustomValidity('교환 조건을 입력해주세요.');
        }
        if (!validateMaterials() || !form.reportValidity()) event.preventDefault();
    });

    // 재료 편집
    const items = form.querySelector('[data-edit-items]');
    const itemTemplate = form.querySelector('#se-item-template');
    const feedback = form.querySelector('[data-items-feedback]');
    const states = new WeakMap();
    const field = (row, name) => row.querySelector(`[data-item-field="${name}"]`);

    // 재료 검색 : 이전 검색과 예약 요청 취소

    function stopSearch(input) {
        const state = states.get(input);
        if (state) {
            clearTimeout(state.timer);
            state.controller.abort();
        }
        states.delete(input);
    }

    // 삭제 후 전송 순서 다시 맞추기
    function renameItems() {
        const rows = [...items.querySelectorAll('[data-edit-item]')];
        rows.forEach((row, index) => {
            row.querySelector('[data-item-title]').textContent = `나눔 재료 ${index + 1}`;
            row.querySelectorAll('[data-item-field]').forEach((input) => {
                input.name = `items[${index}].${input.dataset.itemField}`;
            });
            row.querySelector('[data-remove-edit-item]').disabled = rows.length === 1;
        });
        document.querySelector('[data-edit-item-count]').textContent = rows.length;
    }

    // 새 재료는 기존 재료 번호 없이 추가
    function addItem() {
        const fragment = itemTemplate.content.cloneNode(true);
        const row = fragment.querySelector('[data-edit-item]');
        items.append(fragment);
        renameItems();
        feedback.textContent = '새 재료 입력칸을 추가했습니다.';
        row.querySelector('[data-edit-search]').focus();
    }
    form.querySelector('[data-add-edit-item]').addEventListener('click', addItem);

    // 화면에서만 제거, 실제 삭제는 저장할 때 처리
    items.addEventListener('click', (event) => {
        const button = event.target.closest('[data-remove-edit-item]');
        if (!button || button.disabled) return;
        const row = button.closest('[data-edit-item]');
        const name = field(row, 'ingredient_name').value || '재료';
        stopSearch(row.querySelector('[data-edit-search]'));
        row.remove();
        renameItems();
        feedback.textContent = `${name} 항목을 뺐습니다. 저장해야 삭제가 반영됩니다.`;
    });

    // 재료명 변경 시 기존 선택 해제
    function searchIngredient(input, composing = false) {
        stopSearch(input);
        const row = input.closest('[data-edit-item]');
        const results = row.querySelector('[data-edit-results]');
        const status = row.querySelector('[data-item-status]');
        field(row, 'ingredient_id').value = '';
        field(row, 'unit').value = '';
        input.setCustomValidity('검색 결과에서 재료를 선택해 주세요.');
        results.replaceChildren();
        results.hidden = true;
        const keyword = input.value.trim();
        if (!keyword || composing) {
            status.textContent = '재료명을 입력하고 검색 결과에서 선택해 주세요.';
            return;
        }
        status.textContent = '재료를 찾고 있어요…';
        const state = { timer: null, controller: new AbortController() };
        states.set(input, state);
        state.timer = setTimeout(async () => {
            try {
                const url = new URL(items.dataset.searchUrl, location.origin);
                url.searchParams.set('keyword', keyword);
                const response = await fetch(url, {
                    signal: state.controller.signal,
                    headers: { Accept: 'application/json' }
                });
                if (!response.ok) throw new Error('재료 검색 실패');
                const data = await response.json();
                if (!Array.isArray(data)) throw new Error('검색 응답 오류');
                if (states.get(input) !== state || !row.isConnected) return;
                data.forEach((ingredient) => {
                    const option = document.createElement('button');
                    option.type = 'button';
                    option.className = 'se-search-option';
                    const name = document.createElement('span');
                    name.textContent = ingredient.ingredient_name;
                    const unit = document.createElement('small');
                    unit.textContent = ingredient.unit ? `기본 단위 · ${ingredient.unit}` : '단위 직접 입력';
                    option.append(name, unit);
                    option.addEventListener('click', () => {
                        stopSearch(input);
                        field(row, 'ingredient_id').value = ingredient.ingredient_id;
                        field(row, 'unit').value = ingredient.unit || '';
                        input.value = ingredient.ingredient_name;
                        input.setCustomValidity('');
                        results.replaceChildren();
                        results.hidden = true;
                        status.textContent = `${ingredient.ingredient_name} 선택됨 · 수량과 단위를 확인해 주세요.`;
                        input.focus();
                    });
                    results.append(option);
                });
                results.hidden = data.length === 0;
                status.textContent = data.length
                    ? `${data.length}개 검색됨 · 사용할 재료를 선택해 주세요.`
                    : '검색된 재료가 없습니다. 다른 이름으로 검색해 주세요.';
            } catch (error) {
                if (error.name === 'AbortError' || states.get(input) !== state || !row.isConnected) return;
                status.textContent = '검색하지 못했어요. 잠시 후 다시 입력해 주세요.';
            }
        }, 250);
    }
    items.addEventListener('input', (event) => {
        if (event.target.matches('[data-edit-search]')) searchIngredient(event.target, event.isComposing);
    });
    items.addEventListener('compositionend', (event) => {
        if (event.target.matches('[data-edit-search]')) searchIngredient(event.target);
    });
    items.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && event.target.matches('[data-edit-search]')) event.preventDefault();
    });

    // 기존 재료도 번호가 있어야 저장 가능
    function validateMaterials() {
        renameItems();
        const rows = [...items.querySelectorAll('[data-edit-item]')];
        if (!rows.length) {
            feedback.textContent = '재료를 하나 이상 추가해 주세요.';
            return false;
        }
        rows.forEach((row) => {
            const input = row.querySelector('[data-edit-search]');
            input.setCustomValidity(
                field(row, 'ingredient_id').value ? '' : '검색 결과에서 재료를 선택해 주세요.'
            );
        });
        return true;
    }
    if (!items.querySelector('[data-edit-item]')) addItem();
    renameItems();
})();
