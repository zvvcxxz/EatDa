document.addEventListener("DOMContentLoaded", () => {
    const chatBox = document.getElementById("chatBox");
    const chatForm = document.getElementById("chatForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const suggestions = document.querySelectorAll(".suggestion");

    if (!chatBox || !chatForm || !messageInput || !sendButton) {
        console.error("챗봇 HTML 요소를 찾을 수 없습니다.");
        return;
    }

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

    function parseSSEEvent(eventText) {
        const lines = eventText.split("\n");
        const dataLines = [];

        for (const line of lines) {
            if (line.startsWith("data:")) {
                let data = line.slice(5);

                if (data.startsWith(" ")) {
                    data = data.slice(1);
                }

                dataLines.push(data);
            }
        }

        return dataLines.length > 0
            ? dataLines.join("\n")
            : null;
    }

    async function sendMessage(question) {
        if (!question || isSending) return;

        isSending = true;
        sendButton.disabled = true;
        messageInput.disabled = true;

        addMessage(question, "user");

        const answerElement = addMessage(
            "답변을 불러오는 중입니다...",
            "assistant"
        );

        try {
            const response = await fetch(
                `/api/question?question=${encodeURIComponent(question)}`,
                {
                    method: "GET",
                    headers: {
                        "Accept": "text/event-stream, text/plain, */*"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP 오류: ${response.status}`);
            }

            if (!response.body) {
                throw new Error("서버 응답이 없습니다.");
            }

            const contentType =
                response.headers.get("content-type") || "";

            const isSSE =
                contentType.includes("text/event-stream");

            const reader = response.body.getReader();
            const decoder = new TextDecoder("utf-8");

            let answer = "";
            let buffer = "";

            answerElement.textContent = "";

            function appendAnswer(text) {
                if (!text || text === "[DONE]") return;

                answer += text;
                answerElement.textContent = answer;
                scrollToBottom();
            }

            function processSSEBuffer() {
                let boundary;

                while ((boundary = buffer.indexOf("\n\n")) !== -1) {
                    const eventText = buffer.slice(0, boundary);
                    buffer = buffer.slice(boundary + 2);

                    const data = parseSSEEvent(eventText);

                    if (data !== null) {
                        appendAnswer(data);
                    }
                }
            }

            while (true) {
                const { value, done } = await reader.read();

                if (done) break;

                const chunk = decoder.decode(value, {
                    stream: true
                });

                if (isSSE) {
                    buffer += chunk;

                    buffer = buffer
                        .replace(/\r\n/g, "\n")
                        .replace(/\r/g, "\n");

                    processSSEBuffer();
                } else {
                    appendAnswer(chunk);
                }
            }

            const lastChunk = decoder.decode();

            if (isSSE) {
                buffer += lastChunk;

                buffer = buffer
                    .replace(/\r\n/g, "\n")
                    .replace(/\r/g, "\n");

                processSSEBuffer();

                if (buffer.trim()) {
                    const data = parseSSEEvent(buffer);

                    if (data !== null) {
                        appendAnswer(data);
                    }
                }
            } else {
                appendAnswer(lastChunk);
            }

            if (!answer.trim()) {
                answerElement.textContent =
                    "답변이 비어 있습니다. 다시 질문해 주세요.";
            }

        } catch (error) {
            console.error("AI 챗봇 오류:", error);

            answerElement.textContent =
                "답변을 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.";

        } finally {
            isSending = false;
            sendButton.disabled = false;
            messageInput.disabled = false;
            messageInput.focus();
            scrollToBottom();
        }
    }

    chatForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const question = messageInput.value.trim();

        if (!question || isSending) return;

        messageInput.value = "";
        sendMessage(question);
    });

    suggestions.forEach((button) => {
        button.addEventListener("click", () => {
            const question = button.textContent.trim();

            messageInput.value = question;
            messageInput.focus();
        });
    });
});
