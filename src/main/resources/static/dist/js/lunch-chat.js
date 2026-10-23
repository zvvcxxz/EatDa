document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('#lunch-form');
    const messageInput = document.querySelector('#lunch-message');
    const messages = document.querySelector('#lunch-messages');
    const status = document.querySelector('#lunch-status');

    const budget = document.querySelector('#lunch-budget');
    const category = document.querySelector('#lunch-category');
    const spicy = document.querySelector('#lunch-spicy');
    const exclude = document.querySelector('#lunch-exclude');

    const sendButton = document.querySelector('#lunch-send');
    const randomButton = document.querySelector('#lunch-random');
    const resetButton = document.querySelector('#lunch-reset');
    const suggestionButtons = document.querySelectorAll('[data-message]');

    const welcomeMessage = messages.firstElementChild.cloneNode(true);
    let sending = false;

    // 질문과 답변 말풍선 추가
    function addMessage(content, user) {
        const message = document.createElement('div');
        message.className = user
            ? 'lunch-message lunch-message-user'
            : 'lunch-message lunch-message-bot';

        if (!user) {
            const avatar = document.createElement('span');
            avatar.className = 'lunch-message-avatar';
            avatar.textContent = '🍳';
            avatar.setAttribute('aria-hidden', 'true');
            message.appendChild(avatar);
        }

        const bubble = document.createElement('div');
        bubble.className = 'lunch-message-bubble';
        bubble.textContent = content;

        message.appendChild(bubble);
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;

        return bubble;
    }

    // 답변을 받는 동안 중복 전송 방지
    function setSending(value) {
        sending = value;
        messageInput.disabled = value;
        sendButton.disabled = value;
        randomButton.disabled = value;
        resetButton.disabled = value;

        suggestionButtons.forEach(function (button) {
            button.disabled = value;
        });
    }

    async function sendMessage(question, random = false) {
        question = question.trim();

        if (sending || !question) {
            return;
        }

        const budgetText = budget.value === '0'
            ? '상관없음'
            : Number(budget.value).toLocaleString() + '원 이하';

        // 점심 뽑기는 이번 요청에만 적용
        const requestType = random
            ? '점심 뽑기 요청입니다. 선택 조건에 맞는 메뉴 하나와 이유만 알려주세요.'
            : '일반 질문입니다. 아래 질문에 맞춰 답하세요. 메뉴 개수를 지정하지 않은 추천 요청이면 메뉴 3개를 추천하세요.';

        const userMessage = [
            '요청 방식: ' + requestType,
            '예산: ' + budgetText,
            '음식 종류: ' + category.value,
            '매운맛: ' + spicy.value,
            '제외 재료: ' + (exclude.value.trim() || '없음'),
            '질문: ' + question
        ].join('\n');

        addMessage(question, true);
        messageInput.value = '';

        const answerBubble = addMessage('추천 메뉴를 생각하고 있어요…', false);
        let answer = '';

        setSending(true);
        status.textContent = '답변을 작성하고 있어요.';

        try {
            const url = messages.dataset.chatUrl
                + '?message=' + encodeURIComponent(userMessage);

            const response = await fetch(url, {
                headers: {
                    'Accept': 'text/event-stream'
                }
            });

            const contentType = response.headers.get('content-type') || '';

            if (!response.ok || response.redirected || !response.body
                    || !contentType.includes('text/event-stream')) {
                throw new Error('답변 요청 실패');
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let buffer = '';

            // 나누어 도착한 데이터를 모아 답변 부분만 읽기
            function readEvents() {
                let boundary;

                while ((boundary = buffer.match(/\r?\n\r?\n/)) !== null) {
                    const event = buffer.slice(0, boundary.index);
                    buffer = buffer.slice(
                        boundary.index + boundary[0].length
                    );

                    const data = event.split(/\r?\n/)
                        .filter(function (line) {
                            return line.startsWith('data:');
                        })
                        .map(function (line) {
                            const value = line.slice(5);
                            return value.startsWith(' ')
                                ? value.slice(1)
                                : value;
                        })
                        .join('\n');

                    answer += data;
                    answerBubble.textContent = answer;
                    messages.scrollTop = messages.scrollHeight;
                }
            }

            // AI 답변을 조금씩 읽어서 화면에 표시
            while (true) {
                const result = await reader.read();

                if (result.done) {
                    buffer += decoder.decode();
                    readEvents();
                    break;
                }

                buffer += decoder.decode(result.value, { stream: true });
                readEvents();
            }

            if (!answer.trim()) {
                throw new Error('답변 없음');
            }

            status.textContent = '';
        } catch (e) {
            console.error('점심 추천 오류:', e);

            if (!answer) {
                answerBubble.textContent =
                    '추천을 받지 못했어요. 잠시 후 다시 시도해 주세요.';
            }

            status.textContent = '답변 수신에 실패했습니다. 다시 시도해 주세요.';
        } finally {
            setSending(false);
            messageInput.focus();
        }
    }

    // 직접 입력은 일반 질문으로 전송
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        sendMessage(messageInput.value);
    });

    // 추천 질문 버튼
    suggestionButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            sendMessage(button.dataset.message);
        });
    });

    // 점심 뽑기 요청만 true로 전달
    randomButton.addEventListener('click', function () {
        sendMessage('선택한 조건에 맞는 점심 메뉴를 하나만 골라줘.', true);
    });

    // 화면의 대화 내용 초기화
    resetButton.addEventListener('click', function () {
        messages.replaceChildren(welcomeMessage.cloneNode(true));
        status.textContent = '';
        messageInput.value = '';
        messageInput.focus();
    });
	
	// 대화는 유지하고 선택 조건만 초기화
	const optionsResetButton = document.querySelector('#lunch-options-reset');

	optionsResetButton.addEventListener('click', function () {
	    budget.value = '0';
	    category.value = '상관없음';
	    spicy.value = '상관없음';
	    exclude.value = '';

	    budget.focus();
	});
});