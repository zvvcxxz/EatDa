document.addEventListener("DOMContentLoaded", () => {
    const selectedMap = new Map();
    const $ = (id) => document.getElementById(id);

    const API = {
        recommend: "/recipes/recommend",                     
        recipeDetail: (id) => `/recipes/${encodeURIComponent(id)}`, 
        searchIngredients: "/api/fridge/ingredients/search",  
        categoryIngredients: "/api/fridge/ingredients/by-category" 
    };

    // 1. 선택한 재료 UI 렌더링 (Template 사용)
    function renderSelected() {
        const listContainer = $("selectedList");
        const inputsContainer = $("selectedInputs");
        const items = [...selectedMap.values()];

        $("selectedCountText").textContent = `${items.length}개`;
        listContainer.innerHTML = "";
        inputsContainer.innerHTML = "";

        if (items.length === 0) {
            listContainer.innerHTML = '<span class="empty-hint">오른쪽에서 재료를 선택해 주세요.</span>';
            updateTileSelections();
            return;
        }

        const template = $("selectedChipTemplate");
        const fragment = document.createDocumentFragment();

        items.forEach(item => {
            const clone = template.content.cloneNode(true);
            
            const emojiEl = clone.querySelector(".chip-emoji");
            if (emojiEl) emojiEl.remove();

            clone.querySelector(".chip-name").textContent = item.name;
            clone.querySelector(".remove-chip").dataset.removeId = item.id;

            fragment.appendChild(clone);

            // Hidden input 추가 (폼 전송용)
            const hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = "ingredients";
            hiddenInput.value = item.name;
            inputsContainer.appendChild(hiddenInput);
        });

        listContainer.appendChild(fragment);
        updateTileSelections();
    }

    // 2. 카테고리별 재료 로드 (13개 전체 카테고리 대응)
    async function loadCategoryIngredients(categoryCode = "ALL") {
        const grid = $("ingredientGrid");
        grid.innerHTML = `<div class="muted">재료를 불러오는 중...</div>`;

        try {
            const url = (!categoryCode || categoryCode === "ALL")
                ? `${API.searchIngredients}?query=`
                : `${API.categoryIngredients}?category=${encodeURIComponent(categoryCode)}`;

            const res = await fetch(url);
            if (!res.ok) throw new Error("재료 목록 조회 실패");
            const data = await res.json();

            $("ingredientResultCount").textContent = `${data.length}개`;

            if (!Array.isArray(data) || data.length === 0) {
                grid.innerHTML = `<div class="muted">해당 카테고리에 등록된 재료가 없습니다.</div>`;
                return;
            }

            grid.innerHTML = "";
            const template = $("ingredientTileTemplate");
            const fragment = document.createDocumentFragment();

            data.forEach(ing => {
                const id = String(ing.ingredientId || ing.id || ing.ingredient_id);
                const name = ing.ingredientName || ing.name || ing.ingredient_name;
                const isSelected = selectedMap.has(id);

                const clone = template.content.cloneNode(true);
                const btn = clone.querySelector(".ingredient-tile-btn");

                if (isSelected) btn.classList.add("selected");
                btn.dataset.id = id;
                btn.dataset.name = name;

                const emojiEl = clone.querySelector(".tile-emoji");
                if (emojiEl) emojiEl.remove();

                clone.querySelector(".tile-name").textContent = name;

                fragment.appendChild(clone);
            });

            grid.appendChild(fragment);
        } catch (err) {
            console.error("카테고리 재료 불러오기 실패:", err);
            grid.innerHTML = `<div class="muted">재료 목록을 불러오지 못했습니다.</div>`;
        }
    }

    function updateTileSelections() {
        document.querySelectorAll(".ingredient-tile-btn").forEach(btn => {
            const id = btn.dataset.id;
            btn.classList.toggle("selected", selectedMap.has(id));
        });
    }

    // 3. 재료 연관 검색어 자동완성
    let debounceTimer;
    $("ingredientSearch").addEventListener("input", (e) => {
        clearTimeout(debounceTimer);
        const query = e.target.value.trim();
        const resultsUl = $("searchResults");

        if (!query) {
            resultsUl.hidden = true;
            return;
        }

        debounceTimer = setTimeout(async () => {
            try {
                const res = await fetch(`${API.searchIngredients}?query=${encodeURIComponent(query)}`);
                const data = await res.json();

                if (!Array.isArray(data) || data.length === 0) {
                    resultsUl.hidden = true;
                    return;
                }

                resultsUl.innerHTML = "";
                data.forEach(item => {
                    const li = document.createElement("li");
                    li.dataset.id = item.ingredientId || item.id;
                    li.dataset.name = item.ingredientName || item.name;

                    const nameSpan = document.createElement("span");
                    nameSpan.textContent = item.ingredientName || item.name;

                    const unitSpan = document.createElement("span");
                    unitSpan.className = "muted";
                    unitSpan.textContent = item.defaultUnit || '';

                    li.appendChild(nameSpan);
                    li.appendChild(unitSpan);
                    resultsUl.appendChild(li);
                });

                resultsUl.hidden = false;
            } catch {
                resultsUl.hidden = true;
            }
        }, 200);
    });

    $("searchResults").addEventListener("click", (e) => {
        const li = e.target.closest("li");
        if (!li) return;

        const id = String(li.dataset.id);
        const name = li.dataset.name;

        selectedMap.set(id, { id, name });
        $("ingredientSearch").value = "";
        $("searchResults").hidden = true;
        renderSelected();
    });

    // 카테고리 탭 클릭 이벤트
    $("categoryTiles").addEventListener("click", (e) => {
        const btn = e.target.closest(".category-tile");
        if (!btn) return;

        document.querySelectorAll(".category-tile").forEach(t => t.classList.remove("active"));
        btn.classList.add("active");
        loadCategoryIngredients(btn.dataset.category);
    });

    // 재료 타일 클릭 (추가/해제)
    $("ingredientGrid").addEventListener("click", (e) => {
        const btn = e.target.closest(".ingredient-tile-btn");
        if (!btn) return;

        const id = String(btn.dataset.id);
        const name = btn.dataset.name;

        if (selectedMap.has(id)) {
            selectedMap.delete(id);
        } else {
            selectedMap.set(id, { id, name });
        }
        renderSelected();
    });

    // 선택 재료 삭제
    $("selectedList").addEventListener("click", (e) => {
        const btn = e.target.closest("[data-remove-id]");
        if (!btn) return;

        selectedMap.delete(String(btn.dataset.removeId));
        renderSelected();
    });

    $("clearAllBtn").addEventListener("click", () => {
        selectedMap.clear();
        renderSelected();
    });

    // 4. 레시피 추천 요청
    $("recommendForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        if (selectedMap.size === 0) {
            alert("재료를 최소 1개 이상 선택해 주세요.");
            return;
        }

        const recipeResults = $("recipeResults");
        recipeResults.innerHTML = `
            <div class="recommend-empty-card">
                <div class="spinner mb-16"></div>
                <p>AI가 선택하신 재료 기반 최적의 레시피를 찾고 있습니다...</p>
            </div>
        `;

        try {
            const ingredientNames = [...selectedMap.values()].map(v => v.name);
            const res = await fetch(API.recommend, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ingredients: ingredientNames })
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || "추천 요청 실패");
            }

            const data = await res.json();
            const recipes = Array.isArray(data) ? data : (data.recipes || []);
            renderRecipeCards(recipes);
        } catch (err) {
            console.error("추천 오류:", err);
            recipeResults.innerHTML = `
                <div class="recommend-empty-card">
                    <div class="empty-icon">⚠️</div>
                    <h3>레시피 추천 실패</h3>
                    <p>${err.message}</p>
                </div>
            `;
        }
    });

    // 레시피 추천 카드 렌더링
    function renderRecipeCards(recipes) {
        const container = $("recipeResults");
        $("resultCountBadge").textContent = `추천 결과 ${recipes.length}개`;
        container.innerHTML = "";

        if (!recipes || recipes.length === 0) {
            container.innerHTML = `
                <div class="recommend-empty-card">
                    <div class="empty-icon">🍳</div>
                    <h3>추천 레시피가 없습니다.</h3>
                    <p>다른 재료를 선택하거나 추가한 후 다시 시도해 보세요.</p>
                </div>
            `;
            return;
        }

        const template = $("recipeCardTemplate");
        const fragment = document.createDocumentFragment();

        recipes.forEach(recipe => {
            const clone = template.content.cloneNode(true);

            const id = recipe.rcp_seq ?? recipe.rcpSeq ?? recipe.recipeId ?? recipe.id ?? "";
            const name = recipe.rcp_nm ?? recipe.rcpNm ?? recipe.title ?? "이름 없는 레시피";
            const image = recipe.att_file_no_main ?? recipe.attFileNoMain ?? recipe.thumbnailUrl ?? "";
            const category = recipe.rcp_pat2 ?? recipe.rcpPat2 ?? recipe.category ?? "";
            const method = recipe.rcp_way2 ?? recipe.rcpWay2 ?? recipe.method ?? "";
            const matchRate = Math.round(recipe.matchRate ?? recipe.match_rate ?? 0);

            const imgEl = clone.querySelector(".recipe-image img");
            imgEl.src = image || '/dist/images/default-recipe.png';
            imgEl.alt = name;
            imgEl.onerror = () => { imgEl.src = 'https://via.placeholder.com/300x200?text=Recipe'; };

            clone.querySelector(".recipe-title").textContent = name;
            clone.querySelector(".recipe-match").textContent = `일치율 ${matchRate}%`;
            clone.querySelector(".recipe-sub-info").textContent = [category, method].filter(Boolean).join(" · ");
            clone.querySelector(".match-rate-fill").style.width = `${matchRate}%`;

            const have = Array.isArray(recipe.haveIngredients ?? recipe.have_ingredients)
                ? (recipe.haveIngredients ?? recipe.have_ingredients).join(", ")
                : (recipe.haveIngredients ?? recipe.have_ingredients ?? "");

            const missing = Array.isArray(recipe.missingIngredients ?? recipe.missing_ingredients)
                ? (recipe.missingIngredients ?? recipe.missing_ingredients).join(", ")
                : (recipe.missingIngredients ?? recipe.missing_ingredients ?? "");

            if (have) {
                const haveBox = clone.querySelector(".match-box");
                haveBox.hidden = false;
                haveBox.querySelector(".have-text").textContent = have;
            }

            if (missing) {
                const missingBox = clone.querySelector(".missing-box");
                missingBox.hidden = false;
                missingBox.querySelector(".missing-text").textContent = missing;
            }

            clone.querySelector(".view-detail-btn").dataset.detailId = id;

            fragment.appendChild(clone);
        });

        container.appendChild(fragment);
    }

    // 모달 제어 및 조리 순서 렌더링
    const modal = $("recipeDetailModal");
    const openModal = () => modal.hidden = false;
    const closeModal = () => modal.hidden = true;

    $("closeModalBtn").addEventListener("click", closeModal);
    $("modalCloseFooterBtn").addEventListener("click", closeModal);

    $("recipeResults").addEventListener("click", async (e) => {
        const btn = e.target.closest("[data-detail-id]");
        if (!btn) return;

        const id = btn.dataset.detailId;
        openModal();

        $("recipeDetailModalBody").innerHTML = `
            <div class="loading-box">
                <div class="spinner"></div>
                <p>조리 정보를 불러오는 중입니다...</p>
            </div>
        `;

        try {
            const res = await fetch(API.recipeDetail(id));
            if (!res.ok) throw new Error("조리 정보 조회 실패");

            const data = await res.json();
            const recipe = data.recipe || data;

            const name = recipe.rcp_nm ?? recipe.rcpNm ?? "레시피 상세";
            const category = recipe.rcp_pat2 ?? recipe.rcpPat2 ?? "";
            const method = recipe.rcp_way2 ?? recipe.rcpWay2 ?? "";
            const parts = recipe.rcp_parts_dtls ?? recipe.rcpPartsDtls ?? "";
            const tip = recipe.rcp_na_tip ?? recipe.rcpNaTip ?? "";

            $("recipeDetailModalTitle").textContent = name;
            $("recipeDetailModalSubtitle").textContent = [category, method].filter(Boolean).join(" · ") || "조리 순서";

            const modalBody = $("recipeDetailModalBody");
            modalBody.innerHTML = "";

            if (parts) {
                const partsDiv = document.createElement("div");
                partsDiv.className = "detail-section";
                partsDiv.innerHTML = `<strong>🥕 재료 정보:</strong><p class="preserve-lines mb-0 mt-16"></p>`;
                partsDiv.querySelector("p").textContent = parts;
                modalBody.appendChild(partsDiv);
            }

            if (tip) {
                const tipDiv = document.createElement("div");
                tipDiv.className = "detail-section";
                tipDiv.innerHTML = `<strong>💡 저감 조리법 TIP:</strong><p class="preserve-lines mb-0 mt-16"></p>`;
                tipDiv.querySelector("p").textContent = tip;
                modalBody.appendChild(tipDiv);
            }

            const stepsWrapper = document.createElement("div");
            stepsWrapper.className = "manual-steps mt-16";
            stepsWrapper.innerHTML = `<h4 class="mb-16">👨‍🍳 조리 순서</h4>`;

            const stepTemplate = $("manualStepTemplate");
            let hasSteps = false;

            for (let i = 1; i <= 20; i++) {
                const suffix = String(i).padStart(2, "0");
                const manual = recipe[`manual${suffix}`] ?? recipe[`manual_${suffix}`] ?? "";
                const img = recipe[`manual_img${suffix}`] ?? recipe[`manualImg${suffix}`] ?? "";
                const cleanedManual = manual.replace(/^\s*\d+\.\s*/, "");

                if (!manual && !img) continue;
                hasSteps = true;

                const clone = stepTemplate.content.cloneNode(true);
                if (img) {
                    const imgEl = clone.querySelector(".step-img");
                    imgEl.src = img;
                    imgEl.alt = `STEP ${i}`;
                    imgEl.hidden = false;
                }

                clone.querySelector(".step-title").textContent = `STEP ${i}`;
                clone.querySelector(".step-text").textContent = cleanedManual;

                stepsWrapper.appendChild(clone);
            }

            if (!hasSteps) {
                const emptyP = document.createElement("p");
                emptyP.className = "muted";
                emptyP.textContent = "등록된 조리 순서가 없습니다.";
                stepsWrapper.appendChild(emptyP);
            }

            modalBody.appendChild(stepsWrapper);

        } catch (err) {
            $("recipeDetailModalBody").innerHTML = `<p class="muted">상세 정보를 불러올 수 없습니다. (${err.message})</p>`;
        }
    });

    // 초기 화면 로딩 시 전체('ALL') 재료 로드
    loadCategoryIngredients("ALL");
});