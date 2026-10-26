document.addEventListener('DOMContentLoaded', function () {
    const openButton = document.querySelector('#open-lunch-chat');
    const closeButton = document.querySelector('#close-lunch-chat');
    const dialog = document.querySelector('#lunch-chat-dialog');
    const frame = document.querySelector('#lunch-chat-frame');
    const loginDialog = document.querySelector('#login-modal');

    const pendingKey = 'openLunchChatAfterLogin';
    let previousOverflow = '';

    openButton.addEventListener('click', function () {
        // 로그인 후 챗봇을 열도록 표시
        if (openButton.dataset.loggedIn !== 'true') {
            sessionStorage.setItem(pendingKey, 'true');

            if (typeof window.openLoginModal === 'function') {
                window.openLoginModal();
            } else {
                window.location.href = openButton.dataset.loginUrl;
            }
            return;
        }

        if (dialog.open) {
            return;
        }

        if (!frame.getAttribute('src')) {
            frame.src = frame.dataset.src;
        }

        previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        dialog.showModal();
    });

    closeButton.addEventListener('click', function () {
        dialog.close();
    });

    // 모달 바깥을 누르면 닫기
    dialog.addEventListener('click', function (event) {
        if (event.target !== dialog) {
            return;
        }

        const rect = dialog.getBoundingClientRect();

        if (event.clientX < rect.left || event.clientX > rect.right
                || event.clientY < rect.top || event.clientY > rect.bottom) {
            dialog.close();
        }
    });

    dialog.addEventListener('close', function () {
        document.body.style.overflow = previousOverflow;
        openButton.focus();
    });

    // 로그인 모달을 닫고 취소하면 자동 열기도 취소
    if (loginDialog) {
        loginDialog.addEventListener('close', function () {
            sessionStorage.removeItem(pendingKey);
        });
    }

    // 로그인 완료 후 돌아오면 챗봇 자동 열기
    if (openButton.dataset.loggedIn === 'true'
            && sessionStorage.getItem(pendingKey) === 'true') {
        sessionStorage.removeItem(pendingKey);
        openButton.click();
    }
});
