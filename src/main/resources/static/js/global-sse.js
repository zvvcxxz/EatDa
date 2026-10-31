document.addEventListener('DOMContentLoaded', () => {
    initGlobalSse();
});

function initGlobalSse() {
    const eventSource = new EventSource('/api/notifications/subscribe');

    // 1. 최초 초기화 수신
    eventSource.addEventListener('INIT', (e) => {
        console.log('[SSE] 알림 구독 성공');
    });

    // 2. Heartbeat PING 수신
    eventSource.addEventListener('PING', (e) => {
        // 연결 유지용 PING
    });

    // 3. 1:1 대화 초대 수신 (수신자 B)
    eventSource.addEventListener('CHAT_INVITE', async (e) => {
        const data = JSON.parse(e.data);
        const accept = confirm(`${data.senderName}님에게 1:1 대화요청이 들어왔습니다.\n수락하시겠습니까?`);

        try {
            const response = await fetch('/mychat/respondInvite', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    roomId: String(data.roomId),
                    targetUserId: String(data.senderId),
                    accept: accept
                })
            });

            const result = await response.json();
            if (result.success && accept) {
                window.location.href = `/my/chat?roomId=${data.roomId}`;
            }
        } catch (err) {
            console.error("초대 응답 전송 중 오류:", err);
        }
    });

    // 4. 초대 결과 수신 (요청자 A - 상대가 수락하면 스레드 막힘 없이 바로 대화방 이동)
    eventSource.addEventListener('INVITE_RESPONSE', (e) => {
        const data = JSON.parse(e.data);
        if (data.type === 'INVITE_ACCEPTED') {
            window.showGlobalToast(data.message || '상대방이 대화 요청을 수락했습니다. 대화방으로 이동합니다.', data.roomId);
            setTimeout(() => {
                window.location.href = `/my/chat?roomId=${data.roomId}`;
            }, 800);
        } else if (data.type === 'INVITE_REJECTED') {
            window.showGlobalToast(data.message || '상대방이 채팅 요청을 거절했습니다.');
        }
    });

    // 5. 대화 메시지 토스트 알림 수신
    eventSource.addEventListener('TALK_TOAST', (e) => {
        const message = JSON.parse(e.data);
        const currentRoomId = document.querySelector('.chat-box')?.dataset.roomId;

        if (!currentRoomId || String(currentRoomId) !== String(message.roomId)) {
            window.showGlobalToast(`${message.senderName}: ${message.content}`, message.roomId);
        }
    });

    // 6. 에러 메시지 수신
    eventSource.addEventListener('ERROR_NOTICE', (e) => {
        const data = JSON.parse(e.data);
        window.showGlobalToast(data.content || '메세지를 보낼 수 없습니다.');
    });

    eventSource.onerror = (err) => {
        if (eventSource.readyState === EventSource.CLOSED) {
            console.log('[SSE] 연결이 종료되었습니다.');
        }
    };
	
	window.addEventListener('pagehide', () => {
	        if (eventSource) {
	            eventSource.close();
	        }
	        // 브라우저가 닫혀도 비동기로 서버에 종료 요청을 확실히 전달
	        navigator.sendBeacon('/api/notifications/unsubscribe');
	    });
}

// 전역에서 사용할 수 있도록 window 객체에 등록
window.showGlobalToast = function(text, roomId) {
    let container = document.getElementById('global-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'global-toast-container';
        container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px;';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.cssText = 'background: #3d3224; color: #ffffff; padding: 12px 20px; border-radius: 10px; font-size: 14px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); cursor: pointer; transition: transform 0.2s;';
    toast.textContent = text;

    if (roomId) {
        toast.onclick = () => {
            window.location.href = `/my/chat?roomId=${roomId}`;
        };
    }

    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
};