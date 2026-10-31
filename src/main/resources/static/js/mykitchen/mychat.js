document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const chatWrapper = document.querySelector('.chat-wrapper');
    const currentMemberId = chatWrapper ? chatWrapper.dataset.currentMemberId : null;
    const currentMemberName = chatWrapper ? chatWrapper.dataset.currentMemberName : '익명';
    
    const chatBox = document.querySelector('.chat-box');
    const currentRoomId = chatBox ? chatBox.dataset.roomId : null;
    const targetMemberId = chatBox ? chatBox.dataset.targetMemberId : null;

    const chatMessageArea = document.getElementById('chatMessageArea');
    const chatInput = document.getElementById('chatInput');
    const btnSend = document.getElementById('btnSend');
    const leaveRoomBtn = document.getElementById('leaveRoomBtn');

    const searchModal = document.getElementById('memberSearchModal');
    const openSearchModalBtn = document.getElementById('openSearchModalBtn');
    const closeSearchModalBtn = document.getElementById('closeSearchModalBtn');
    const searchKeywordInput = document.getElementById('searchKeyword');
    const btnSearchMember = document.getElementById('btnSearchMember');
    const searchResultList = document.getElementById('searchResultList');

    let stompClient = null;
    let isRoomDisabled = false;

    // 채팅 전용 독립 WebSocket 연결
    function connectChatWebSocket() {
        if (!currentRoomId) return;

        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, () => {
            stompClient.subscribe(`/topic/chat/${currentRoomId}`, (messageOutput) => {
                const message = JSON.parse(messageOutput.body);

                // 대화방을 연 채로 동일 상대에게 다시 초대를 받은 경우
                if (message.type === 'CHAT_INVITE') {
                    const accept = confirm(`${message.senderName}님에게 1:1 대화요청이 들어왔습니다.\n수락하시겠습니까?`);
                    if (accept) {
                        fetch('/mychat/respondInvite', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                roomId: String(message.roomId),
                                targetUserId: String(message.senderId),
                                accept: true
                            })
                        }).then(() => {
                            window.location.reload();
                        });
                    }
                    return;
                }

                // 상대방이 나가기/로그아웃/종료 시 대화창 비활성화
                if (message.type === 'LEAVE') {
                    appendSystemNotice(message.content || "상대가 방을 나갔습니다.");
                    if (String(message.senderId) !== String(currentMemberId)) {
                        window.setChatInputEnabled(false);
                    }
                } else if (message.type === 'TALK') {
                    appendMessage(message);
                    scrollToBottom();
                }
            });
        }, (error) => {
            console.error('채팅 웹소켓 연결 에러:', error);
            setTimeout(connectChatWebSocket, 5000);
        });
    }

    // 진짜 '나가기' 버튼 클릭
    if (leaveRoomBtn) {
        leaveRoomBtn.addEventListener('click', () => {
            if (!confirm('대화방을 나가시겠습니까?')) return;

            if (stompClient && stompClient.connected && currentRoomId) {
                const leaveMessage = {
                    type: 'LEAVE',
                    roomId: String(currentRoomId),
                    senderId: Number(currentMemberId),
                    senderName: currentMemberName,
                    content: `${currentMemberName}님이 대화방을 나갔습니다.`
                };

                stompClient.send(`/app/chat.sendMessage/${currentRoomId}`, {}, JSON.stringify(leaveMessage));
            }

            window.location.href = '/my/chat';
        });
    }

    function appendSystemNotice(text) {
        if (!chatMessageArea) return;
        const noticeDiv = document.createElement('div');
        noticeDiv.className = 'system-notice';
        noticeDiv.style.cssText = 'text-align: center; margin: 10px 0; color: #857868; font-size: 13px; font-weight: bold; background: #f0e8dc; padding: 6px 12px; border-radius: 12px;';
        noticeDiv.textContent = text;
        chatMessageArea.appendChild(noticeDiv);
        scrollToBottom();
    }

    window.setChatInputEnabled = function(enabled) {
        isRoomDisabled = !enabled;
        if (chatInput) {
            chatInput.disabled = !enabled;
            chatInput.placeholder = enabled ? "메시지를 입력하세요..." : "메세지를 보낼 수 없습니다. 나간 후 다시 초대해주세요";
        }
        if (btnSend) {
            btnSend.disabled = !enabled;
        }
    };

    function sendMessage() {
        if (isRoomDisabled || (chatInput && chatInput.disabled)) {
            if (window.showGlobalToast) {
                window.showGlobalToast('메세지를 보낼 수 없습니다. 나간 후 다시 초대해주세요');
            } else {
                alert('메세지를 보낼 수 없습니다. 나간 후 다시 초대해주세요');
            }
            return;
        }

        const content = chatInput.value.trim();
        if (!content) return;

        if (!stompClient || !stompClient.connected) {
            if (window.showGlobalToast) {
                window.showGlobalToast('채팅 서버에 연결되어 있지 않습니다.');
            }
            return;
        }

        const chatMessage = {
            type: 'TALK',
            roomId: String(currentRoomId),
            senderId: Number(currentMemberId),
            senderName: currentMemberName,
            receiver: String(targetMemberId),
            content: content,
            createdAtText: formatCurrentTime()
        };

        stompClient.send(`/app/chat.sendMessage/${currentRoomId}`, {}, JSON.stringify(chatMessage));
        chatInput.value = '';
        chatInput.focus();
    }

    function appendMessage(msg) {
        if (!chatMessageArea) return;

        const template = document.getElementById('chatMessageTemplate');
        if (!template) return;

        const clone = template.content.cloneNode(true);
        const messageGroup = clone.querySelector('.message-group');
        const senderName = clone.querySelector('.sender-name');
        const messageBubble = clone.querySelector('.message-bubble');
        const messageTime = clone.querySelector('.message-time');

        const isMyMessage = String(msg.senderId) === String(currentMemberId);

        if (isMyMessage) {
            messageGroup.classList.add('my-message');
            if (senderName) senderName.remove();
        } else {
            messageGroup.classList.add('other-message');
            if (senderName) senderName.textContent = msg.senderName || '상대방';
        }

        messageBubble.textContent = msg.content || '';
        messageTime.textContent = msg.createdAtText || formatCurrentTime();

        chatMessageArea.appendChild(clone);
    }

    function scrollToBottom() {
        if (chatMessageArea) {
            requestAnimationFrame(() => {
                chatMessageArea.scrollTop = chatMessageArea.scrollHeight;
            });
        }
    }

    function formatCurrentTime() {
        const now = new Date();
        let hours = now.getHours();
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const ampm = hours >= 12 ? '오후' : '오전';
        hours = hours % 12 || 12;
        return `${ampm} ${hours}:${minutes}`;
    }

    async function searchMembers() {
        const keyword = searchKeywordInput.value.trim();
        if (!keyword) {
            if (window.showGlobalToast) window.showGlobalToast('검색어를 입력해 주세요.');
            return;
        }

        try {
            const response = await fetch(`/mychat/searchMember?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) throw new Error('회원 검색 실패');

            const members = await response.json();
            renderSearchResults(members);
        } catch (error) {
            console.error('검색 오류:', error);
            searchResultList.innerHTML = `<li class="empty-text">검색 중 오류가 발생했습니다.</li>`;
        }
    }

    function renderSearchResults(members) {
        searchResultList.innerHTML = '';

        if (!Array.isArray(members) || members.length === 0) {
            searchResultList.innerHTML = `<li class="empty-text">검색 결과가 없습니다.</li>`;
            return;
        }

        const filtered = members.filter(m => String(m.member_id) !== String(currentMemberId));

        if (filtered.length === 0) {
            searchResultList.innerHTML = `<li class="empty-text">검색된 회원이 없거나 본인입니다.</li>`;
            return;
        }

        filtered.forEach(member => {
            const li = document.createElement('li');
            li.className = 'search-result-item';

            li.innerHTML = `
                <div class="user-info">
                    <strong>${escapeHtml(member.name)}</strong>
                    <small class="muted">(${escapeHtml(member.login_id)})</small>
                </div>
                <button type="button" class="btn btn-primary btn-small start-chat-btn" data-target-id="${member.member_id}">
                    대화하기
                </button>
            `;

            searchResultList.appendChild(li);
        });
    }

    // ★ 대화 신청 (alert 대신 토스트 사용으로 스레드 차단 방지)
    async function startChat(targetMemberId) {
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            const response = await fetch('/mychat/room', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ targetMemberId: targetMemberId })
            });

            if (!response.ok) {
                throw new Error(`채팅방 생성 실패 (Status: ${response.status})`);
            }

            const data = await response.json();

            // 모달 닫기
            if (searchModal) searchModal.style.display = 'none';

            if (data.exists) {
                if (window.showGlobalToast) window.showGlobalToast(data.message || '이미 대화하고 있는 대화방이 존재합니다.');
                return;
            }

            if (data.isOnline === false) {
                if (window.showGlobalToast) window.showGlobalToast(data.message || '상대방이 현재 오프라인 상태입니다.');
                return;
            }

            // ★ 알림창(alert) 대신 부드러운 토스트 메시지 출력
            if (data.status === 'PENDING') {
                if (window.showGlobalToast) {
                    window.showGlobalToast(data.message || '상대방에게 대화 요청을 보냈습니다. 응답을 기다리는 중입니다.');
                }
            }
        } catch (error) {
            console.error('1:1 채팅 시작 오류:', error);
            if (window.showGlobalToast) window.showGlobalToast('채팅방 생성 중 오류가 발생했습니다.');
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;");
    }

    if (btnSend) btnSend.addEventListener('click', sendMessage);
    if (chatInput) {
        chatInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }

    if (openSearchModalBtn) {
        openSearchModalBtn.addEventListener('click', () => {
            searchModal.style.display = 'flex';
            searchKeywordInput.value = '';
            searchResultList.innerHTML = `<li class="empty-text">검색어를 입력 후 검색 버튼을 눌러주세요.</li>`;
            searchKeywordInput.focus();
        });
    }

    if (closeSearchModalBtn) {
        closeSearchModalBtn.addEventListener('click', () => {
            searchModal.style.display = 'none';
        });
    }

    if (searchModal) {
        searchModal.addEventListener('click', (e) => {
            if (e.target === searchModal) {
                searchModal.style.display = 'none';
            }
        });
    }

    if (btnSearchMember) btnSearchMember.addEventListener('click', searchMembers);
    if (searchKeywordInput) {
        searchKeywordInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchMembers();
            }
        });
    }

    if (searchResultList) {
        searchResultList.addEventListener('click', (e) => {
            const btn = e.target.closest('.start-chat-btn');
            if (btn) {
                const targetId = btn.dataset.targetId;
                startChat(targetId);
            }
        });
    }

    if (currentRoomId) {
        scrollToBottom();
        connectChatWebSocket();
    }
});