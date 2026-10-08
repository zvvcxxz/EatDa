(() => {
 const dialog=document.getElementById('login-modal');
 if(!dialog)return;
 let opener;
 window.openLoginModal=()=>{opener=document.activeElement;if(!dialog.open)dialog.showModal();};
 window.closeLoginModal=()=>dialog.close();
 document.addEventListener('click',event=>{
  if(event.target.closest('[data-open-login]')){event.preventDefault();window.openLoginModal();}
  if(event.target.closest('[data-close-login]'))dialog.close();
 });
 //모달 바깥(배경) 클릭 시 흔들리는(커지는) 효과 추가
 dialog.addEventListener('click', event => {
   if (event.target !== dialog) return;
   if (dialog.classList.contains('bounce-shake')) return;
   
   dialog.classList.add('bounce-shake');
   setTimeout(() => {
     dialog.classList.remove('bounce-shake');
   }, 400);
 });

 //dialog.addEventListener('click',event=>{if(event.target!==dialog)return;const r=dialog.getBoundingClientRect();if(event.clientX<r.left||event.clientX>r.right||event.clientY<r.top||event.clientY>r.bottom)dialog.close();});
 dialog.addEventListener('close',()=>opener?.focus());
})();
