// 레시피 검색 : 화면 확인용 예시 데이터 (DB / API / AI 미연결)
(() => {
    const ingredients = [
        {id:1,name:'계란',aliases:['달걀']}, {id:2,name:'두부',aliases:[]},
        {id:3,name:'대파',aliases:['파']}, {id:4,name:'밥',aliases:['쌀밥']},
        {id:5,name:'김치',aliases:['배추김치']}, {id:6,name:'돼지고기',aliases:['돼지']},
        {id:7,name:'감자',aliases:[]}, {id:8,name:'애호박',aliases:['호박']},
        {id:9,name:'된장',aliases:[]}, {id:10,name:'고등어',aliases:[]},
        {id:11,name:'무',aliases:[]}, {id:12,name:'양파',aliases:[]}
    ];
    const eggPhoto = {file:'Gyeran-jjim.jpg',name:'계란찜',author:'stu_spivack',license:'CC BY-SA 2.0',licenseUrl:'https://creativecommons.org/licenses/by-sa/2.0/'};
    const tofuPhoto = {file:'Korean.pancake-Dubujeon-01.jpg',name:'두부전',author:'blue.tofu',license:'CC BY 2.0',licenseUrl:'https://creativecommons.org/licenses/by/2.0/'};
    const recipes = [
        {id:'demo-1',title:'대파 계란찜',source:'official',main:[1,3],photo:eggPhoto,minutes:15,servings:2,difficulty:'쉬움',description:'부드럽게 익힌 계란에 대파 향을 더하는 한 접시.',amounts:['계란 3개','대파 약간','물 150mL','소금·참기름 약간'],steps:['계란을 풀고 물과 소금을 섞어요.','잘게 썬 대파를 넣고 내열 그릇에 담아요.','김이 오른 찜기에서 중약불로 약 10~12분, 중심까지 익혀요. 그릇과 화력에 따라 시간이 달라요.','참기름을 조금 더해 마무리해요.']},
        {id:'demo-2',title:'두부 계란전',source:'member',main:[1,2,3],photo:tofuPhoto,minutes:20,servings:2,difficulty:'쉬움',description:'두부의 물기를 빼고 작게 부쳐내는 노릇한 반찬.',amounts:['두부 반 모','계란 1개','다진 대파 약간','소금·식용유 약간'],steps:['두부의 물기를 충분히 빼고 으깨요.','계란과 다진 대파, 소금을 섞어요.','기름을 두른 팬에 반죽을 작게 떠 넣어요.','중약불에서 앞뒤를 노릇하게 굽고 속까지 익혀요.']},
        {id:'demo-3',title:'두부 계란국',source:'official',main:[1,2,3],photo:null,minutes:15,servings:2,difficulty:'쉬움',description:'두부와 계란을 넣은 담백한 국물 요리.',amounts:['두부 반 모','계란 1개','대파 약간','육수 600mL','국간장·다진 마늘 약간'],steps:['육수에 작게 썬 두부를 넣고 끓여요.','국간장과 다진 마늘로 간해요.','풀어 둔 계란을 가늘게 붓고 익을 때까지 끓여요.','썬 대파를 넣고 한 번 더 끓여요.']},
        {id:'demo-4',title:'대파 계란볶음밥',source:'member',main:[1,3,4],photo:null,minutes:15,servings:1,difficulty:'쉬움',description:'한 가지 재료가 부족한 경우의 목록 디자인 예시.',amounts:['계란·대파·밥 — 예시 재료'],steps:['대파를 얇게 썰고 계란을 풀어 준비해요.','팬에 기름을 두르고 대파를 볶아 향을 내요.','계란을 넣고 저어 익힌 뒤 밥을 넣어요.','밥을 고루 볶아 간하고 계란이 충분히 익으면 마무리해요.']},
        {id:'demo-5',title:'두부 김치볶음',source:'official',main:[2,5,6],photo:tofuPhoto,minutes:null,servings:null,difficulty:null,description:'사진·재료 정보가 일부만 제공되는 레시피 예시.',amounts:['두부·김치·돼지고기 — 예시 재료'],steps:['두부와 김치, 돼지고기를 먹기 좋은 크기로 준비해요.','팬에서 돼지고기를 충분히 익힌 뒤 김치를 넣고 볶아요.','두부를 곁들이고 재료가 고루 익도록 볶아요.','접시에 담아 따뜻하게 완성해요.']},
        {id:'demo-6',title:'애호박 두부 된장국',source:'official',main:[2,3,8,9],photo:null,minutes:20,servings:null,difficulty:null,description:'부족한 주재료 이름과 개수를 확인하는 예시.',amounts:['두부·대파·애호박·된장 — 예시 재료'],steps:['애호박과 두부를 먹기 좋은 크기로 썰어요.','끓는 육수에 된장을 풀어요.','애호박과 두부를 넣고 재료가 익을 때까지 끓여요.','대파를 넣고 간을 확인한 뒤 마무리해요.']},
        {id:'demo-7',title:'감자 계란국',source:'member',main:[1,3,7],photo:eggPhoto,minutes:null,servings:2,difficulty:null,description:'원본에 없는 시간과 난이도는 표시하지 않아요.',amounts:['계란·대파·감자 — 예시 재료'],steps:['감자는 얇게 썰고 계란은 풀어 준비해요.','끓는 육수에 감자를 넣고 부드럽게 익혀요.','계란을 가늘게 부어 익을 때까지 끓여요.','대파를 넣고 간을 맞춰 마무리해요.']},
        {id:'demo-8',title:'고등어 무조림',source:'official',main:[3,10,11],photo:null,minutes:null,servings:null,difficulty:null,description:'선택한 재료와 적게 일치하는 결과의 예시.',amounts:['고등어·무·대파 — 예시 재료'],steps:['손질한 고등어와 도톰하게 썬 무, 대파를 준비해요.','냄비에 무를 깔고 고등어와 조림 양념, 물을 넣어요.','무가 부드러워지고 생선이 속까지 충분히 익도록 조려요.','대파를 넣고 국물을 조금 더 졸여 완성해요.']},
        {id:'private-example',title:'비공개 예시',source:'member',main:[1,2,3],public:false,steps:[]},
        {id:'deleted-example',title:'삭제 예시',source:'member',main:[1,2,3],deleted:true,steps:[]}
    ];
    const name = id => ingredients.find(item=>item.id===id)?.name || '';
    function candidates(query) {
        const term = query.trim().toLocaleLowerCase('ko');
        if (!term) return [];
        return ingredients.filter(item=>[item.name,...item.aliases].some(value=>value.toLocaleLowerCase('ko').includes(term)));
    }
    function compare(recipe,selected) {
        const matched = recipe.main.filter(id=>selected.includes(id));
        const missing = recipe.main.filter(id=>!selected.includes(id));
        return {matched,missing};
    }
    function search(selected,{source='all',availability='all',term=''}={}) {
        return recipes.filter(recipe=>recipe.public!==false&&!recipe.deleted)
            .map(recipe=>({...recipe,...compare(recipe,selected)}))
            .filter(recipe=>recipe.matched.length>0)
            .filter(recipe=>source==='all'||recipe.source===source)
            .filter(recipe=>availability==='all'||(availability==='complete'?recipe.missing.length===0:recipe.missing.length>0))
            .filter(recipe=>recipe.title.includes(term.trim()))
            .sort((a,b)=>b.matched.length-a.matched.length||a.title.localeCompare(b.title,'ko'));
    }
    globalThis.EatdaAiDemo = {ingredients,recipes,name,candidates,compare,search};
})();

(() => {
    'use strict';
    const island = document.querySelector('.ai-island');
    const model = globalThis.EatdaAiDemo;
    if (!island || !model) return;
    const $ = id => document.getElementById(id);
    const toggle = $('ai-island-toggle');
    const panel = island.querySelector('.ai-results-panel');
    const launcher = island.querySelector('.ai-launcher');
    $('ai-recipe-submit').setAttribute('aria-controls','ai-island-body');
    $('ai-recipe-submit').setAttribute('aria-expanded','false');
    const body = $('ai-island-body');
    const input = $('ai-ingredient-input');
    const options = $('ai-ingredient-options');
    const picked = $('ai-picked');
    const status = $('ai-island-status');
    const resultsSection = $('ai-results');
    const list = $('ai-results-list');
    const more = $('ai-results-more');
    const selected = new Set();
    const reduced = () => matchMedia('(prefers-reduced-motion: reduce)').matches;
    function el(tag, className, value) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (value !== undefined) node.textContent = value;
        return node;
    }
    function button(label,className,handler) {
        const node=el('button',className,label); node.type='button';
        if(handler)node.addEventListener('click',handler);
        return node;
    }
    function closeIcon() {
        const node=button('','ai-icon-close');
        node.setAttribute('aria-label','닫기');
        node.innerHTML='<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18"/></svg>';
        return node;
    }

    // 검색 결과창 : 열기 / 닫기 애니메이션 처리
    let open=false, panelAnimation, contentAnimation, panelTimer, panelVersion=0;
    let savedOverflow='', savedScroll=0;
    function setOpen(value) {
        const version=++panelVersion;
        const before=(island.classList.contains('is-open')?panel:launcher).getBoundingClientRect();
        panelAnimation?.cancel(); contentAnimation?.cancel(); clearTimeout(panelTimer);
        if(value) {
            const wasFloating=island.classList.contains('is-open');
            if(!wasFloating) { savedOverflow=document.documentElement.style.overflow; document.documentElement.style.overflow='hidden'; }
            const headerBottom=Math.max(0,document.querySelector('.site-header')?.getBoundingClientRect().bottom||0);
            island.style.setProperty('--ai-panel-top',Math.max(16,Math.min(headerBottom+12,window.innerHeight-240))+'px');
            open=true; island.classList.add('is-open'); island.classList.remove('is-closing');
            body.inert=false; body.scrollTop=savedScroll;
            launcher.inert=true;
            panel.focus({preventScroll:true});
            $('ai-recipe-submit').setAttribute('aria-expanded','true');
            const after=panel.getBoundingClientRect();
            if(!reduced()) {
                panelAnimation=panel.animate([box(before),box(after)],{duration:520,easing:'cubic-bezier(.22,1,.36,1)'});
                contentAnimation=body.animate([{opacity:0},{opacity:1}],{duration:300,delay:100,easing:'ease-out'});
            }
            rotatePlaceholder();
        } else {
            if(!island.classList.contains('is-open'))return;
            open=false; savedScroll=body.scrollTop; body.inert=true; stopPlaceholder(); hideOptions();
            island.classList.add('is-closing'); $('ai-recipe-submit').setAttribute('aria-expanded','false');
            const anchor=launcher.getBoundingClientRect();
            function finish() {
                if(version!==panelVersion)return;
                clearTimeout(panelTimer);
                island.classList.remove('is-open','is-closing');
                panelAnimation?.cancel(); contentAnimation?.cancel();
                document.documentElement.style.overflow=savedOverflow;
                launcher.inert=false;
                input.focus({preventScroll:true});
            }
            if(reduced())finish();
            else {
                contentAnimation=body.animate([{opacity:1},{opacity:0}],{duration:170,fill:'forwards'});
                panelAnimation=panel.animate([box(before),box(anchor)],{duration:460,easing:'cubic-bezier(.4,0,.2,1)',fill:'forwards'});
                panelAnimation.onfinish=finish;
                panelTimer=setTimeout(finish,560);
            }
        }
    }
    function box(rect){return {left:rect.left+'px',top:rect.top+'px',width:rect.width+'px',height:rect.height+'px'};}
    toggle.addEventListener('click',()=>setOpen(false));
    $('ai-edit-ingredients').addEventListener('click',()=>setOpen(false));
    let placeholderTimer, placeholderFade;
    const examples=['예: 계란 또는 달걀','예: 두부, 대파','예: 김치, 돼지고기','예: 감자, 애호박'];
    let exampleIndex=Math.floor(Math.random()*examples.length);
    input.placeholder=examples[exampleIndex];
    function stopPlaceholder(){clearTimeout(placeholderTimer);clearTimeout(placeholderFade);input.classList.remove('ai-placeholder-changing');}
    function rotatePlaceholder(){
        stopPlaceholder();
        if(document.hidden||input.value||document.activeElement===input||reduced())return;
        placeholderTimer=setTimeout(()=>{
            input.classList.add('ai-placeholder-changing');
            placeholderFade=setTimeout(()=>{
                exampleIndex=(exampleIndex+1+Math.floor(Math.random()*(examples.length-1)))%examples.length;
                input.placeholder=examples[exampleIndex];input.classList.remove('ai-placeholder-changing');rotatePlaceholder();
            },180);
        },3500);
    }
    input.addEventListener('focus',stopPlaceholder);
    input.addEventListener('blur',rotatePlaceholder);
    document.addEventListener('visibilitychange',()=>document.hidden?stopPlaceholder():rotatePlaceholder());

    // 재료 자동완성 : 등록된 예시 재료만 선택 가능
    let candidates=[], active=-1, composing=false;
    function hideOptions(){options.hidden=true;input.setAttribute('aria-expanded','false');input.removeAttribute('aria-activedescendant');active=-1;}
    function activate(index){
        active=index;
        options.querySelectorAll('[role="option"]').forEach((node,i)=>node.setAttribute('aria-selected',String(i===active)));
        if(active>=0)input.setAttribute('aria-activedescendant','ai-option-'+active);else input.removeAttribute('aria-activedescendant');
    }
    function suggest(){
        stopPlaceholder();
        if(composing)return;
        candidates=model.candidates(input.value);
        options.replaceChildren();
        if(!input.value.trim()){hideOptions();return;}
        if(!candidates.length)options.append(el('p','ai-no-suggestion','등록된 재료가 없습니다. 입력한 문자열은 검색 조건에 추가되지 않아요.'));
        candidates.forEach((item,index)=>{
            const node=button('','ai-suggestion',()=>choose(item));
            node.id='ai-option-'+index;node.setAttribute('role','option');node.setAttribute('aria-selected','false');
            node.append(el('span','',item.name),el('small','',selected.has(item.id)?'선택됨':'재료 선택'));
            options.append(node);
        });
        options.hidden=false;input.setAttribute('aria-expanded','true');activate(candidates.length?0:-1);
    }
    function choose(item){
        const duplicate=selected.has(item.id);
        selected.add(item.id);input.value='';hideOptions();
        invalidateResults();renderPicked();
        status.textContent=duplicate?'이미 선택한 재료입니다.':item.name+' 재료를 추가했어요.';
        input.focus();
    }
    function renderPicked(){
        picked.replaceChildren();
        for(const id of selected){
            const node=button('','ai-chip',()=>{selected.delete(id);invalidateResults();renderPicked();status.textContent='재료가 변경됐어요. 다시 검색해 주세요.';input.focus();});
            node.setAttribute('aria-label',model.name(id)+' 삭제');
            node.append(document.createTextNode(model.name(id)),el('span','','×'));picked.append(node);
        }
        if(!selected.size)picked.append(el('span','ai-picked-empty','재료 후보를 선택하면 여기에 표시돼요.'));
    }
    input.addEventListener('input',suggest);
    input.addEventListener('compositionstart',()=>{composing=true;stopPlaceholder();});
    input.addEventListener('compositionend',()=>{composing=false;suggest();});
    input.addEventListener('keydown',event=>{
        if(event.isComposing||composing||event.keyCode===229)return;
        if(event.key==='ArrowDown'&&candidates.length&&!options.hidden){event.preventDefault();activate((active+1)%candidates.length);}
        if(event.key==='ArrowUp'&&candidates.length&&!options.hidden){event.preventDefault();activate((active-1+candidates.length)%candidates.length);}
        if(event.key==='Enter'&&!options.hidden&&active>=0){event.preventDefault();choose(candidates[active]);}
        if(event.key==='Escape'&&!options.hidden){event.preventDefault();event.stopPropagation();hideOptions();}
    });

    // 레시피 검색 : 이전 응답이 최신 검색 결과를 덮어쓰지 않도록 처리
    let requestVersion=0, requestTimer, results=[], shown=0, searchSelection=[], source='all';
    function invalidateResults(){++requestVersion;clearTimeout(requestTimer);list.setAttribute('aria-busy','false');resultsSection.hidden=true;}
    function runSearch(){
        hideOptions();
        if(input.value.trim()){status.textContent='입력한 재료 후보를 먼저 선택해 주세요.';input.focus();suggest();return;}
        if(!selected.size){status.textContent='재료를 하나 이상 선택해 주세요.';input.focus();return;}
        if(!open)setOpen(true);
        const version=++requestVersion;
        clearTimeout(requestTimer);
        searchSelection=[...selected];
        const snapshot={source,availability:$('ai-availability').value};
        resultsSection.hidden=false;list.setAttribute('aria-busy','true');
        list.replaceChildren(el('p','ai-empty-state','예시 결과를 준비하고 있어요…'));
        more.hidden=true;status.textContent='디자인 예시를 조회하는 중입니다. 실제 서버 요청은 보내지 않아요.';
        // 로딩 확인용 타이머 > 서버 연동 시 fetch로 변경
        requestTimer=setTimeout(()=>{
            if(version!==requestVersion)return;
            results=model.search(searchSelection,snapshot);shown=0;list.replaceChildren();appendResults();
            list.setAttribute('aria-busy','false');
            $('ai-results-query').textContent='선택한 재료 · '+searchSelection.map(model.name).join(' · ');
            $('ai-result-total').textContent='예시 레시피 '+results.length+'개';
            status.textContent=results.length?'예시 결과를 표시했어요. 실제 API·AI 결과가 아닙니다.':'선택한 재료와 일치하는 예시 결과가 없어요. 재료나 필터를 바꿔보세요.';
            if(!results.length)list.append(el('p','ai-empty-state','검색 결과가 없습니다. 조회 오류가 아닌 빈 목록입니다.'));
            if(!reduced())list.animate([{opacity:0,transform:'translateY(6px)'},{opacity:1,transform:'translateY(0)'}],{duration:260,easing:'ease-out'});
        },420);
    }
    $('ai-island-form').addEventListener('submit',event=>{event.preventDefault();runSearch();});
    document.addEventListener('ai-fridge-search',event=>{
        selected.clear();event.detail.forEach(id=>selected.add(id));input.value='';renderPicked();runSearch();
    });
    $('ai-demo-results').addEventListener('click',()=>{selected.clear();[1,2,3].forEach(id=>selected.add(id));input.value='';renderPicked();runSearch();});
    $('ai-demo-cook').addEventListener('click',event=>openRecipe('demo-1','cook',event.currentTarget));
    document.querySelectorAll('[data-source]').forEach(node=>node.addEventListener('click',()=>{
        source=node.dataset.source;
        document.querySelectorAll('[data-source]').forEach(button=>button.setAttribute('aria-pressed',String(button===node)));
        runSearch();
    }));
    $('ai-availability').addEventListener('change',runSearch);
    function sourceName(recipe){return recipe.source==='official'?'공식 API · 출처 표시 예시':'공개 회원 · 출처 표시 예시';}
    function photo(recipe){
        const figure=el('figure','ai-recipe-photo');
        if(!recipe.photo){figure.append(el('div','ai-photo-fallback','사진 미제공'));return figure;}
        const item=recipe.photo, image=el('img');
        image.src='https://commons.wikimedia.org/wiki/Special:FilePath/'+encodeURIComponent(item.file)+'?width=500';
        image.alt=item.name+' · 사진 배치 예시';image.loading='lazy';image.width=155;image.height=124;
        image.onerror=()=>image.replaceWith(el('div','ai-photo-fallback','사진을 불러오지 못했어요'));
        const caption=el('figcaption');
        const credit=el('a','',item.author);credit.href='https://commons.wikimedia.org/wiki/File:'+encodeURIComponent(item.file);credit.target='_blank';credit.rel='noopener noreferrer';
        const license=el('a','',item.license);license.href=item.licenseUrl;license.target='_blank';license.rel='noopener noreferrer';
        caption.append(document.createTextNode('예시 사진 · '+item.name+' / '),credit,document.createTextNode(' · '),license,document.createTextNode(' · 화면 크롭'));
        figure.append(image,caption);return figure;
    }
    function facts(recipe,className){
        const node=el('div',className);
        if(recipe.minutes!=null)node.append(el('span','','약 '+recipe.minutes+'분'));
        if(recipe.servings!=null)node.append(el('span','',recipe.servings+'인분'));
        if(recipe.difficulty)node.append(el('span','',recipe.difficulty));
        return node;
    }
    function appendResults(){
        const fragment=document.createDocumentFragment();
        results.slice(shown,shown+4).forEach(recipe=>{
            const row=el('article','ai-recipe-card');row.dataset.recipeId=recipe.id;
            const title=el('a','ai-recipe-title',recipe.title);title.href='#ai-recipe-detail';title.setAttribute('aria-haspopup','dialog');
            title.addEventListener('click',event=>{event.preventDefault();openRecipe(recipe.id,'detail',title);});
            const match=el('div','ai-recipe-match');
            match.append(el('span','ai-match-number','주재료 '+recipe.matched.length+'/'+recipe.main.length+' 일치'),el('span','',recipe.matched.map(model.name).join(' · ')));
            match.append(el('span','ai-missing-state'+(recipe.missing.length?'':' is-complete'),recipe.missing.length?'추가 주재료 '+recipe.missing.length+'개':'주재료 모두 있어요'));
            if(recipe.missing.length)match.append(el('span','',recipe.missing.map(model.name).join(' · ')));
            const actions=el('div','ai-recipe-actions');
            const detail=button('상세보기','',()=>openRecipe(recipe.id,'detail',detail));detail.setAttribute('aria-haspopup','dialog');
            const cook=button('요리 시작','ai-cook-button',()=>openRecipe(recipe.id,'cook',cook));cook.setAttribute('aria-haspopup','dialog');cook.disabled=!recipe.steps.length;
            actions.append(detail,cook);if(!recipe.steps.length)actions.append(el('small','','조리 단계 미제공'));
            row.append(photo(recipe),el('span','ai-recipe-source',sourceName(recipe)),title,el('p','ai-recipe-description',recipe.description),facts(recipe,'ai-recipe-facts'),match,actions);fragment.append(row);
        });
        list.append(fragment);shown=Math.min(shown+4,results.length);
        $('ai-result-count').textContent=shown+' / '+results.length+'개 표시';more.hidden=shown>=results.length;
    }
    more.addEventListener('click',appendResults);

    // 레시피 대화상자 : 상세보기 / 요리시작 화면 전환
    const modal=el('dialog','ai-work-dialog');modal.id='ai-recipe-detail';modal.setAttribute('aria-labelledby','ai-modal-title');
    document.body.append(modal);
    let currentRecipe, currentView='detail', step=0, completed=new Set(), originFocus, closing=false;
    // 상세 정보 : 한번 조회한 예시 레시피 저장
    const detailCache=new Map();
    function openRecipe(id,view,opener){
        const recipe=model.recipes.find(item=>item.id===id&&item.public!==false&&!item.deleted);
        if(!recipe){status.textContent='없거나 비공개인 레시피입니다.';return;}
        if(!detailCache.has(id))detailCache.set(id,recipe);
        currentRecipe=detailCache.get(id);currentView=view;step=0;completed=new Set();originFocus=opener;closing=false;
        renderModal();modal.showModal();
    }
    function closeModal(){
        if(closing||!modal.open)return;closing=true;
        let finished=false;
        const finish=()=>{if(finished)return;finished=true;modal.close();closing=false;originFocus?.focus({preventScroll:true});};
        if(reduced())finish();else{
            const animation=modal.animate([{opacity:1,transform:'translateY(0)'},{opacity:0,transform:'translateY(8px)'}],{duration:180,easing:'ease-in'});
            animation.onfinish=finish;setTimeout(finish,230);
        }
    }
    modal.addEventListener('cancel',event=>{event.preventDefault();closeModal();});
    modal.addEventListener('click',event=>{
        if(event.target!==modal)return;
        const rect=modal.getBoundingClientRect();
        if(event.clientX<rect.left||event.clientX>rect.right||event.clientY<rect.top||event.clientY>rect.bottom)closeModal();
    });
    function renderModal(){
        modal.replaceChildren();
        const heading=el('div','ai-modal-heading');
        const close=closeIcon();close.addEventListener('click',closeModal);
        heading.append(el('span','',currentView==='cook'?'단계별 조리 · 예시':'레시피 상세 · 예시'),close);
        const content=el('div','ai-modal-body');modal.append(heading,content);
        if(currentView==='detail')renderDetail(content);else if(currentView==='cook')renderCook(content);else renderDone(content);
        modal.scrollTop=0;
    }
    function renderDetail(content){
        const recipe=currentRecipe;
        const top=el('div','ai-detail-top'), info=el('div');
        const title=el('h2','',recipe.title);title.id='ai-modal-title';
        info.append(el('p','ai-modal-source',sourceName(recipe)),title,el('p','ai-modal-description',recipe.description),facts(recipe,'ai-modal-facts'));
        top.append(photo(recipe),info);content.append(top);
        if(searchSelection.length){
            const comparison=model.compare(recipe,searchSelection),match=el('div','ai-detail-match');
            match.append(el('span','','선택한 주재료 · '+comparison.matched.map(model.name).join(' · ')),el('span','ai-missing',comparison.missing.length?'추가로 필요한 주재료 · '+comparison.missing.map(model.name).join(' · '):'비교한 주재료를 모두 선택했어요.'));
            content.append(match);
        }
        content.append(el('h3','','필요한 재료'));
        const amounts=el('ul','ai-amounts');recipe.amounts.forEach(item=>amounts.append(el('li','',item)));content.append(amounts);
        content.append(el('h3','','이렇게 만들어요'));
        if(recipe.steps.length){const steps=el('ol','ai-detail-steps');recipe.steps.forEach(item=>steps.append(el('li','',item)));content.append(steps);}
        else content.append(el('p','ai-modal-note','조리 단계가 제공되지 않았어요. 요리 시작 기능은 사용할 수 없습니다.'));
        const bottom=el('div','ai-modal-bottom');
        const cook=button('요리 시작','ai-modal-primary',()=>{currentView='cook';renderModal();});cook.disabled=!recipe.steps.length;
        bottom.append(el('p','ai-modal-note','디자인 예시 · 분량과 시간은 예시입니다.\n조리 단계 이미지는 없으므로 임의 사진을 넣지 않았어요.'),cook);content.append(bottom);
    }
    function renderCook(content){
        const recipe=currentRecipe;
        if(!recipe.steps.length){currentView='detail';renderModal();return;}
        const title=el('h2','',recipe.title);title.id='ai-modal-title';
        content.append(el('p','ai-cook-overline','COOKING · '+(step+1)+' / '+recipe.steps.length+' 단계'),title);
        const progress=el('div','ai-cook-progress');progress.setAttribute('aria-label',recipe.steps.length+'단계 중 '+completed.size+'단계 완료');
        recipe.steps.forEach((_,index)=>progress.append(el('span',completed.has(index)?'is-done':index===step?'is-current':'')));
        const stage=el('section','ai-cook-stage');stage.append(el('small','','STEP '+String(step+1).padStart(2,'0')),el('p','',recipe.steps[step]));
        let touchStart;
        stage.addEventListener('pointerdown',event=>{if(event.pointerType==='touch')touchStart={x:event.clientX,y:event.clientY};});
        stage.addEventListener('pointerup',event=>{
            if(!touchStart||event.pointerType!=='touch')return;
            const dx=event.clientX-touchStart.x,dy=event.clientY-touchStart.y;touchStart=null;
            if(Math.abs(dx)<55||Math.abs(dx)<Math.abs(dy)*1.5)return;
            if(dx<0&&step<recipe.steps.length-1)nextStep();else if(dx>0&&step>0){step--;renderModal();}
        });
        const nav=el('div','ai-cook-nav');
        const prev=button('이전 단계','ai-modal-secondary',()=>{step--;renderModal();});prev.disabled=step===0;
        const next=button(step===recipe.steps.length-1?'요리 완료':'완료하고 다음','ai-modal-primary',nextStep);
        nav.append(prev,next);
        content.append(progress,stage,nav,el('p','ai-cook-help','버튼 또는 좌우 스와이프로 이동해요. 마지막 단계에서는 요리 완료를 눌러주세요. 화면 확인용 고정 조리 예시이며 실제 API·AI 결과가 아닙니다.'));
        const back=button('레시피 상세로 돌아가기','ai-demo-button',()=>{currentView='detail';renderModal();});content.append(back);
    }
    function nextStep(){completed.add(step);if(step<currentRecipe.steps.length-1)step++;else currentView='done';renderModal();}
    function renderDone(content){
        const view=el('div','ai-done-view'),title=el('h2','','한 끼가 완성됐어요.');title.id='ai-modal-title';
        view.append(title,el('p','',currentRecipe.title+' · '+completed.size+'개 단계 완료\n디자인 시연이며 별도의 기록은 저장하지 않았습니다.'),button('검색 결과로 돌아가기','ai-modal-primary',closeModal));content.append(view);
    }
    document.addEventListener('pointerdown',event=>{
        if(modal.open)return;
        if (!event.target.closest('.ai-input-wrap')) hideOptions();
        if (open && !island.contains(event.target)) setOpen(false);
    });
    island.addEventListener('keydown',event=>{if(event.key==='Escape'&&!modal.open){event.preventDefault();setOpen(false);}});
    renderPicked();
    rotatePlaceholder();
})();
