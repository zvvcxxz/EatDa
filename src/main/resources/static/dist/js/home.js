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

        // 같은 재료를 여러 번 등록했어도 선택 버튼은 하나만 표시
        const ingredients = new Map();

        data.list.forEach((item) => {
            ingredients.set(String(item.ingredientId), {
                id: item.ingredientId,
                name: item.ingredientName
            });
        });

        ingredients.forEach((item) => {
            const button = document.createElement('button');

            button.type = 'button';
            button.className = 'tag';
            button.textContent = item.name;

            button.onclick = () => {
                selected.set(String(item.id), item);
                renderSelection();
            };

            target.append(button);
        });

        status.textContent = ingredients.size ? '' : '냉장고에 등록된 재료가 없습니다.';
    } catch (error) {
        console.error('빠른 재료 조합기 조회 실패', error);
        status.textContent = '냉장고 재료를 불러오지 못했습니다.';
    }
}
loadFridgeIngredients();
preview();

// 메인 냉장고 : 관리 페이지 이동 효과
import './home-fridge-door.js';
