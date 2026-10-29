// 메인 배경 : 화면에서 벗어나거나 다른 탭을 보면 효과 정지
(() => {
    const hero = document.querySelector('.home-main .hero');
    if (!hero) return;
    let visible = true;
    const update = () => hero.classList.toggle('hero-motion-paused', document.hidden || !visible);
    const observer = new IntersectionObserver(([entry]) => {
        visible = entry.isIntersecting;
        update();
    });
    observer.observe(hero);
    document.addEventListener('visibilitychange', update);
    update();
})();
