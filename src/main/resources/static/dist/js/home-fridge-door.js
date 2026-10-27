// 메인 냉장고 : 3D 문 열기와 관리 페이지 이동
// 메인 냉장고 : 문이 열리면 관리 페이지로 이동
const fridge = document.querySelector('.mf-fridge');
const door = fridge?.querySelector('.mf-door');
const link = door?.querySelector('.mf-actions a');
let opening = false;
let animation;
let scene;
function resetDoor() {
    opening = false;
    animation?.cancel();
    scene?.remove();
    fridge?.classList.remove('mf-opening');
    link?.removeAttribute('aria-disabled');
}
link?.addEventListener('click', (event) => {
    if (
        event.button !== 0 ||
        event.ctrlKey ||
        event.metaKey ||
        event.shiftKey ||
        event.altKey ||
        link.target === '_blank'
    )
        return;
    if (matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    event.preventDefault();
    if (opening) return;
    opening = true;
    link.setAttribute('aria-disabled', 'true');
    scene = document.createElement('div');
    scene.className = 'mf-door-scene';
    scene.setAttribute('aria-hidden', 'true');
    scene.inert = true;
    scene.style.top = `${door.offsetTop}px`;
    scene.style.height = `${door.offsetHeight}px`;
    const pivot = document.createElement('div');
    pivot.className = 'mf-door-pivot';
    const front = door.cloneNode(true);
    front.classList.add('mf-door-front');
    front.querySelectorAll('[id]').forEach((node) => {
        if (node.id === 'fridge-items') node.classList.add('mf-items-copy');
        node.removeAttribute('id');
    });
    const back = document.createElement('div');
    back.className = 'mf-door-back';
    for (let i = 0; i < 3; i++) back.append(document.createElement('span'));
    const edge = document.createElement('div');
    edge.className = 'mf-door-edge';
    pivot.append(front, back, edge);
    scene.append(pivot);
    fridge.append(scene);
    fridge.classList.add('mf-opening');
    animation = pivot.animate([{ transform: 'rotateY(0deg)' }, { transform: 'rotateY(112deg)' }], {
        duration: 650,
        easing: 'cubic-bezier(.25,.65,.25,1)',
        fill: 'forwards'
    });
    animation.finished
        .then(() => {
            if (opening) window.location.assign(link.href);
        })
        .catch(() => {});
});
// 뒤로 돌아오면 원래 상태로 표시
window.addEventListener('pageshow', resetDoor);
window.addEventListener('pagehide', resetDoor);
