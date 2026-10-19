document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('reviewForm');
    if (!form) return;

    const photoInput = document.getElementById('reviewPhoto');
    const photoPreview = document.getElementById('reviewPhotoPreview');
    const photoMessage = document.getElementById('reviewPhotoMessage');
    const deleteImage = document.getElementById('deleteImage');
    const changeMessage = document.getElementById('photoChangeMessage');

    const originalImage = form.dataset.originalImage || '';
    const contextPath = form.dataset.contextPath.slice(0, -1);

    const originalUrl = originalImage.startsWith('/')
        ? contextPath + originalImage : originalImage;

    if (deleteImage.value === 'true') {
        changeMessage.textContent =
            '수정 완료를 누르면 기존 사진이 삭제됩니다.';
    }

    // 새 사진을 선택하면 기존 사진을 교체
    photoInput.addEventListener('change', function() {
        const file = this.files[0];

        if (!file) {
            if (deleteImage.value === 'false' && originalUrl) {
                photoPreview.src = originalUrl;
                photoPreview.hidden = false;
                photoMessage.hidden = true;
            } else {
                photoPreview.hidden = true;
                photoPreview.removeAttribute('src');
                photoMessage.hidden = false;
            }

            changeMessage.textContent = deleteImage.value === 'true'
                ? '수정 완료를 누르면 기존 사진이 삭제됩니다.' : '';

            return;
        }

        deleteImage.value = 'false';

        const url = URL.createObjectURL(file);

        photoPreview.onload = function() {
            URL.revokeObjectURL(url);
        };

        photoPreview.src = url;
        photoPreview.hidden = false;
        photoMessage.hidden = true;

        changeMessage.textContent =
            '수정 완료를 누르면 새 사진으로 변경됩니다.';
    });

    // 삭제 표시만 하고 실제 삭제는 저장할 때 처리
    document.getElementById('deleteReviewPhoto')
        .addEventListener('click', function() {
            photoInput.value = '';
            deleteImage.value = 'true';

            photoPreview.hidden = true;
            photoPreview.removeAttribute('src');
            photoMessage.hidden = false;

            changeMessage.textContent =
                '수정 완료를 누르면 사진이 삭제됩니다.';
        });

    // 사진 변경을 취소하고 저장된 사진으로 복구
    document.getElementById('restoreReviewPhoto')
        .addEventListener('click', function() {
            photoInput.value = '';
            deleteImage.value = 'false';
            changeMessage.textContent = '';

            if (originalUrl) {
                photoPreview.src = originalUrl;
                photoPreview.hidden = false;
                photoMessage.hidden = true;
            } else {
                photoPreview.hidden = true;
                photoPreview.removeAttribute('src');
                photoMessage.hidden = false;
            }
        });

    form.addEventListener('submit', function() {
        document.getElementById('reviewSubmit').disabled = true;
    });
});