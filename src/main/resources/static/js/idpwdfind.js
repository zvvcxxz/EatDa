const contextPath = "";

function idFind() {
    const nameInput = document.querySelector("#name");
    const emailInput = document.querySelector("#email");

    if (!nameInput || !nameInput.value.trim()) {
        alert("이름을 입력하세요.");
        if (nameInput) nameInput.focus();
        return;
    }
    if (!emailInput || !emailInput.value.trim()) {
        alert("이메일을 입력하세요.");
        if (emailInput) emailInput.focus();
        return;
    }

    fetch(contextPath + "/member/idFind", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "name=" + encodeURIComponent(nameInput.value.trim()) 
            + "&email=" + encodeURIComponent(emailInput.value.trim())
    })
    .then(response => {
        if (!response.ok) throw new Error("HTTP 오류 : " + response.status);
        return response.text(); 
    })
    .then(text => {
        const result = document.querySelector("#findIdResult");
        if (!result) return;

        result.innerHTML = "";

        if (text.includes("존재하지 않거나") || text.includes("찾을 수 없습니다") || text.includes("일치하는")) {
            result.innerHTML = `<p style="color: #e03131; font-weight: bold; margin-top: 10px;">${text}</p>`;
        } else {
            result.innerHTML = `<p style="color: #2b8a3e; font-weight: bold; margin-top: 10px;">찾으시는 아이디는 <span style="font-size: 1.1em; text-decoration: underline;">${text}</span> 입니다.</p>`;
        }
    })
    .catch(error => {
        console.error("idFind Error:", error);
        alert("처리 중 오류가 발생했습니다.");
    });
}

function sendOk() {
    const f = document.pwdForm;

    if (!f.login_id.value.trim()) {
        alert("아이디를 입력하세요.");
        f.login_id.focus();
        return;
    }

    if (!f.name.value.trim()) {
        alert("이름을 입력하세요.");
        f.name.focus();
        return;
    }

    f.submit();
}