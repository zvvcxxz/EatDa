document.addEventListener('DOMContentLoaded', () => {

    const chatWrapper = document.querySelector('.chat-wrapper');
    const chatBox = document.querySelector('.chat-box');
    const chatMessageArea = document.getElementById('chatMessageArea');
    const chatInput = document.getElementById('chatInput');
    const btnSend = document.getElementById('btnSend');

    //const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    //const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // 현재 사용자 ID 및 선택된 방 ID
    const currentMemberId = chatWrapper?.dataset.currentMemberId;
    const roomId = chatBox?.dataset.roomId;

    let stompClient = null;

    //초기화 및 웹소켓 연결
    if (chatBox && roomId) {
        connectStomp(roomId);
        scrollToBottom();
    }

    // STOMP 연결
    function connectStomp(roomId) {
        const socket = new SockJS('/ws-stomp');
        stompClient = Stomp.over(socket);

        // stompClient.debug = null;

        stompClient.connect({}, () => {
            console.log(`[STOMP] Connected to room: ${roomId}`);

            // /sub/chat/room/{roomId} 구독
            stompClient.subscribe(`/sub/chat/room/${roomId}`, (output) => {
                const messageDto = JSON.parse(output.body);
                appendMessageWithTemplate(messageDto);
                scrollToBottom();
            });
        }, (error) => {
            console.error('[STOMP] Connection error:', error);
        });
    }

    //메시지 전송
    btnSend?.addEventListener('click', sendMessage);
    chatInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });

    function sendMessage() {
        const content = chatInput.value.trim();
        if (!content || !stompClient) return;

        const chatMessage = {
            roomId: parseInt(roomId, 10),
            senderId: parseInt(currentMemberId, 10),
            messageType: 1, // 1: TEXT
            content: content
        };

        stompClient.send('/pub/chat/message', {}, JSON.stringify(chatMessage));
        chatInput.value = '';
        chatInput.focus();
    }

    //<template> 태그 활용한 메시지 출력
    function appendMessageWithTemplate(msg) {
        const template = document.getElementById('chatMessageTemplate');
        if (!template || !chatMessageArea) return;

        // 템플릿 복사 Deep Clone
        const clone = template.content.cloneNode(true);
        const groupDiv = clone.querySelector('.message-group');
        const senderSpan = clone.querySelector('.sender-name');
        const bubbleDiv = clone.querySelector('.message-bubble');
        const timeSpan = clone.querySelector('.message-time');

        const isMyMsg = String(msg.senderId) === String(currentMemberId);

        if (isMyMsg) {
            groupDiv.classList.add('my-message');
            if (senderSpan) senderSpan.remove(); // 내 메시지일 때는 닉네임 영역 제거
        } else {
            groupDiv.classList.add('other-message');
            // 상대방 닉네임 설정 (STOMP DTO에 닉네임이 없으면 대화창 헤더의 닉네임 사용)
            const targetNicknameHeader = document.querySelector('.chat-target strong')?.textContent;
            if (senderSpan) {
                senderSpan.textContent = msg.senderNickname || targetNicknameHeader || '상대방';
            }
        }

        bubbleDiv.textContent = msg.content;
        timeSpan.textContent = msg.createdAtText || getCurrentTimeString();

        chatMessageArea.appendChild(clone);
    }

    // 대화창 하단 자동 스크롤
    function scrollToBottom() {
        if (chatMessageArea) {
            chatMessageArea.scrollTop = chatMessageArea.scrollHeight;
        }
    }

    // 시간 포맷 생성기 (HH:mm)
    function getCurrentTimeString() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    //회원 검색 모달 및 1:1 방 생성 
    const openSearchModalBtn = document.getElementById('openSearchModalBtn');
    const closeSearchModalBtn = document.getElementById('closeSearchModalBtn');
    const memberSearchModal = document.getElementById('memberSearchModal');
    const searchKeywordInput = document.getElementById('searchKeyword');
    const btnSearchMember = document.getElementById('btnSearchMember');
    const searchResultList = document.getElementById('searchResultList');

    // 모달 열기/닫기
    openSearchModalBtn?.addEventListener('click', () => {
        if (memberSearchModal) memberSearchModal.style.display = 'flex';
        searchKeywordInput?.focus();
    });

    closeSearchModalBtn?.addEventListener('click', closeModal);

    // 모달 바깥 영역 클릭 시 닫기
    memberSearchModal?.addEventListener('click', (e) => {
        if (e.target === memberSearchModal) closeModal();
    });

    function closeModal() {
        if (memberSearchModal) memberSearchModal.style.display = 'none';
    }

    // 회원 검색 (엔터키 및 버튼 클릭)
    btnSearchMember?.addEventListener('click', searchMembers);
    searchKeywordInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchMembers();
        }
    });

    async function searchMembers() {
        const keyword = searchKeywordInput.value.trim();
        if (!keyword) {
            alert('검색할 닉네임을 입력해 주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/members/search?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) throw new Error('검색 실패');

            const members = await response.json();
            renderSearchResults(members);
        } catch (error) {
            console.error('회원 검색 중 오류 발생:', error);
            alert('회원 목록을 불러오는 중 오류가 발생했습니다.');
        }
    }

    // 검색 결과 목록 출력
    function renderSearchResults(members) {
        if (!searchResultList) return;
        searchResultList.innerHTML = '';

        if (!members || members.length === 0) {
            searchResultList.innerHTML = '<li class="empty-text muted">검색 결과가 없습니다.</li>';
            return;
        }

        members.forEach(member => {
            const li = document.createElement('li');
            li.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #f0f0f0;';

            const nameSpan = document.createElement('span');
            nameSpan.textContent = member.nickname;

            const chatBtn = document.createElement('button');
            chatBtn.type = 'button';
            chatBtn.className = 'btn btn-primary btn-small';
            chatBtn.textContent = '대화하기';
            chatBtn.addEventListener('click', () => createOrGetChatRoom(member.memberId));

            li.appendChild(nameSpan);
            li.appendChild(chatBtn);
            searchResultList.appendChild(li);
        });
    }

    // 1:1 방 생성 및 이동 API 연동
    async function createOrGetChatRoom(targetMemberId) {
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        try {
            const response = await fetch('/api/chat/room', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ targetMemberId: targetMemberId })
            });

            if (!response.ok) throw new Error('채팅방 생성 실패');

            const data = await response.json(); // { roomId: 123 }
            // 해당 채팅방으로 페이지 이동
            window.location.href = `/my/chat?roomId=${data.roomId}`;
        } catch (error) {
            console.error('채팅방 이동 중 오류 발생:', error);
            alert('채팅방을 생성하거나 이동할 수 없습니다.');
        }
    }
});