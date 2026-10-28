// communityPost-article.html 전용 동작

document.addEventListener('DOMContentLoaded', () => {
    const btnEL = document.querySelector('.btnSendBoardLike');

    btnEL.addEventListener('click', function() {
        const userLiked = this.dataset.liked === 'true';
        const msg = userLiked
                ? '좋아요를 취소할까요?' : '좋아요를 누를까요?';

        if (!confirm(msg)) return;

        // 등록은 POST, 취소는 DELETE
        const method = userLiked ? 'delete' : 'post';
        this.disabled = true;

        fetchRequest(this.dataset.url, method, null,
                'form', 'json', communityRequestHeaders())
            .then(data => {
                if (!data) return;

                // 서버 결과로 좋아요 표시와 개수 변경
                this.dataset.liked = String(data.liked);
                this.classList.toggle('active', data.liked);
                this.setAttribute('aria-pressed', String(data.liked));
                this.querySelector('.like-label').textContent =
                        data.liked ? '♥ 좋아요' : '♡ 좋아요';

                document.getElementById('boardLikeCount').textContent =
                        data.likeCount;
            })
            .catch(error => alert(error.message))
            .finally(() => this.disabled = false);
    });
});

// 삭제·숨김 전 확인
 document.querySelectorAll('form[data-community-confirm]').forEach(function(form) {
     form.addEventListener('submit', function(event) {
         if (!confirm(form.dataset.communityConfirm)) {
             event.preventDefault();
         }
     });
 });
