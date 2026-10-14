document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('recipeForm');
    if (!form) return;

    const thumbnailFile = document.getElementById('recipePhoto');
    const thumbnailImage = document.getElementById('recipePhotoPreview');
    const thumbnailFlag = document.getElementById('deleteThumbnail');
    const thumbnailMessage = document.getElementById('thumbnailDeleteMessage');

    function clearPreview(image) {
        image.onload = null;
        image.hidden = true;
        image.removeAttribute('src');
    }

    form.addEventListener('click', function(event) {
        const thumbnailButton = event.target.closest(
            '[data-delete-thumbnail]'
        );

        if (thumbnailButton) {
            // 선택한 파일도 함께 취소
            thumbnailFile.value = '';
            thumbnailFlag.value = 'true';

            clearPreview(thumbnailImage);
            thumbnailMessage.hidden = false;
            return;
        }

        const stepButton = event.target.closest(
            '[data-delete-step-image]'
        );

        if (!stepButton) return;

        const row = stepButton.closest('.ds-step-editor');

        row.querySelector('[data-field="selectFile"]').value = '';
        row.querySelector('[data-field="deleteImage"]').value = 'true';

        clearPreview(row.querySelector('.recipe-step-image'));
        row.querySelector('[data-image-delete-message]').hidden = false;
    });

    // 새 사진을 선택하면 삭제 여부를 해제
    form.addEventListener('change', function(event) {
        if (event.target === thumbnailFile) {
            if (thumbnailFile.files.length > 0) {
                thumbnailFlag.value = 'false';
                thumbnailMessage.hidden = true;
            }
            return;
        }

        const input = event.target.closest(
            'input[data-field="selectFile"]'
        );

        if (!input || input.files.length === 0) return;

        const row = input.closest('.ds-step-editor');
        row.querySelector('[data-field="deleteImage"]').value = 'false';
        row.querySelector('[data-image-delete-message]').hidden = true;
    });
});