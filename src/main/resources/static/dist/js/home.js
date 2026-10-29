// 메인 냉장고 : 임박 재료 조회와 빠른 재료 선택
import { $, escapeHtml as e } from './common.js';

// 빠른 재료 조합기 : 선택한 재료 저장
const selected = new Map();

// 냉장고 안내 : 빈 목록 문구 생성

function empty(message) {
    return '<p class="empty">' + e(message) + '</p>';
}
// 선택 재료 : 삭제 버튼과 목록 표시
function renderSelection() {
    const markup = [...selected.values()]
        .map(
            (item) =>
                '<button class="tag" data-remove-ingredient="' +
                e(item.id) +
                '" aria-label="' +
                e(item.name) +
                ' 삭제">' +
                e(item.name) +
                ' ×</button>'
        )
        .join('');
    $('basket-selected').innerHTML = markup || '<em>위의 재료를 선택해보세요!</em>';
    // 선택 재료 : 위쪽 재료 버튼의 체크 상태도 같이 갱신
    document.querySelectorAll('#quick-ingredients [data-fridge-ingredient]').forEach((button) => {
        button.setAttribute('aria-pressed', String(selected.has(button.dataset.fridgeIngredient)));
    });
}

// 빠른 재료 조합기 : AI 검색창 연결 / 선택 초기화
$('basket-search').onclick = () => {
    if (!selected.size) {
        $('quick-status').textContent = '재료를 하나 이상 선택해주세요.';
        return;
    }

    document.dispatchEvent(
        new CustomEvent('ai-fridge-search', {
            detail: [...selected.values()]
        })
    );
};
$('basket-clear').onclick = () => {
    selected.clear();
    renderSelection();
};

// 선택한 재료 : 삭제 버튼 처리
document.addEventListener('click', (event) => {
    const remove = event.target.closest('[data-remove-ingredient]');
    if (!remove) return;
    selected.delete(remove.dataset.removeIngredient);
    renderSelection();
});

// 메인 냉장고 : 소비기한 임박 재료 조회
async function preview() {
    const target = $('fridge-items');

    if (!target) return;

    target.innerHTML = empty('재료를 불러오는 중입니다.');

    try {
        // 로그인한 회원의 냉장고 재료 요청
        const response = await fetch('/home/items', {
            headers: {
                Accept: 'application/json'
            }
        });

        // 비로그인 또는 로그인 페이지로 이동한 경우
        if (response.status === 401 || response.status === 403 || response.redirected) {
            target.innerHTML = empty('로그인 후 냉장고를 확인해보세요.');
            return;
        }

        if (!response.ok) {
            throw new Error('냉장고 조회 실패');
        }

        const data = await response.json();

        // 비로그인이면 안내 후 종료
        if (data.state === 'logout') {
            target.innerHTML = empty('로그인 후 냉장고를 확인해보세요.');
            return;
        }

        // 로그인 상태와 목록 형식 확인
        if (data.state !== 'login' || !Array.isArray(data.list)) {
            throw new Error('냉장고 응답 형식 오류');
        }

        const items = data.list;

        // 임박 재료만 선택 → 기한순 정렬 → 최대 4개
        const urgentItems = items
            .filter((item) => item.expiring === true)
            .sort((a, b) => a.expiresOn.localeCompare(b.expiresOn))
            .slice(0, 4);

        if (urgentItems.length === 0) {
            target.innerHTML = empty('3일 이내 기한이 오는 재료가 없습니다.');
            return;
        }

        // 냉장고 안에 재료 목록 출력
        target.innerHTML = urgentItems
            .map((item) => {
                const remaining = item.dDay === 0 ? '오늘까지' : `D-${item.dDay}`;

                return `
                <div class="list-row">
                    <span>
                        ${e(item.ingredientName)}
                        ${e(item.quantity ?? '')} ${e(item.unit || '')}
                    </span>
                    <span>${e(remaining)}</span>
                </div>
            `;
            })
            .join('');
    } catch (error) {
        console.error('메인 냉장고 조회 실패', error);
        target.innerHTML = empty('냉장고 정보를 불러오지 못했습니다.');
    }
}

// 빠른 재료 조합기 : 내 냉장고 재료 조회

async function loadFridgeIngredients() {
    const target = $('quick-ingredients');
    const status = $('quick-status');

    if (!target || !status) return;

    target.replaceChildren();
    status.textContent = '냉장고 재료를 불러오는 중입니다.';

    try {
        const response = await fetch('/home/items', {
            headers: {
                Accept: 'application/json'
            }
        });

        if (!response.ok || response.redirected) {
            throw new Error('냉장고 재료 조회 실패');
        }

        const data = await response.json();

        if (data.state === 'logout') {
            status.textContent = '로그인 후 내 냉장고 재료를 확인해보세요.';
            return;
        }

        if (data.state !== 'login' || !Array.isArray(data.list)) {
            throw new Error('냉장고 응답 형식 오류');
        }
        // 임박 재료만 선택하고 소비기한 빠른 순으로 정렬
        const urgentItems = data.list
            .filter(item => {
                return item.expiring === true &&
                item.expired !== true &&
                item.expiresOn
            })
            .sort((a,b) => {
                return a.expiresOn.localeCompare(b.expiresOn)
            });

        // 같은 재료는 소비기한이 가장 빠른 것만 남김
        const ingredients = new Map();

        urgentItems.forEach((item) => {
              if(item.ingredientId == null) return;

            const id = String(item.ingredientId);

            if(!ingredients.has(id)) {
                ingredients.set(id, {
                    id: item.ingredientId,
                    name : item.ingredientName
                });
            }
        });

        // 중복 제거 후 최대 10개 표시
        const visibleItems = [...ingredients.values()].slice(0 , 10);

        visibleItems.forEach(item => {
            const button = document.createElement("button");

            button.type = "button";
            button.className = "tag";
            button.textContent = item.name;
            button.dataset.fridgeIngredient = String(item.id);
            button.setAttribute('aria-pressed', String(selected.has(String(item.id))));

            button.onclick = () => {
                selected.set(String(item.id), item);
                renderSelection();
            };

            target.append(button);
        });

        status.textContent = visibleItems.length
            ? ''
            : '소비기한이 임박한 재료가 없습니다.';

    } catch (error) {
        console.error('빠른 재료 조합기 조회 실패', error);
        status.textContent = '냉장고 재료를 불러오지 못했습니다.';
    }
}
loadFridgeIngredients();
preview();

// 메인 냉장고 : 관리 페이지 이동 효과
import './home-fridge-door.js';
