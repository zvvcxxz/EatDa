async function deleteNoticeFile(button) {
    if (!confirm('이 첨부파일을 즉시 삭제하시겠습니까?')) return;
    button.disabled = true;
    try {
        const response = await fetch(button.dataset.deleteUrl, {
            method: 'POST',
            headers: {Accept: 'application/json', AJAX: 'true'},
            body: new URLSearchParams({
                noticeId: button.dataset.noticeId,
                fileId: button.dataset.fileId
            })
        });
        if (response.redirected || response.status === 401 || response.status === 403)
            throw new Error('다시 로그인한 뒤 삭제해주세요.');
        if (!response.ok) throw new Error('첨부파일 삭제 요청에 실패했습니다.');
        const result = await response.json();
        if (!result.success) throw new Error(result.message || '첨부파일 삭제에 실패했습니다.');
        button.closest('.tag-item').remove();
    } catch (error) {
        alert(error.message);
        button.disabled = false;
    }
}
