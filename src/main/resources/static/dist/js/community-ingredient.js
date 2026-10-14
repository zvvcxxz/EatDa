document.addEventListener('DOMContentLoaded', function() {
    const rows = document.getElementById('ingredientRows');
    if (!rows) return;

    // 분류 이모지는 화면에만 표시
    const typeEmoji = {
        '육류': '🥩',
        '해산물': '🐟',
        '난류': '🥚',
        '유제품': '🥛',
        '곡물·면류': '🌾',
        '채소': '🥬',
        '과일': '🍎',
        '버섯': '🍄',
        '콩·견과류': '🫘',
        '가공식품': '🥫',
        '유지류': '🧈',
        '조미료': '🧂',
        '물·육수': '💧'
    };

    function setupRow(row) {
        if (row.dataset.categoryReady === 'true') return;

        const ingredientSelect = row.querySelector(
            'select[data-field="ingredientId"]'
        );
        if (!ingredientSelect) return;

        row.dataset.categoryReady = 'true';

        // 필터링 전에 전체 재료 목록을 보관
        const ingredients = Array.from(ingredientSelect.options)
            .filter(function(option) {
                return option.value !== '';
            })
            .map(function(option) {
                return {
                    id: option.value,
                    name: option.textContent.trim(),
                    type: option.dataset.type?.trim() || '기타',
                    unit: option.dataset.unit || ''
                };
            });

        const selectedId = ingredientSelect.value;
        const selectedIngredient = ingredients.find(function(item) {
            return item.id === selectedId;
        });

        const categorySelect = document.createElement('select');
        categorySelect.className = 'ingredient-category';
        categorySelect.required = true;
        categorySelect.setAttribute('aria-label', '재료 분류');
        categorySelect.add(new Option('분류 선택', ''));

        const types = [...new Set(ingredients.map(function(item) {
            return item.type;
        }))];

        types.forEach(function(type) {
            const emoji = typeEmoji[type] || '🍽️';
            categorySelect.add(new Option(emoji + ' ' + type, type));
        });

        // 분류 선택창은 화면 필터용이라 DTO 이름을 붙이지 않음
        ingredientSelect.before(categorySelect);
        ingredientSelect.setAttribute('aria-label', '재료');

        function showIngredients(type, keepId) {
            ingredientSelect.replaceChildren(
                new Option(type ? '재료 선택' : '분류를 먼저 선택', '')
            );

            ingredients.forEach(function(item) {
                if (item.type !== type) return;

                const option = new Option(item.name, item.id);
                option.dataset.unit = item.unit;
                option.dataset.type = item.type;
                ingredientSelect.add(option);
            });

            ingredientSelect.disabled = !type;
            ingredientSelect.value = keepId || '';
        }

        // 수정 화면에서는 기존 재료에 맞는 분류를 선택
        if (selectedIngredient) {
            categorySelect.value = selectedIngredient.type;
            showIngredients(selectedIngredient.type, selectedId);
        } else {
            showIngredients('', '');
        }

        categorySelect.addEventListener('change', function() {
            showIngredients(this.value, '');

            const unitInput = row.querySelector('[data-field="unit"]');
            if (unitInput) unitInput.value = '';
        });
    }

    function setupRows() {
        rows.querySelectorAll('.ds-ingredient-row').forEach(setupRow);
    }

    setupRows();

    // 재료 추가 버튼으로 생긴 입력칸에도 적용
    const observer = new MutationObserver(function() {
        setupRows();
    });

    observer.observe(rows, { childList: true });
});