// 디자인 확인용 동작. 서버 저장은 실제 컨트롤러 연결 후 구현
const designRoot = document.querySelector('.community-design');
const designView = designRoot.dataset.view;
const designPreview = designRoot.dataset.preview === 'true';
const designId = Number(designRoot.dataset.id || new URLSearchParams(location.search).get('id') || 1);
const designContext = designPreview ? '' : location.pathname.split('/community')[0];
const d = communityDesignData;
const demoRecipe = d.recipes.find(r => r.recipeId === designId) || d.recipes[0];
const previewPages = {
 'recipes':'communityRecipe-list.html','recipe-detail':'communityRecipe-detail.html',
 'recipe-write':'communityRecipe-write.html','recipe-update':'communityRecipe-update.html','recipe-mode':'communityRecipe-mode.html',
 'reviews':'communityReview-list.html','review-detail':'communityReview-detail.html','review-write':'communityReview-write.html','review-update':'communityReview-update.html',
 'questions':'communityQuestion-list.html','question-detail':'communityQuestion-detail.html','question-write':'communityQuestion-write.html','question-update':'communityQuestion-update.html',
 'chefs':'communityChef-list.html','chef-recipes':'communityChef-recipes.html'
};
function designUrl(key,id=designId) {
 if (designPreview) return previewPages[key]+'?id='+id;
 const paths={recipes:'/community/recipes','recipe-detail':`/community/recipes/${id}`,'recipe-write':'/community/recipes/write','recipe-update':`/community/recipes/${id}/update`,'recipe-mode':`/community/recipes/${id}/mode`,reviews:'/community/cooking','review-detail':`/community/cooking/${id}`,'review-write':id > 0 ? `/community/cooking/write?recipeId=${id}` : '/community/cooking/write','review-update':`/community/cooking/${id}/update`,questions:'/community/questions','question-detail':`/community/questions/${id}`,'question-write':'/community/questions/write','question-update':`/community/questions/${id}/update`,chefs:'/community/chefs','chef-recipes':`/community/chefs/${id}`};
 return designContext+paths[key];
}
function designImage(name) { return designPreview ? 'assets/'+name : designContext+'/dist/images/community-design/'+name; }
function escapeDesign(value) { const el=document.createElement('div'); el.textContent=value??''; return el.innerHTML; }
function designToast(message) {
 const el=document.getElementById('designToast'); el.textContent=message;el.hidden=false;
 clearTimeout(designToast.timeout);designToast.timeout=setTimeout(()=>el.hidden=true,3200);
}
function setDesignLinks() {
    document.querySelectorAll('[data-link]').forEach(a => {
        const id = a.dataset.recipeId !== undefined ? Number(a.dataset.recipeId) : designId;
        a.href = designUrl(a.dataset.link, id);
    });
}
const designChoices = new Map();
function toggleMarkup(r,type) {
 const active=designChoices.get(type+'-'+r.recipeId)===true;
 const count=(type==='like'?r.likes:r.bookmarks)+(active?1:0);
 return `<button type="button" class="ds-toggle ${type==='bookmark'?'bookmark':''} ${active?'active':''}" data-toggle="${type}" data-recipe="${r.recipeId}" aria-pressed="${active}">${type==='like'?(active?'♥':'♡'):(active?'✓':'＋')} ${type==='like'?'좋아요':'찜'} <span>${count}</span></button>`;
}
function renderRecipeTiles(list) {
 document.getElementById('recipeTiles').innerHTML=list.length?list.map(r=>`<article class="ds-recipe-tile"><div class="ds-dish"><img src="${designImage(r.image)}" alt="${escapeDesign(r.title)} 샘플 이미지"><div class="ds-tile-overlay"><a href="${designUrl('recipe-detail',r.recipeId)}">📄 전체보기</a><a href="${designUrl('recipe-mode',r.recipeId)}">🍳 요리모드</a></div></div><span class="ds-tile-category">${escapeDesign(r.category)} · ${escapeDesign(r.tags[0])}</span><h3><a href="${designUrl('recipe-detail',r.recipeId)}">${escapeDesign(r.title)}</a></h3><div class="ds-tile-meta">${escapeDesign(r.authorName)} · ${r.cookMinutes}분 · ${r.servings}인분</div><div class="ds-tile-actions">${toggleMarkup(r,'like')}${toggleMarkup(r,'bookmark')}</div></article>`).join(''):'<p class="ds-empty">작성한 레시피가 없습니다.</p>';
}
function initRecipeList() {
 const search=document.getElementById('recipeSearch'),category=document.getElementById('recipeCategory');
 function draw(){ const list=d.recipes.filter(r=>(!category.value||r.category===category.value)&&(r.title+r.authorName+r.summary).includes(search.value.trim()));renderRecipeTiles(list);document.getElementById('recipeCount').textContent=list.length+'개의 레시피';document.getElementById('recipeEmpty').hidden=list.length>0; }
 search.addEventListener('input',draw);category.addEventListener('change',draw);draw();
}
function initRecipeDetail() {
 const r=demoRecipe;
 document.getElementById('recipeDetail').innerHTML=`<div class="ds-detail-top"><img src="${designImage(r.image)}" alt="${escapeDesign(r.title)}"><div><span class="ds-pill">${escapeDesign(r.category)}</span><h2>${escapeDesign(r.title)}</h2><p class="ds-description">${escapeDesign(r.summary)}</p><p class="ds-author-line">${escapeDesign(r.authorName)} · 2026.10.12</p><div class="ds-info-badges"><span class="ds-pill">◷ ${r.cookMinutes}분</span><span class="ds-pill">♙ ${r.servings}인분</span><span class="ds-pill">난이도 ${r.difficulty}</span></div><div class="ds-tags">${r.tags.map(t=>'<span>#'+escapeDesign(t)+'</span>').join('')}</div><div class="ds-tile-actions" style="justify-content:flex-start">${toggleMarkup(r,'like')}${toggleMarkup(r,'bookmark')}</div></div></div>`;
 document.getElementById('recipeIngredients').innerHTML=r.ingredients.map(i=>`<div class="ds-ingredient-line"><span>${escapeDesign(i.name)}</span><strong>${i.amount}${escapeDesign(i.unit)}</strong></div>`).join('');
 document.getElementById('recipeSteps').innerHTML=r.steps.map((s,i)=>`<div class="ds-step"><span class="ds-step-number">${i+1}</span><p>${escapeDesign(s.description)}</p><img src="${designImage(s.image)}" alt="STEP ${i+1} 이미지"></div>`).join('');
}
// 선택 목록은 샘플 데이터. 실제 구현 시 DB 사전 조회 결과 사용
function addIngredientRow(value=null) {
 const row=document.createElement('div');row.className='ds-ingredient-row';
 row.innerHTML=`<select aria-label="재료 선택" required><option value="">재료 선택</option>${d.ingredients.map(i=>`<option value="${i.ingredientId}">${escapeDesign(i.name)}</option>`).join('')}</select><input type="number" min="0" step="0.001" placeholder="분량" aria-label="재료 분량" required><select aria-label="단위">${['g','ml','개','모','큰술','작은술','포기','단','컵'].map(u=>`<option>${u}</option>`).join('')}</select><button type="button" class="ds-icon-button" data-remove-ingredient aria-label="재료 삭제">×</button>`;
 const selects=row.querySelectorAll('select');if(value){selects[0].value=value.ingredientId;row.querySelector('input').value=value.amount;selects[1].value=value.unit;}
 selects[0].addEventListener('change',()=>{const ingredient=d.ingredients.find(i=>i.ingredientId===Number(selects[0].value));if(ingredient)selects[1].value=ingredient.unit;});
 row.querySelector('[data-remove-ingredient]').addEventListener('click',()=>row.remove());document.getElementById('ingredientRows').append(row);
}
let stepEditorNumber=0;
function addStepRow(value=null) {
 const row=document.createElement('div');row.className='ds-step-editor';const uid=++stepEditorNumber;
 row.innerHTML=`<div class="ds-step-editor-head"><span class="step-label"></span><div><button type="button" class="ds-icon-button" data-step-up aria-label="단계 위로">↑</button><button type="button" class="ds-icon-button" data-step-down aria-label="단계 아래로">↓</button><button type="button" class="ds-icon-button" data-step-remove aria-label="단계 삭제">×</button></div></div><div class="ds-step-editor-body"><label class="ds-photo-upload"><img id="stepPhoto${uid}" alt="단계 사진 미리보기" hidden><span>📷<br>단계 사진 선택</span><input type="file" accept="image/*" data-preview="stepPhoto${uid}"></label><textarea rows="4" required placeholder="이 단계에서 하는 일을 설명해 주세요." aria-label="조리 방법"></textarea></div><label class="ds-step-timer">타이머 · 초 <input type="number" min="0" placeholder="선택" aria-label="타이머 시간"></label>`;
 if(value){row.querySelector('textarea').value=value.description;row.querySelector('input[type=number]').value=value.timerSeconds||'';if(value.image){const img=row.querySelector('img');img.src=designImage(value.image);img.hidden=false;}}
 row.addEventListener('click',e=>{const button=e.target.closest('button');if(!button)return;const box=row.parentElement;if(button.hasAttribute('data-step-up')&&row.previousElementSibling)box.insertBefore(row,row.previousElementSibling);if(button.hasAttribute('data-step-down')&&row.nextElementSibling)box.insertBefore(row.nextElementSibling,row);if(button.hasAttribute('data-step-remove')){if(box.children.length===1){designToast('조리 단계는 최소 한 개 필요해요.');return;}row.remove();}renumberSteps();});
 document.getElementById('stepRows').append(row);renumberSteps();
}
function renumberSteps() {const rows=[...document.querySelectorAll('.ds-step-editor')];rows.forEach((row,i)=>{row.querySelector('.step-label').textContent='STEP '+(i+1);row.querySelector('[data-step-up]').disabled=i===0;row.querySelector('[data-step-down]').disabled=i===rows.length-1;});}
function renderChoices(id,list,name,selected=[]) {document.getElementById(id).innerHTML=list.map((v,i)=>`<label class="ds-chip"><input type="checkbox" name="${name}" value="${i+1}" ${selected.includes(v)?'checked':''}><span>${escapeDesign(v)}</span></label>`).join('');}
function initRecipeForm() {
 const edit=designView==='recipe-update';const r=demoRecipe;
 renderChoices('categoryChoices',d.categories,'categoryIds',edit?[r.category]:[]);renderChoices('tagChoices',d.tags,'tagIds',edit?r.tags:[]);
 if(edit){document.getElementById('recipeTitle').value=r.title;document.getElementById('recipeSummary').value=r.summary;document.getElementById('cookMinutes').value=r.cookMinutes;document.getElementById('servings').value=r.servings;document.getElementById('difficulty').value=r.difficulty;const img=document.getElementById('recipePhotoPreview');img.src=designImage(r.image);img.hidden=false;r.ingredients.forEach(addIngredientRow);r.steps.forEach(addStepRow);document.getElementById('recipeSubmit').textContent='수정 완료';}else{addIngredientRow();addStepRow();}
 document.getElementById('addIngredient').addEventListener('click',()=>addIngredientRow());document.getElementById('addStep').addEventListener('click',()=>addStepRow());
 document.getElementById('recipeForm').addEventListener('submit',e=>{e.preventDefault();const ids=[...document.querySelectorAll('.ds-ingredient-row>select:first-child')].map(el=>el.value);if(!ids.length){designToast('재료를 최소 한 개 선택해 주세요.');return;}if(new Set(ids).size!==ids.length){designToast('같은 재료는 한 번만 선택해 주세요.');return;}designToast('입력 확인 완료! 디자인 화면이므로 실제 저장은 하지 않습니다.');});
}
let modeIndex=0, modeRemaining=0, modeInterval=null;
function initMode(){document.getElementById('modeTitle').textContent=demoRecipe.title;document.getElementById('modePrev').addEventListener('click',()=>{if(modeIndex>0){modeIndex--;drawMode();}});document.getElementById('modeNext').addEventListener('click',()=>{if(modeIndex<demoRecipe.steps.length-1){modeIndex++;drawMode();}});document.getElementById('timerStart').addEventListener('click',()=>{if(modeInterval){clearInterval(modeInterval);modeInterval=null;document.getElementById('timerStart').textContent='타이머 계속';return;}if(modeRemaining===0)modeRemaining=demoRecipe.steps[modeIndex].timerSeconds;document.getElementById('timerStart').textContent='일시정지';modeInterval=setInterval(()=>{modeRemaining=Math.max(0,modeRemaining-1);drawTimer();if(modeRemaining===0){clearInterval(modeInterval);modeInterval=null;document.getElementById('timerStart').textContent='다시 시작';designToast('타이머가 완료됐어요.');}},1000);});drawMode();}
function drawTimer(){document.getElementById('timerText').textContent=String(Math.floor(modeRemaining/60)).padStart(2,'0')+':'+String(modeRemaining%60).padStart(2,'0');}
function drawMode(){clearInterval(modeInterval);modeInterval=null;const steps=demoRecipe.steps,s=steps[modeIndex];document.getElementById('modeStep').textContent='STEP '+(modeIndex+1);document.getElementById('modeProgressText').textContent=(modeIndex+1)+' / '+steps.length+' 단계';document.getElementById('modeProgress').style.width=((modeIndex+1)/steps.length*100)+'%';document.getElementById('modeImage').src=designImage(s.image);document.getElementById('modeStepLabel').textContent='STEP '+(modeIndex+1)+' · 지금 할 일';document.getElementById('modeDescription').textContent=s.description;document.getElementById('modePrev').disabled=modeIndex===0;document.getElementById('modeNext').disabled=modeIndex===steps.length-1;document.getElementById('modeFinish').hidden=modeIndex!==steps.length-1;document.getElementById('modeTimer').hidden=!s.timerSeconds;modeRemaining=s.timerSeconds;document.getElementById('timerStart').textContent='타이머 시작';drawTimer();}
function originalMarkup(recipeId,active=true){const r=d.recipes.find(x=>x.recipeId===recipeId)||d.recipes[0];return `<img src="${designImage(r.image)}" alt="원본 레시피"><div><p>따라 만든 원본 레시피</p><strong>${escapeDesign(r.title)}</strong></div>${active?`<a class="ds-button light" href="${designUrl('recipe-detail',r.recipeId)}">원본 레시피 보기 →</a>`:'<span class="ds-pill">현재 공개되지 않은 레시피</span>'}`;}
function initReviews(){const input=document.getElementById('reviewSearch');function draw(){const list=d.reviews.filter(r=>(r.title+(d.recipes.find(x=>x.recipeId===r.recipeId)?.title||'')).includes(input.value.trim()));document.getElementById('reviewCount').textContent=list.length+'개의 후기';document.getElementById('reviewGrid').innerHTML=list.length?list.map(r=>`<article class="ds-review-card"><a href="${designUrl('review-detail',r.reviewId)}"><img src="${designImage(r.image)}" alt="${escapeDesign(r.title)}"><div class="ds-review-card-content"><span class="ds-pill">따라 만든 한 접시</span><h3>${escapeDesign(r.title)}</h3><p>${escapeDesign(r.authorName)} · 2026.10.12</p></div></a></article>`).join(''):'<p class="ds-empty">검색 결과가 없습니다.</p>'; }input.addEventListener('input',draw);draw();}
function initReviewDetail(){const r=d.reviews.find(x=>x.reviewId===designId)||d.reviews[0];document.getElementById('reviewDetail').innerHTML=`<p class="ds-eyebrow">MY COOKING STORY</p><h2 class="ds-question-title">${escapeDesign(r.title)}</h2><p class="ds-author-line">${escapeDesign(r.authorName)} · 2026.10.12</p><div class="ds-original">${originalMarkup(r.recipeId,r.originalActive)}</div><img class="ds-review-photo" src="${designImage(r.image)}" alt="따라 만든 요리"><p class="ds-review-text">${escapeDesign(r.content)}</p>${r.authorId===3?`<div class="ds-bottom-actions"><a class="ds-button light" href="${designUrl('review-update',r.reviewId)}">수정 화면 보기</a><button type="button" class="ds-button light" data-demo-delete>삭제</button></div>`:''}`;}
// 목록에서 작성하면 원본 미선택, 레시피 상세에서 작성하면 원본 미리 선택
function initReviewForm() {
    const edit = designView === 'review-update';
    const review = d.reviews.find(x => x.reviewId === designId) || d.reviews[0];
    const form = document.getElementById('reviewForm');
    const original = document.getElementById('reviewOriginal');
    let selectedRecipeId = edit ? review.recipeId : designId;

    // 기존 수정 HTML에서도 원본 번호를 담을 수 있도록 처리
    let recipeInput = form.querySelector('input[name="recipeId"]');
    if (!recipeInput) {
        recipeInput = document.createElement('input');
        recipeInput.type = 'hidden';
        recipeInput.name = 'recipeId';
        form.prepend(recipeInput);
    }

    function drawOriginal() {
        const recipe = d.recipes.find(r => r.recipeId === selectedRecipeId);
        recipeInput.value = recipe ? recipe.recipeId : '';

        if (!recipe) {
            selectedRecipeId = 0;
            original.innerHTML = `
                <div class="ds-original-empty-icon" aria-hidden="true">🍳</div>
                <div><p>따라 만든 원본 레시피</p><strong>원본 레시피를 선택해 주세요.</strong></div>
                <button type="button" class="ds-button primary" data-choose-original>원본 레시피 선택</button>
            `;
            return;
        }

        original.innerHTML = originalMarkup(recipe.recipeId, edit ? review.originalActive : true);
        if (!edit) {
            original.insertAdjacentHTML('beforeend', '<button type="button" class="ds-button light" data-choose-original>변경</button>');
        }
    }

    drawOriginal();

    if (edit) {
        document.getElementById('reviewTitle').value = review.title;
        document.getElementById('reviewContent').value = review.content;
        const img = document.getElementById('reviewPhotoPreview');
        img.src = designImage(review.image);
        img.hidden = false;
        document.getElementById('reviewSubmit').textContent = '수정 완료';
    } else {
        const picker = document.getElementById('recipePicker');
        const search = document.getElementById('recipePickerSearch');
        const list = document.getElementById('recipePickerList');

        // 현재는 샘플 목록. 실제 구현에서는 공개 레시피 검색 API로 교체
        function drawPicker() {
            const kwd = search.value.trim();
            const recipes = d.recipes.filter(r => (r.title + r.authorName).includes(kwd));
            list.innerHTML = recipes.length ? recipes.map(r => `
                <button type="button" class="ds-picker-row" data-select-recipe="${r.recipeId}">
                    <img src="${designImage(r.image)}" alt="${escapeDesign(r.title)}">
                    <span class="ds-picker-recipe-text"><strong>${escapeDesign(r.title)}</strong><small>${escapeDesign(r.authorName)} · ${r.cookMinutes}분</small></span>
                    <span class="ds-picker-select">${r.recipeId === selectedRecipeId ? '선택됨 ✓' : '선택'}</span>
                </button>
            `).join('') : '<p class="ds-empty">검색 결과가 없습니다.</p>';
        }

        original.addEventListener('click', e => {
            if (!e.target.closest('[data-choose-original]')) return;
            search.value = '';
            drawPicker();
            picker.showModal();
            search.focus();
        });

        search.addEventListener('input', drawPicker);
        document.getElementById('closeRecipePicker').addEventListener('click', () => picker.close());
        picker.addEventListener('click', e => { if (e.target === picker) picker.close(); });

        list.addEventListener('click', e => {
            const button = e.target.closest('[data-select-recipe]');
            if (!button) return;
            selectedRecipeId = Number(button.dataset.selectRecipe);
            drawOriginal();
            picker.close();
        });
    }

    form.addEventListener('submit', e => {
        e.preventDefault();
        if (!selectedRecipeId || !recipeInput.value) {
            designToast('따라 만든 원본 레시피를 먼저 선택해 주세요.');
            original.querySelector('[data-choose-original]')?.focus();
            return;
        }
        designToast('입력 확인 완료! 실제 후기 저장은 다음 구현에서 연결합니다.');
    });
}

function initQuestions(){const input=document.getElementById('questionSearch'),type=document.getElementById('questionSearchType');function draw(){const list=d.questions.filter(q=>(q.title+(type.value==='all'?q.content:'')).includes(input.value.trim()));document.getElementById('questionList').innerHTML=list.length?list.map(q=>`<a class="ds-question-row" href="${designUrl('question-detail',q.questionId)}"><div><h3>${escapeDesign(q.title)}</h3><div class="ds-question-meta">${escapeDesign(q.authorName)} · ${q.date}</div></div><div class="ds-answer-count"><strong>${q.answers.length}</strong>답변</div></a>`).join(''):'<p class="ds-empty">검색 결과가 없습니다.</p>'; }input.addEventListener('input',draw);type.addEventListener('change',draw);draw();}
function drawAnswers(q){document.getElementById('answerCount').textContent=q.answers.length;document.getElementById('answerList').innerHTML=q.answers.length?q.answers.map(a=>`<div class="ds-answer" data-answer-id="${a.answerId}"><div class="ds-answer-head"><strong>${escapeDesign(a.authorName)}</strong><span>2026.10.12</span></div><p>${escapeDesign(a.content)}</p>${a.owner?'<div class="ds-text-actions"><button type="button" data-answer-edit>수정</button><button type="button" data-answer-delete>삭제</button></div>':''}</div>`).join(''):'<p class="ds-help">아직 답변이 없어요. 첫 답변을 남겨보세요.</p>';}
function initQuestionDetail(){const q=d.questions.find(x=>x.questionId===designId)||d.questions[0];document.getElementById('questionDetail').innerHTML=`<span class="ds-pill">🙋 요리 질문</span><h2 class="ds-question-title" style="margin-top:12px">${escapeDesign(q.title)}</h2><p class="ds-author-line">${escapeDesign(q.authorName)} · ${q.date}</p><p class="ds-question-text">${escapeDesign(q.content)}</p>${q.authorId===3?`<div class="ds-bottom-actions"><a class="ds-button light" href="${designUrl('question-update',q.questionId)}">수정 화면 보기</a><button type="button" class="ds-button light" data-demo-delete>삭제</button></div>`:''}`;drawAnswers(q);document.getElementById('answerForm').addEventListener('submit',e=>{e.preventDefault();const input=document.getElementById('answerContent');if(!input.value.trim())return;q.answers.push({answerId:Date.now(),authorName:'오늘의밥상',owner:true,content:input.value.trim()});input.value='';drawAnswers(q);designToast('샘플 답변을 화면에 추가했어요. DB에는 저장되지 않습니다.');});document.getElementById('answerList').addEventListener('click',e=>{const btn=e.target.closest('button');if(!btn)return;const item=btn.closest('.ds-answer'),answer=q.answers.find(a=>a.answerId===Number(item.dataset.answerId));if(!answer?.owner)return;if(btn.hasAttribute('data-answer-delete')){if(confirm('샘플 답변을 화면에서 지울까요?')){q.answers=q.answers.filter(a=>a!==answer);drawAnswers(q);}return;}if(btn.hasAttribute('data-answer-edit')){const edit=document.createElement('div');edit.className='ds-answer-form';edit.innerHTML='<textarea rows="3" aria-label="답변 수정"></textarea><div class="ds-bottom-actions"><button type="button" class="ds-button light" data-cancel>취소</button><button type="button" class="ds-button primary" data-save>저장</button></div>';edit.querySelector('textarea').value=answer.content;item.replaceChildren(edit);edit.querySelector('[data-cancel]').onclick=()=>drawAnswers(q);edit.querySelector('[data-save]').onclick=()=>{const value=edit.querySelector('textarea').value.trim();if(!value){designToast('답변 내용을 입력해 주세요.');return;}answer.content=value;drawAnswers(q);};}});}
function initQuestionForm(){const edit=designView==='question-update',q=d.questions.find(x=>x.questionId===designId)||d.questions[0];if(edit){document.getElementById('questionTitle').value=q.title;document.getElementById('questionContent').value=q.content;document.getElementById('questionSubmit').textContent='수정 완료';}document.getElementById('questionForm').addEventListener('submit',e=>{e.preventDefault();designToast('입력 확인 완료! 실제 질문 저장은 다음 구현에서 연결합니다.');});}
function chefStats(c){return `<div class="ds-chef-stats"><div><strong>${c.recipeCount}</strong>작성 레시피</div><div><strong>${c.likeCount}</strong>받은 좋아요</div></div>`;}
function initChefs(){document.getElementById('chefPodium').innerHTML=d.chefs.slice(0,3).map((c,i)=>`<a class="ds-chef-card ${i===0?'first':''}" href="${designUrl('chef-recipes',c.memberId)}"><span class="ds-rank">${['🥇','🥈','🥉'][i]} ${i+1}위</span><img src="${designImage(c.image)}" alt="${escapeDesign(c.name)} 프로필"><h3>${escapeDesign(c.name)}</h3><p>${escapeDesign(c.intro)}</p>${chefStats(c)}</a>`).join('');document.getElementById('chefList').innerHTML=d.chefs.slice(3).map((c,i)=>`<a class="ds-chef-row" href="${designUrl('chef-recipes',c.memberId)}"><strong>${i+4}</strong><img src="${designImage(c.image)}" alt="프로필"><div><strong>${escapeDesign(c.name)}</strong><p class="ds-help">${escapeDesign(c.intro)}</p></div>${chefStats(c)}</a>`).join('');}
function initChefRecipes(){const c=d.chefs.find(c=>c.memberId===designId)||d.chefs[0];document.getElementById('chefProfile').innerHTML=`<img src="${designImage(c.image)}" alt="${escapeDesign(c.name)} 프로필"><div><p class="ds-eyebrow">OUR KITCHEN CREATOR</p><h2>${escapeDesign(c.name)}</h2><p>${escapeDesign(c.intro)}</p>${chefStats(c)}</div>`;renderRecipeTiles(d.recipes.filter(r=>r.authorId===c.memberId));}
// 사진 선택 시 서버 업로드 대신 미리보기만 표시
const designBlobUrls=[];
designRoot.addEventListener('change',e=>{const input=e.target;if(!input.matches('input[type=file][data-preview]'))return;const file=input.files?.[0];if(!file)return;if(!file.type.startsWith('image/')){designToast('이미지 파일을 선택해 주세요.');input.value='';return;}const img=document.getElementById(input.dataset.preview);const url=URL.createObjectURL(file);designBlobUrls.push(url);img.src=url;img.hidden=false;});
window.addEventListener('beforeunload',()=>{designBlobUrls.forEach(url=>URL.revokeObjectURL(url));clearInterval(modeInterval);});
designRoot.addEventListener('click',e=>{const btn=e.target.closest('[data-toggle]');if(btn){const r=d.recipes.find(x=>x.recipeId===Number(btn.dataset.recipe)),type=btn.dataset.toggle,key=type+'-'+r.recipeId;designChoices.set(key,!designChoices.get(key));document.querySelectorAll(`[data-toggle="${type}"][data-recipe="${r.recipeId}"]`).forEach(el=>{const temp=document.createElement('div');temp.innerHTML=toggleMarkup(r,type);el.replaceWith(temp.firstElementChild);});designToast(type==='like'?'샘플 좋아요 표시를 변경했어요.':'샘플 찜 표시를 변경했어요.');}if(e.target.closest('[data-demo-delete]')){if(confirm('디자인 화면에서는 실제 데이터를 삭제하지 않습니다. 확인할까요?'))designToast('실제 삭제는 작성자 권한 확인 후 서버에 연결할 예정입니다.');}});
window.addEventListener('DOMContentLoaded',()=>{setDesignLinks();const init={'recipe-list':initRecipeList,'recipe-detail':initRecipeDetail,'recipe-write':initRecipeForm,'recipe-update':initRecipeForm,'recipe-mode':initMode,'review-list':initReviews,'review-detail':initReviewDetail,'review-write':initReviewForm,'review-update':initReviewForm,'question-list':initQuestions,'question-detail':initQuestionDetail,'question-write':initQuestionForm,'question-update':initQuestionForm,'chef-list':initChefs,'chef-recipes':initChefRecipes};init[designView]?.();});
