document.addEventListener("DOMContentLoaded", () => {
    const chatBox = document.getElementById("chatBox");
    const chatForm = document.getElementById("chatForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const suggestions = document.querySelectorAll(".suggestion");

    let isSending = false;

    function scrollToBottom() {
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function addMessage(text, type) {
        const message = document.createElement("div");
        message.className = `message ${type}`;

        if (type === "assistant") {
            const avatar = document.createElement("div");
            avatar.className = "message-avatar";

            const icon = document.createElement("i");
            icon.className = "fa-solid fa-robot";

            avatar.appendChild(icon);
            message.appendChild(avatar);
        }

        const content = document.createElement("div");
        content.className = "message-content";

        content.textContent = text;

        message.appendChild(content);
        chatBox.appendChild(message);
        scrollToBottom();

        return content;
    }

    async function sendMessage(question) {
        if (!question || isSending) {
            return;
        }

        isSending = true;
        sendButton.disabled = true;
        messageInput.disabled = true;

        addMessage(question, "user");

        const loadingMessage = addMessage(
            "답변을 불러오는 중입니다...",
            "assistant"
        );

        try {
            const response = await fetch(
                `/api/question?question=${encodeURIComponent(question)}`,
                {
                    method: "GET",
                    headers: {
                        "Accept": "text/plain, text/event-stream, */*"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP 오류: ${response.status}`);
            }

            if (!response.body) {
                throw new Error("응답 본문이 없습니다.");
            }

            loadingMessage.textContent = "";

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { value, done } = await reader.read();

                if (done) {
                    break;
                }

                loadingMessage.textContent += decoder.decode(
                    value,
                    { stream: true }
                );

                scrollToBottom();
            }

            loadingMessage.textContent += decoder.decode();

            if (!loadingMessage.textContent.trim()) {
                loadingMessage.textContent =
                    "답변이 비어 있습니다. 다시 질문해 주세요.";
            }

        } catch (error) {
            console.error("챗봇 오류:", error);
            loadingMessage.textContent =
                "답변을 가져오지 못했습니다. API 주소와 서버 로그를 확인해 주세요.";
        } finally {
            isSending = false;
            sendButton.disabled = false;
            messageInput.disabled = false;
            messageInput.focus();
        }
    }

    chatForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const question = messageInput.value.trim();

        if (!question) {
            return;
        }

        messageInput.value = "";
        sendMessage(question);
    });

    suggestions.forEach((button) => {
        button.addEventListener("click", () => {
            messageInput.value = button.textContent.trim();
            messageInput.focus();
        });
    });
});