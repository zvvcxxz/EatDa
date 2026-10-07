// 홈 진입 시 브랜드 문구를 보여주고 자동 종료합니다. API/로그인과 무관합니다.
const dialog = document.getElementById("intro-dialog");
const lines = ["냉장고를 열고,", "한 끼를 잇다."];
const first = document.getElementById("intro-line-one");
const second = document.getElementById("intro-line-two");
let typing, start, hold, safety, fade;
let closed = false;
function skip() {
  if (closed) return;
  closed = true;
  clearInterval(typing);
  clearTimeout(start);
  clearTimeout(hold);
  clearTimeout(safety);
  document.removeEventListener("keydown", skip);
  dialog.classList.add("intro-leaving");
  fade = setTimeout(() => dialog.close(), 350);
}
if (dialog && first && second) {
  dialog.showModal();
  document.getElementById("intro-skip").onclick = skip;
  document.addEventListener("keydown", skip);
  dialog.addEventListener("cancel", (event) => { event.preventDefault(); skip(); });
  dialog.addEventListener("close", () => {
    closed = true;
    clearInterval(typing);
    [start, hold, safety, fade].forEach(clearTimeout);
    document.removeEventListener("keydown", skip);
  });
  // 요청한 브랜드 타이핑은 OS의 동작 줄이기 설정에서도 생략하지 않습니다.
  // 글자 자체만 추가하며 이동/확대 효과는 사용하지 않습니다.
  first.textContent = "";
  second.textContent = "";
  start = setTimeout(() => {
    let index = 0;
    typing = setInterval(() => {
      index++;
      first.textContent = lines[0].slice(0, index);
      second.textContent = lines[1].slice(0, Math.max(0, index - lines[0].length));
      if (index >= lines[0].length + lines[1].length) {
        clearInterval(typing);
        hold = setTimeout(skip, 1000);
      }
    }, 150);
  }, 400);
  // 애니메이션 처리 실패로 홈이 계속 가려지는 상황을 방지합니다.
  safety = setTimeout(skip, 8000);
}
