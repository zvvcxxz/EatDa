window.globalStompClient = null;

document.addEventListener('DOMContentLoaded', () => {
    connectGlobalWebSocket();
});

function connectGlobalWebSocket() {
    if (window.globalStompClient && window.globalStompClient.connected) return;

    const socket = new SockJS('/ws-chat');
    window.globalStompClient = Stomp.over(socket);
    window.globalStompClient.debug = null;

    window.globalStompClient.connect({}, (frame) => {

        // 1:1 대화 초대 수신
        window.globalStompClient.subscribe('/user/queue/invite', async (messageOutput) => {
            const data = JSON.parse(messageOutput.body);
            if (data.type === 'CHAT_INVITE') {
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
            }
        });

        // 초대 요청을 보냈던 당사자가 결과를 수신
        window.globalStompClient.subscribe('/user/queue/inviteResponse', (messageOutput) => {
            const data = JSON.parse(messageOutput.body);
            if (data.type === 'INVITE_ACCEPTED') {
                showGlobalToast(data.message || '상대방이 대화 요청을 수락했습니다.', data.roomId);
                setTimeout(() => {
                    window.location.href = `/my/chat?roomId=${data.roomId}`;
                }, 500);
            } else if (data.type === 'INVITE_REJECTED') {
                showGlobalToast(data.message || '상대방이 채팅 요청을 거절했습니다.');
            }
        });

        // 개인 메시지 및 에러 수신
        window.globalStompClient.subscribe('/user/queue/private', (messageOutput) => {
            const message = JSON.parse(messageOutput.body);
            
            if (message.type === 'ERROR') {
                alert(message.content || '메세지를 보낼 수 없습니다. 나간 후 다시 초대해주세요.');
                if (typeof window.setChatInputEnabled === 'function') {
                    window.setChatInputEnabled(false);
                }
                return;
            }

            const currentRoomId = document.querySelector('.chat-box')?.dataset.roomId;
            if (message.type === 'TALK' && (!currentRoomId || String(currentRoomId) !== String(message.roomId))) {
                showGlobalToast(`${message.senderName}: ${message.content}`, message.roomId);
            }
        });

        if (typeof window.onWebSocketConnected === 'function') {
            window.onWebSocketConnected(window.globalStompClient);
        }

    }, (error) => {
        window.globalStompClient = null;
        setTimeout(connectGlobalWebSocket, 5000);
    });
}

function showGlobalToast(text, roomId) {
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
}