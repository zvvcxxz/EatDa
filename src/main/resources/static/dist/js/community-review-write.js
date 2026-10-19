document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('reviewForm');
    if (!form) return;

    const recipeId = document.getElementById('reviewRecipeId');
    const recipeTitle = document.getElementById('selectedRecipeTitle');
    const recipePhoto = document.getElementById('selectedRecipePhoto');
    const chooseButton = document.getElementById('chooseRecipe');

    const picker = document.getElementById('recipePicker');
    const searchForm = document.getElementById('recipeSearchForm');
    const searchInput = document.getElementById('recipePickerSearch');
    const list = document.getElementById('recipePickerList');

    const previous = document.getElementById('recipePickerPrev');
    const next = document.getElementById('recipePickerNext');
    const pageText = document.getElementById('recipePickerPage');

    const contextPath = form.dataset.contextPath.slice(0, -1);

    let currentPage = 1;
    let loading = false;

    chooseButton.addEventListener('click', function() {
        picker.showModal();
        loadRecipes(1);
    });

    document.getElementById('closeRecipePicker')
        .addEventListener('click', function() {
            picker.close();
        });

    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        loadRecipes(1);
    });

    previous.addEventListener('click', function() {
        loadRecipes(currentPage - 1);
    });

    next.addEventListener('click', function() {
        loadRecipes(currentPage + 1);
    });

    // DB에서 공개 레시피 목록 조회
    async function loadRecipes(page) {
        if (loading) return;

        loading = true;
        previous.disabled = true;
        next.disabled = true;
        pageText.textContent = '';
        list.textContent = '레시피를 불러오는 중입니다.';

        try {
            const data = await fetchRequest(
                form.dataset.recipeUrl,
                'GET',
                {
                    page: page,
                    kwd: searchInput.value.trim()
                }
            );

            if (!data) {
                list.textContent =
                    '목록을 불러오지 못했습니다. 다시 검색해 주세요.';
                return;
            }

            currentPage = data.page;

            pageText.textContent = data.page + ' / ' + data.totalPage;
            previous.disabled = data.page <= 1;
            next.disabled = data.page >= data.totalPage;

            list.textContent = '';

            if (data.recipes.length === 0) {
                list.textContent = '검색된 레시피가 없습니다.';
                return;
            }

            for (const recipe of data.recipes) {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'ds-picker-row';

                if (recipe.thumbnailUrl) {
                    const image = document.createElement('img');

                    image.src = recipe.thumbnailUrl.startsWith('/')
                        ? contextPath + recipe.thumbnailUrl
                        : recipe.thumbnailUrl;

                    image.alt = recipe.title;
                    button.appendChild(image);
                }

                const text = document.createElement('span');
                text.className = 'ds-picker-recipe-text';

                const title = document.createElement('strong');
                title.textContent = recipe.title;

                const author = document.createElement('small');
                author.textContent = recipe.authorName || '회원';

                text.appendChild(title);
                text.appendChild(author);
                button.appendChild(text);

                const select = document.createElement('span');
                select.className = 'ds-picker-select';
                select.textContent = '선택';

                button.appendChild(select);

                // 선택한 원본 번호를 등록 폼에 넣기
                button.addEventListener('click', function() {
                    recipeId.value = recipe.recipeId;
                    recipeTitle.textContent = recipe.title;
                    recipePhoto.textContent = '';

                    if (recipe.thumbnailUrl) {
                        const image = document.createElement('img');

                        image.src = recipe.thumbnailUrl.startsWith('/')
                            ? contextPath + recipe.thumbnailUrl
                            : recipe.thumbnailUrl;

                        image.alt = recipe.title;
                        recipePhoto.appendChild(image);
                    }

                    chooseButton.textContent = '원본 변경';
                    picker.close();
                });

                list.appendChild(button);
            }
        } catch (error) {
            console.log(error);

            list.textContent =
                '목록을 불러오지 못했습니다. 다시 검색해 주세요.';

            previous.disabled = true;
            next.disabled = true;
            pageText.textContent = '';
        } finally {
            loading = false;
        }
    }

    // 업로드 전 사진 미리보기
    const photoInput = document.getElementById('reviewPhoto');
    const photoPreview = document.getElementById('reviewPhotoPreview');
    const photoMessage = document.getElementById('reviewPhotoMessage');

    photoInput.addEventListener('change', function() {
        const file = this.files[0];

        if (!file) {
            photoPreview.hidden = true;
            photoPreview.removeAttribute('src');
            photoMessage.hidden = false;
            return;
        }

        const url = URL.createObjectURL(file);

        photoPreview.onload = function() {
            URL.revokeObjectURL(url);
        };

        photoPreview.src = url;
        photoPreview.hidden = false;
        photoMessage.hidden = true;
    });

    // 원본 선택 여부 확인 후 일반 폼으로 전송
    form.addEventListener('submit', function(event) {
        if (Number(recipeId.value) <= 0) {
            event.preventDefault();
            alert('따라 만든 원본 레시피를 선택하세요.');
            chooseButton.focus();
            return;
        }

        document.getElementById('reviewSubmit').disabled = true;
    });
});