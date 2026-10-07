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
 dialog.addEventListener('click',event=>{if(event.target!==dialog)return;const r=dialog.getBoundingClientRect();if(event.clientX<r.left||event.clientX>r.right||event.clientY<r.top||event.clientY>r.bottom)dialog.close();});
 dialog.addEventListener('close',()=>opener?.focus());
})();
