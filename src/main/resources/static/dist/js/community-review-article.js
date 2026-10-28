// 삭제·숨김 전 확인
 document.querySelectorAll('form[data-community-confirm]').forEach(function(form) {
     form.addEventListener('submit', function(event) {
         if (!confirm(form.dataset.communityConfirm)) {
             event.preventDefault();
         }
     });
 });
