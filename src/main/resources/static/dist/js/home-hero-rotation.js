(() => {
    const title=document.querySelector('.home-main .hero h1');
    if(!title)return;
    // 메인 문구 : 일정 간격으로 문구 교체
    const phrases=[
        ['냉장고를 열고,','한 끼를'],
        ['남은 재료로,','새 맛을'],
        ['나만의 요리로,','일상을'],
        ['한 접시 나누고,','마음을']
    ];
    const reduced=matchMedia('(prefers-reduced-motion: reduce)');
    title.setAttribute('aria-label','냉장고를 열고, 한 끼를 잇다.');
    title.classList.add('hero-rotating-title');
    title.replaceChildren();
    const topRow=document.createElement('span');topRow.className='hero-top-words';
    const bottomRow=document.createElement('span');bottomRow.className='hero-bottom-row';
    const prefix=document.createElement('span');prefix.className='hero-prefix-words';
    const fixed=document.createElement('span');fixed.className='hero-fixed-word';fixed.textContent='잇다.';
    [topRow,bottomRow].forEach(node=>node.setAttribute('aria-hidden','true'));
    bottomRow.append(prefix,fixed);title.append(topRow,bottomRow);
    const frames=phrases.map(([first,second],index)=>{
        const top=document.createElement('span');top.className='hero-phrase-top';top.textContent=first;
        const bottom=document.createElement('span');bottom.className='hero-phrase-bottom';bottom.textContent=second;
        top.style.visibility=bottom.style.visibility=index?'hidden':'visible';
        topRow.append(top);prefix.append(bottom);return [top,bottom];
    });
    let current=0,timer,animations=[];
    function resetAnimations(){animations.forEach(a=>a.cancel());animations=[];frames.forEach((frame,index)=>frame.forEach(node=>node.style.visibility=index===current?'visible':'hidden'));}
    function rotate(){
        resetAnimations();
        const old=frames[current];current=(current+1)%frames.length;
        const next=frames[current];next.forEach(node=>node.style.visibility='visible');
        old.forEach(node=>animations.push(node.animate([{opacity:1,transform:'translateY(0)'},{opacity:0,transform:'translateY(-22px)'}],{duration:450,easing:'cubic-bezier(.4,0,.2,1)',fill:'forwards'})));
        next.forEach(node=>animations.push(node.animate([{opacity:0,transform:'translateY(24px)'},{opacity:1,transform:'translateY(0)'}],{duration:700,easing:'cubic-bezier(.22,1,.36,1)',fill:'both',delay:100})));
        animations[animations.length-1].onfinish=resetAnimations;
    }
    function schedule(){clearInterval(timer);resetAnimations();if(!document.hidden&&!reduced.matches)timer=setInterval(rotate,4800);}
    document.addEventListener('visibilitychange',schedule);
    reduced.addEventListener('change',schedule);
    schedule();
})();
