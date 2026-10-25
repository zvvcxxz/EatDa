// 전역 STOMP 클라이언트 객체
window.globalStompClient = null;

document.addEventListener('DOMContentLoaded', () => {
    connectGlobalWebSocket();
});

function connectGlobalWebSocket() {
    if (window.globalStompClient && window.globalStompClient.connected) {
        return;
    }

    const socket = new SockJS('/ws-chat');
    window.globalStompClient = Stomp.over(socket);

    // 디버그 로그 끄기 (필요시 주석 처리)
    window.globalStompClient.debug = null;

    window.globalStompClient.connect({}, (frame) => {
        console.log('[Global WebSocket] Connected:', frame);

        // 1. 어느 페이지에 있든 실시간 대화 초대 수신 (/user/queue/invite)
        window.globalStompClient.subscribe('/user/queue/invite', (messageOutput) => {
            const data = JSON.parse(messageOutput.body);
            if (data.type === 'CHAT_INVITE') {
                const confirmAnswer = confirm(`${data.senderName}님에게 1:1 대화요청이 들어왔습니다.\n수락하시겠습니까?`);
                if (confirmAnswer) {
                    window.location.href = `/my/chat?roomId=${data.roomId}`;
                }
            }
        });

        // 2. 현재 페이지가 채팅 페이지일 경우 대화방 전용 이벤트 바인딩
        if (typeof window.onWebSocketConnected === 'function') {
            window.onWebSocketConnected(window.globalStompClient);
        }

    }, (error) => {
        console.error('[Global WebSocket] 연결 실패:', error);
        window.globalStompClient = null;
        setTimeout(connectGlobalWebSocket, 5000);
    });
}