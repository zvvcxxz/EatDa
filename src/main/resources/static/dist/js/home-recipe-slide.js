// 메인슬라이드
(() => {
    const root = document.querySelector('.community-showcase');
    const track = root.querySelector('.slide-track');
    const viewport = root.querySelector('.slide-window');
    const counter = root.querySelector('.slide-counter');
    const play = document.getElementById('slide-play');
    const reduced = matchMedia('(prefers-reduced-motion: reduce)');
    let index = 0, paused = reduced.matches, hover = false, focused = false, visible = false, timer;
    let allItems = [];
	let items = [];

    const perPage = () => innerWidth <= 600 ? 1 : innerWidth <= 1000 ? 2 : 3;
    const maxIndex = () => Math.max(0, items.length - perPage());
    function update() {
        index = Math.min(index, maxIndex());
        const cards = [...track.children];
        const step = (cards[0]?.getBoundingClientRect().width || 0) + 24;
        track.style.transform = `translateX(${-index * step}px)`;
        cards.forEach((card, i) => { card.inert = i < index || i >= index + perPage(); card.setAttribute('aria-hidden', String(card.inert)); });
        counter.textContent = String(index + 1).padStart(2, '0') + ' / ' + String(maxIndex() + 1).padStart(2, '0');
        root.querySelector('.slide-line span').style.width = ((index + 1) / (maxIndex() + 1) * 100) + '%';
        play.textContent = paused ? '재생' : '일시정지';
        play.setAttribute('aria-label', paused ? '슬라이드 자동 재생' : '슬라이드 자동 재생 정지');
    }
    function schedule() {
        clearInterval(timer);
        if (!paused && !hover && !focused && visible && !document.hidden && maxIndex() > 0) timer = setInterval(() => move(1, false), 4500);
    }
    function move(delta, manual = true) {
		if(!items.length) return;
        index = (index + delta + maxIndex() + 1) % (maxIndex() + 1); update();
        if (manual) { root.querySelector('#slide-announcement').textContent = `${index + 1}번째 위치, ${items[index].title}`; schedule(); }
    }
    function render() {
        track.replaceChildren(); index = 0;
        items.forEach(item => {
            const article = document.createElement('article'); article.className = 'slide-card';
            const button = document.createElement('div'); button.className = 'slide-card-content';
            const photo = document.createElement('div'); photo.className = 'slide-photo';
            const image = document.createElement('img'); image.src = item.imageUrl; image.alt = item.title; image.draggable = false; photo.append(image);
            // 레시피 사진 : 상세보기 / 조리하기 버튼
            const actions = document.createElement('div'); actions.className = 'slide-photo-actions';
            [['상세보기', 'detailUrl'], ['조리하기', 'cookUrl']].forEach(([label, key]) => {

				if(!item[key]) return;
                const action = document.createElement('button'); action.type = 'button'; action.className = key === 'cookUrl' ? 'slide-action slide-action-primary' : 'slide-action';
                const icon = document.createElement('span'); icon.className = 'slide-action-icon'; icon.setAttribute('aria-hidden', 'true'); icon.textContent = key === 'cookUrl' ? '🍳' : '📖';
                const text = document.createElement('span'); text.textContent = label; action.append(icon, text); action.setAttribute('aria-label', item.title + ' ' + label);
                action.onclick = () => {
                    location.href = item[key];
                };
                actions.append(action);
            });
            photo.append(actions);
            const type = document.createElement('span'); type.className = 'slide-type'; type.textContent = item.type;
            const title = document.createElement('h3'); title.textContent = item.title;
            const description = document.createElement('p'); description.textContent = item.description;
            button.append(photo, type, title, description);
            article.append(button); track.append(article);
        }); update(); schedule();
    }
    root.querySelector('#slide-prev').onclick = () => move(-1);
    root.querySelector('#slide-next').onclick = () => move(1);
    play.onclick = () => { paused = !paused; update(); schedule(); };
    root.addEventListener('mouseenter', () => { hover = true; schedule(); });
    root.addEventListener('mouseleave', () => { hover = false; schedule(); });
    root.addEventListener('focusin', () => { focused = true; schedule(); });
    root.addEventListener('focusout', () => setTimeout(() => { focused = root.contains(document.activeElement); schedule(); }, 0));
    root.querySelectorAll('[data-slide-type]').forEach(button => button.onclick = () => {
        root.querySelectorAll('[data-slide-type]').forEach(node => node.setAttribute('aria-pressed', String(node === button)));
		items = allItems.filter(item =>
		    button.dataset.slideType === 'all'
		    || item.type === button.dataset.slideType
		);
		render();
    });
    let touchX;
    viewport.addEventListener('touchstart', event => { touchX = event.touches[0].clientX; }, { passive: true });
    viewport.addEventListener('touchend', event => { const dx = event.changedTouches[0].clientX - touchX; if (Math.abs(dx) > 50) move(dx < 0 ? 1 : -1); }, { passive: true });
    new ResizeObserver(() => { update(); schedule(); }).observe(viewport);
    new IntersectionObserver(entries => { visible = entries[0].isIntersecting; schedule(); }, { threshold: .2 }).observe(root);
    document.addEventListener('visibilitychange', schedule);
    reduced.addEventListener('change', () => { paused = reduced.matches; update(); schedule(); });

	// 메인슬라이드 : 서버에서 게시글 조회
	async function loadSlides() {
		track.textContent = "레시피를 불러오는 중입니다.";

		try {
			const recipeResponse = await fetch("/home/recipe-slides", {
				headers : {Accept : "application/json"}
			});

			if(!recipeResponse.ok || recipeResponse.redirected) {
				throw new Error("모두의 레시피 조회 실패");
			}

			const recipes = await recipeResponse.json();

			const cookingResponse = await fetch("/home/cooking-slides", {
				headers : {Accpet : "application/json"}
			});

			if(!cookingResponse.ok || cookingResponse.redirected) {
				throw new Error("따라 만든 요리 조회 실패");
			}

			const reviews = await cookingResponse.json();

			if(!Array.isArray(recipes) || !Array.isArray(reviews)) {
				throw new Error("슬라이드 응답 형식 오류");
			}

			// 모두의 레시피
			const recipeItems = recipes.map(recipe =>({
				title : recipe.title,
				imageUrl : recipe.thumbnailUrl || "/dist/images/noimage.png",
				description : recipe.summary || "",
				type : "모두의 레시피",
				detailUrl : `/community/recipes/${recipe.recipeId}`,
				cookUrl : `/community/recipes/${recipe.recipeId}/mode`
			}));

			// 따라만든 요리
			const cookingItems = reviews.map(review => ({
				title : review.title,
				imageUrl : review.imageUrl || "/dist/images/noimage.png",
				description : review.content || "",
				type : "따라 만든 요리",
				datailUrl : `/community/cooking/${review.reviewId}`,
				cookUrl : review.originalActive ? `/community/recipes/${review.recipeId}/mode` : null
			}));

			// 두 게시판 목록 합치기
			allItems = [...recipeItems, ...cookingItems];

			const selectedType = root.querySelector(
				'[data-slide-type][aria-pressed="true"]'
			)?.dataset.slideType || "all";

			items = allItems.filter(item => selectedType === "all" || item.type === selectedType);

			render();

			if(!items.length) {
				track.textContent = "등록된 게시글이 없습니다.";
			}
		} catch (error) {
			console.error("메인 슬라이드 조회 실패", error);
			track.textContent = "게시글을 불러오지 못했습니다.";
		}
	}
	loadSlides();
})();
