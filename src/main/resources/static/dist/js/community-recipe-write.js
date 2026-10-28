// communityRecipe-write.html 전용 동작

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('recipeForm');
    const ingredientRows = document.getElementById('ingredientRows');
    const stepRows = document.getElementById('stepRows');

    // 입력칸 이름을 DTO의 List 순서에 맞춤
    function setInputNames() {
        ingredientRows.querySelectorAll('.ds-ingredient-row')
            .forEach(function(row, index) {
                row.querySelectorAll('[data-field]').forEach(function(input) {
                    input.name = 'ingredients[' + index + '].' + input.dataset.field;
                });
            });

        stepRows.querySelectorAll('.ds-step-editor')
            .forEach(function(row, index) {
                row.querySelector('.step-number').textContent = 'STEP ' + (index + 1);

                row.querySelectorAll('[data-field]').forEach(function(input) {
                    input.name = 'steps[' + index + '].' + input.dataset.field;
                });
            });
    }

    function addIngredient() {
        const template = document.getElementById('ingredientTemplate');
        ingredientRows.appendChild(template.content.cloneNode(true));
        setInputNames();
    }

    function addStep() {
        const template = document.getElementById('stepTemplate');
        stepRows.appendChild(template.content.cloneNode(true));
        setInputNames();
    }

    document.getElementById('addIngredient')
        .addEventListener('click', addIngredient);

    document.getElementById('addStep')
        .addEventListener('click', addStep);

    ingredientRows.addEventListener('click', function(event) {
        const button = event.target.closest('[data-remove-ingredient]');
        if (!button) return;

        if (ingredientRows.children.length <= 1) {
            alert('재료는 한 개 이상 입력해 주세요.');
            return;
        }

        button.closest('.ds-ingredient-row').remove();
        setInputNames();
    });

    stepRows.addEventListener('click', function(event) {
        const button = event.target.closest('[data-remove-step]');
        if (!button) return;

        if (stepRows.children.length <= 1) {
            alert('조리 단계는 한 개 이상 입력해 주세요.');
            return;
        }

        button.closest('.ds-step-editor').remove();
        setInputNames();
    });

    // DB에 저장된 재료의 기본 단위 사용
    ingredientRows.addEventListener('change', function(event) {
        const select = event.target.closest('select[data-field="ingredientId"]');
        if (!select) return;

        const row = select.closest('.ds-ingredient-row');
        const option = select.selectedOptions[0];
        row.querySelector('[data-field="unit"]').value =
            option.dataset.unit || '';
    });

    // 선택한 사진은 화면에서 먼저 확인
    function previewPhoto(file, image) {
        if (!file) {
            image.hidden = true;
            image.removeAttribute('src');
            return;
        }

        const url = URL.createObjectURL(file);
        image.onload = function() {
            URL.revokeObjectURL(url);
        };
        image.src = url;
        image.hidden = false;
    }

    document.getElementById('recipePhoto')
        .addEventListener('change', function() {
            previewPhoto(
                this.files[0],
                document.getElementById('recipePhotoPreview')
            );
        });

    stepRows.addEventListener('change', function(event) {
        const input = event.target.closest('input[data-field="selectFile"]');
        if (!input) return;

        const image = input.closest('.ds-step-editor')
            .querySelector('.recipe-step-image');

        previewPhoto(input.files[0], image);
    });

    // 단위에는 한글과 영문만 입력
    function cleanUnit(event) {
        if (event.isComposing) return;
        const input = event.target;
        if (input.matches('input[data-field="unit"]')) {
            input.value = input.value.replace(/[^가-힣a-zA-Z]/g, '');
        }
    }

    ingredientRows.addEventListener('input', cleanUnit);
    ingredientRows.addEventListener('compositionend', cleanUnit);

    const submitButton = form.querySelector('button[type="submit"]');
    const submitLabel = submitButton.textContent;
    let submitting = false;

    form.addEventListener('submit', function(event) {
        // 저장 중에는 다시 제출하지 않음
        if (submitting) {
            event.preventDefault();
            return;
        }

        setInputNames();

        const selectedIds = new Set();
        const selects = ingredientRows.querySelectorAll(
            'select[data-field="ingredientId"]'
        );

        for (const select of selects) {
            if (selectedIds.has(select.value)) {
                event.preventDefault();
                alert('같은 재료는 한 번만 선택해 주세요.');
                select.focus();
                return;
            }
            selectedIds.add(select.value);
        }

        if (event.defaultPrevented) return;

        submitting = true;
        submitButton.disabled = true;
        submitButton.textContent = '저장 중...';
    });

    // 뒤로 가기로 돌아오면 버튼 복구
    window.addEventListener('pageshow', function() {
        submitting = false;
        submitButton.disabled = false;
        submitButton.textContent = submitLabel;
    });

    if (ingredientRows.children.length === 0) addIngredient();
    if (stepRows.children.length === 0) addStep();

    setInputNames();
});
