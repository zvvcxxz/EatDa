const { createApp, ref, computed, onMounted } = Vue;

// 영문 코드 및 한글명 호환 이모지 사전
const CATEGORY_EMOJI = {
    '육류': '🥩', 'MEAT': '🥩',
    '해산물': '🐟', 'SEAFOOD': '🐟',
    '난류': '🥚', 'EGG': '🥚',
    '유제품': '🥛', 'DAIRY': '🥛',
    '곡물·면류': '🌾', 'GRAIN': '🌾',
    '채소': '🥬', 'VEGETABLE': '🥬',
    '과일': '🍎', 'FRUIT': '🍎',
    '버섯': '🍄', 'MUSHROOM': '🍄',
    '콩·견과류': '🫘', 'NUT': '🫘',
    '가공식품': '🥫', 'PROCESSED': '🥫',
    '유지류': '🧈', 'OIL': '🧈',
    '조미료': '🧂', 'SEASONING': '🧂',
    '물·육수': '💧', 'WATER': '💧'
};

const CATEGORY_NAME_MAP = {
    'MEAT': '육류', 'SEAFOOD': '해산물', 'EGG': '난류', 'DAIRY': '유제품',
    'GRAIN': '곡물·면류', 'VEGETABLE': '채소', 'FRUIT': '과일', 'MUSHROOM': '버섯',
    'NUT': '콩·견과류', 'PROCESSED': '가공식품', 'OIL': '유지류', 'SEASONING': '조미료',
    'WATER': '물·육수'
};

const INGREDIENT_EMOJI = {
    '돼지고기': '🥩', '삼겹살': '🥓', '돼지고기 목살': '🥩', '돼지고기 앞다리살': '🥩', '돼지고기 다짐육': '🥩',
    '소고기': '🥩', '소고기 다짐육': '🥩', '소고기 국거리': '🥩', '닭고기': '🍗', '닭가슴살': '🍗', '닭다리': '🍗', '오리고기': '🍗',
    '고등어': '🐟', '갈치': '🐟', '연어': '🐟', '대구': '🐟', '참치살': '🐟', '새우': '🦐', '오징어': '🦑', '낙지': '🐙',
    '문어': '🐙', '바지락': '🐚', '홍합': '🐚', '굴': '🦪', '전복': '🐚', '꽃게': '🦀', '멸치': '🐟', '다시마': '🌿', '미역': '🌿',
    '계란': '🥚', '달걀': '🥚', '메추리알': '🥚', '우유': '🥛', '생크림': '🥛', '플레인 요거트': '🥛', '슬라이스 치즈': '🧀',
    '모차렐라 치즈': '🧀', '파르메산 치즈': '🧀', '크림치즈': '🧀', '쌀': '🍚', '현미': '🌾', '밀가루': '🌾', '파스타 면': '🍝',
    '소면': '🍜', '우동 면': '🍜', '라면': '🍜', '당면': '🍜', '떡국 떡': '🍚', '떡볶이 떡': '🍚', '식빵': '🍞',
    '양파': '🧅', '대파': '🌿', '마늘': '🧄', '생강': '🧄', '감자': '🥔', '고구마': '🍠', '당근': '🥕', '오이': '🥒',
    '애호박': '🥒', '배추': '🥬', '양배추': '🥬', '상추': '🥬', '시금치': '🥬', '브로콜리': '🥦', '파프리카': '🫑',
    '청양고추': '🌶️', '고추': '🌶️', '고춧가루': '🌶️', '고추장': '🌶️', '무': '🥬', '콩나물': '🌱', '가지': '🍆', '옥수수': '🌽', '토마토': '🍅',
    '사과': '🍎', '배': '🍐', '바나나': '🍌', '딸기': '🍓', '블루베리': '🫐', '포도': '🍇', '귤': '🍊', '레몬': '🍋', '수박': '🍉', '아보카도': '🥑',
    '표고버섯': '🍄', '양송이버섯': '🍄', '새송이버섯': '🍄', '팽이버섯': '🍄', '두부': '🫘', '순두부': '🫘', '땅콩': '🥜',
    '배추김치': '🥬', '김치': '🥬', '참치 통조림': '🥫', '스팸': '🥫', '햄': '🥓', '베이컨': '🥓', '소시지': '🌭', '어묵': '🍢',
    '참기름': '🫗', '식용유': '🫗', '올리브유': '🫒', '버터': '🧈', '소금': '🧂', '설탕': '🧂', '후추': '🧂', '간장': '🧂',
    '된장': '🧂', '식초': '🧂', '다진 마늘': '🧄', '꿀': '🍯', '케첩': '🍅', '마요네즈': '🧂', '물': '💧'
};

function getCategoryEmoji(categoryName) {
    if (!categoryName) return '🍽️';
    return CATEGORY_EMOJI[categoryName] || CATEGORY_EMOJI[CATEGORY_NAME_MAP[categoryName]] || '🍽️';
}

function getIngredientEmoji(name, type) {
    if (!name) return '🍽️';
    if (INGREDIENT_EMOJI[name]) return INGREDIENT_EMOJI[name];

    const matchedKey = Object.keys(INGREDIENT_EMOJI).find(key => name.includes(key));
    if (matchedKey) return INGREDIENT_EMOJI[matchedKey];

    return getCategoryEmoji(type);
}

createApp({
    setup() {
        const isModalOpen = ref(false);
        const isEditModalOpen = ref(false);
        
        // 레시피 추천 모달 전용 상태
        const isRecommendModalOpen = ref(false);
        const isLoadingRecommendations = ref(false);
        const recommendedRecipes = ref([]);

        const keyword = ref('');
        const selectedIngredients = ref([]);
        const searchResults = ref([]);
        const selectedCategory = ref(null);
        const categoryIngredients = ref([]);

        const fridgeItems = ref([]);
        const filterKeyword = ref('');
        const filterStatus = ref('');
        const selectedFilterCategory = ref(null);

        // 화면 노출 개수 제한
        const visibleLimit = ref(10);

        const visibleFridgeItems = computed(() => {
            return fridgeItems.value.slice(0, visibleLimit.value);
        });

        const loadMore = () => {
            visibleLimit.value += 10;
        };

        const editForm = ref({
            fridgeItemId: null,
            ingredientName: '',
            quantity: null,
            unit: '',
            expiresOn: '',
            memo: ''
        });

        const categoryList = ref([
            { code: 'MEAT', label: '육류 🥩' },
            { code: 'SEAFOOD', label: '해산물 🐟' },
            { code: 'EGG', label: '난류 🥚' },
            { code: 'DAIRY', label: '유제품 🥛' },
            { code: 'GRAIN', label: '곡물·면류 🌾' },
            { code: 'VEGETABLE', label: '채소 🥬' },
            { code: 'FRUIT', label: '과일 🍎' },
            { code: 'MUSHROOM', label: '버섯 🍄' },
            { code: 'NUT', label: '콩·견과류 🫘' },
            { code: 'PROCESSED', label: '가공식품 🥫' },
            { code: 'OIL', label: '유지류 🧈' },
            { code: 'SEASONING', label: '조미료 🧂' },
            { code: 'WATER', label: '물·육수 💧' }
        ]);

        const loadFridgeItems = () => {
            const params = {};
            if (filterKeyword.value.trim()) params.keyword = filterKeyword.value.trim();
            if (selectedFilterCategory.value) {
                params.categoryId = CATEGORY_NAME_MAP[selectedFilterCategory.value] || selectedFilterCategory.value;
            }

            axios.get('/api/fridge/items', { params })
                .then(res => {
                    let items = res.data;
                    if (filterStatus.value === 'OLD') {
                        items = items.filter(item => item.isOld || item.old);
                    } else if (filterStatus.value === 'EXPIRING') {
                        items = items.filter(item => item.isExpiring || item.expiring);
                    } else if (filterStatus.value === 'EXPIRED') {
                        items = items.filter(item => item.isExpired || item.expired);
                    }

                    fridgeItems.value = items.map(item => {
                        const rawCat = item.categoryName || item.ingredientType || item.category || item.type || item.category_name;
                        const finalCatName = CATEGORY_NAME_MAP[rawCat] || rawCat || '기타';
                        return {
                            ...item,
                            fridgeItemId: item.fridgeItemId || item.id,
                            ingredientName: item.ingredientName || item.name || item.ingredient_name,
                            categoryName: finalCatName
                        };
                    });

                    visibleLimit.value = 10;
                })
                .catch(err => console.error('내 냉장고 재료 조회 실패:', err));
        };

        const applyFilter = () => loadFridgeItems();
        const filterByCategory = (categoryCode) => {
            selectedFilterCategory.value = categoryCode;
            loadFridgeItems();
        };

        // 레시피 가중치 추천 API 호출 함수
        const openRecommendModal = () => {
            isRecommendModalOpen.value = true;
            isLoadingRecommendations.value = true;

            axios.get('/api/fridge/recipes/recommend')
                .then(res => {
                    recommendedRecipes.value = res.data;
                })
                .catch(err => {
                    console.error('레시피 추천 조회 실패:', err);
                    alert('레시피 추천 목록을 불러오지 못했습니다.');
                })
                .finally(() => {
                    isLoadingRecommendations.value = false;
                });
        };

        const closeRecommendModal = () => {
            isRecommendModalOpen.value = false;
        };

        onMounted(() => {
            loadFridgeItems();

            // 주소창에 tab=recommendations 쿼리가 넘어온 경우 자동 추천 모달 팝업
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('tab') === 'recommendations') {
                openRecommendModal();
            }
        });

        const openAddModal = () => { isModalOpen.value = true; };
        const closeModal = () => {
            isModalOpen.value = false;
            searchResults.value = [];
            keyword.value = '';
            selectedIngredients.value = [];
            selectedCategory.value = null;
            categoryIngredients.value = [];
        };
        const closeEditModal = () => { isEditModalOpen.value = false; };

        let debounceTimer = null;
        const onSearchInput = (event) => {
            const val = event ? event.target.value : keyword.value;
            keyword.value = val;

            clearTimeout(debounceTimer);
            if (!val.trim()) {
                searchResults.value = [];
                return;
            }

            debounceTimer = setTimeout(() => {
                axios.get('/api/fridge/ingredients/search', { params: { query: val.trim() } })
                    .then(res => {
                        searchResults.value = res.data.map(item => ({
                            ingredientId: item.ingredientId || item.id || item.ingredient_id,
                            ingredientName: item.ingredientName || item.name || item.ingredient_name,
                            ingredientType: item.ingredientType || item.type || item.categoryName,
                            defaultUnit: item.defaultUnit || item.unit || '개'
                        }));
                    })
                    .catch(err => console.error('검색 실패:', err));
            }, 200);
        };

        const selectIngredient = (ingredient) => {
            const id = ingredient.ingredientId || ingredient.id;
            const name = ingredient.ingredientName || ingredient.name;
            const type = ingredient.ingredientType || ingredient.type || ingredient.categoryName;
            const unit = ingredient.defaultUnit || ingredient.unit || '개';

            if (!isIngredientSelected(id)) {
                selectedIngredients.value.push({
                    ingredientId: id,
                    ingredientName: name,
                    ingredientType: type,
                    quantity: 1,
                    unit: unit,
                    expiresOn: '',
                    memo: ''
                });
            }
            keyword.value = '';
            searchResults.value = [];
        };

        const removeIngredient = (ingredientId) => {
            selectedIngredients.value = selectedIngredients.value.filter(
                item => (item.ingredientId || item.id) !== ingredientId
            );
        };

        const isIngredientSelected = (ingredientId) => {
            return selectedIngredients.value.some(item => (item.ingredientId || item.id) === ingredientId);
        };

        const selectCategory = (categoryCode) => {
            selectedCategory.value = categoryCode;
            const categoryParam = CATEGORY_NAME_MAP[categoryCode] || categoryCode;

            axios.get('/api/fridge/ingredients/by-category', { params: { category: categoryParam } })
                .then(res => {
                    categoryIngredients.value = res.data.map(item => ({
                        ingredientId: item.ingredientId || item.id || item.ingredient_id,
                        ingredientName: item.ingredientName || item.name || item.ingredient_name,
                        ingredientType: item.ingredientType || item.type || item.categoryName,
                        defaultUnit: item.defaultUnit || item.unit || '개'
                    }));
                })
                .catch(err => console.error('카테고리 재료 조회 실패:', err));
        };

        const toggleIngredient = (ingredient) => {
            const id = ingredient.ingredientId || ingredient.id;
            if (isIngredientSelected(id)) {
                removeIngredient(id);
            } else {
                selectIngredient(ingredient);
            }
        };

        const submitIngredients = () => {
            if (selectedIngredients.value.length === 0) return;

            const payload = selectedIngredients.value.map(item => ({
                ingredientId: item.ingredientId || item.id,
                quantity: item.quantity,
                unit: item.unit,
                expiresOn: item.expiresOn || null,
                memo: item.memo || null
            }));

            axios.post('/api/fridge/items', payload)
                .then(() => {
                    alert('냉장고에 성공적으로 등록되었습니다.');
                    closeModal();
                    loadFridgeItems();
                })
                .catch(err => {
                    alert(err.response?.data?.message || '등록 중 오류가 발생했습니다.');
                    console.error(err);
                });
        };

        const openEditModal = (item) => {
            editForm.value = {
                fridgeItemId: item.fridgeItemId || item.id,
                ingredientName: item.ingredientName || item.name,
                quantity: item.quantity,
                unit: item.unit,
                expiresOn: (item.expiresOn && item.expiresOn !== 'null') ? item.expiresOn : '',
                memo: item.memo || ''
            };
            isEditModalOpen.value = true;
        };

        const submitEdit = () => {
            const payload = {
                quantity: editForm.value.quantity,
                unit: editForm.value.unit,
                expiresOn: editForm.value.expiresOn || null,
                memo: editForm.value.memo || null
            };

            axios.put(`/api/fridge/items/${editForm.value.fridgeItemId}`, payload)
                .then(() => {
                    alert('수정되었습니다.');
                    closeEditModal();
                    loadFridgeItems();
                })
                .catch(err => {
                    alert('수정에 실패했습니다.');
                    console.error(err);
                });
        };

        const deleteFridgeItem = (fridgeItemId) => {
            if (!confirm('정말 삭제하시겠습니까?')) return;

            axios.delete(`/api/fridge/items/${fridgeItemId}`)
                .then(() => {
                    alert('삭제되었습니다.');
                    loadFridgeItems();
                })
                .catch(err => {
                    alert('삭제 처리 실패');
                    console.error(err);
                });
        };

        return {
            isModalOpen,
            isEditModalOpen,
            isRecommendModalOpen,
            isLoadingRecommendations,
            recommendedRecipes,
            openRecommendModal,
            closeRecommendModal,
            keyword,
            selectedIngredients,
            searchResults,
            categoryList,
            selectedCategory,
            categoryIngredients,
            fridgeItems,
            visibleFridgeItems,
            visibleLimit,
            loadMore,
            filterKeyword,
            filterStatus,
            selectedFilterCategory,
            editForm,
            openAddModal,
            closeModal,
            closeEditModal,
            onSearchInput,
            selectIngredient,
            removeIngredient,
            isIngredientSelected,
            selectCategory,
            toggleIngredient,
            submitIngredients,
            openEditModal,
            submitEdit,
            deleteFridgeItem,
            applyFilter,
            filterByCategory,
            getCategoryEmoji,
            getIngredientEmoji
        };
    }
}).mount('#fridgeApp');