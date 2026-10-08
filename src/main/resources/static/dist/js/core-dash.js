(function() {
	const isHidden = el => {
		if (!el) return true;
		const styles = window.getComputedStyle(el);
		return styles.display === 'none' || styles.visibility === 'hidden';
	}; 
		
	const mobileNavToggleBtn = document.querySelector('.mobile-nav-toggle');
	function mobileNavToogle() {
		document.querySelectorAll('.vertical-nav > .nav-menu > li.has-sub-menu').forEach(el => {
			el.setAttribute('aria-expanded', 'false');
			if(el.querySelector('ul')) {
				el.querySelector('ul').classList.remove('dropdown-active');
			}
		});
		
		const bodyEl = document.querySelector('body');
		if(bodyEl) bodyEl.classList.toggle('mobile-nav-active');
	}
  
	if (mobileNavToggleBtn) {
		mobileNavToggleBtn.addEventListener('click', mobileNavToogle);
	}

	const menuClosedIcon = document.querySelector('.menu-closed-icon');
	if (menuClosedIcon) {
		menuClosedIcon.addEventListener('click', () => {
			if (document.querySelector('.mobile-nav-active')) {
				mobileNavToogle();
			}
		});
	}
		
	document.querySelectorAll('.vertical-nav > .nav-menu > li > .menu-link').forEach(menu => {
		menu.addEventListener('click', function(e) {
		
			if(mobileNavToggleBtn && isHidden(mobileNavToggleBtn)) {
				document.querySelector('body').classList.remove('mobile-nav-active');
			}

			const preEl = this.nextElementSibling || null;
				
			document.querySelectorAll('.vertical-nav > .nav-menu > li').forEach(el => {
				el.classList.remove('active');
          
				const submenu = el.querySelector('ul');
				if(submenu && preEl !== submenu) {
					el.setAttribute('aria-expanded', 'false');
					submenu.classList.remove('dropdown-active');
				}
			});

			if(this.parentNode) this.parentNode.classList.toggle('active');
			if (this.parentNode && this.parentNode.classList.contains('has-sub-menu')) {
				const expanded = this.parentNode.getAttribute('aria-expanded') === 'true';
				this.parentNode.setAttribute('aria-expanded', !expanded);
					
				const submenu = this.nextElementSibling;
				if(submenu) submenu.classList.toggle('dropdown-active');
			}

			e.stopImmediatePropagation();
		});
	});

	const collapsedMenu = document.querySelector('.collapsed-menu');
	if (collapsedMenu) {
		collapsedMenu.onclick = function () {
			document.querySelectorAll('.vertical-nav > .nav-menu > li').forEach(el => {
				const submenu = el.querySelector('ul');
				if(submenu) {
					el.setAttribute('aria-expanded', 'false');
					submenu.classList.remove('dropdown-active');
				}
			});
		  
			const vNav = document.querySelector('.vertical-nav');
			if (vNav) {
				vNav.classList.toggle('nav-expand-lg');
				vNav.classList.toggle('nav-expand-sm');
			}
			
			const navMenu = document.querySelector('.nav-menu');
			if (navMenu) navMenu.classList.toggle('ps');
			
			if (this.parentNode) {
				const expanded = this.parentNode.getAttribute('aria-expanded') === 'true';
				this.parentNode.setAttribute('aria-expanded', !expanded);
			}
		};
	}
 
	const preloader = document.querySelector('#preloader');
	if (preloader && !isHidden(preloader)) {
		window.addEventListener('load', () => {
			preloader.style.display = 'none';
		});
	}
  
	let scrollTop = document.querySelector('.scroll-top');
	if(scrollTop) {
		function toggleScrollTop() {
			if (scrollTop) {
				window.scrollY > 100 ? scrollTop.classList.add('active') : scrollTop.classList.remove('active');
			}
		}
		
		scrollTop.addEventListener('click', (e) => {
			e.preventDefault();
			window.scrollTo({
				top: 0,
				behavior: 'smooth'
			});
		});
	
		window.addEventListener('load', toggleScrollTop);
		document.addEventListener('scroll', toggleScrollTop);
	}
  
	function aosInit() {
		if (typeof AOS !== 'undefined') {
			AOS.init({
				duration: 600,
				easing: 'ease-in-out',
				once: true,
				mirror: false
			});
		}
	}
	
	window.addEventListener('load', aosInit);
})();

document.addEventListener("DOMContentLoaded", function () {
	const currentPath = window.location.pathname;
	const allLinks = document.querySelectorAll(".nav-menu a.menu-link, .nav-menu a.sub-menu-link");
	if (allLinks.length === 0) return;

    function getPathCandidates(path) {
        const parts = path.split("/");
        const candidates = [];
        for (let i = parts.length; i > 1; i--) {
            const subPath = parts.slice(0, i).join("/");
            candidates.push(subPath);
        }
        return candidates;
    }
    
    const pathCandidates = getPathCandidates(currentPath);

    let matchedLink = null;
    for (let i = 0; i < pathCandidates.length; i++) {
    	for (let j = 0; j < allLinks.length; j++) {
    		let linkPath = allLinks[j].getAttribute('href');
    		if (!linkPath || linkPath === '#') continue;
    		
    		if(i != 0) {
    			let linkCandidates = getPathCandidates(linkPath);
    			if(linkCandidates[i]) {
    				linkPath = linkCandidates[i];
    			}
    		}
    		
    		if (linkPath.startsWith(pathCandidates[i])) {
    			matchedLink = allLinks[j];
                break;
    		}
    	}
    	if (matchedLink) break;
    }
    
    if (matchedLink) {
        const li = matchedLink.closest("li");
		const parentMenu = matchedLink.closest("li.has-sub-menu");
        
        if (parentMenu) {
        	parentMenu.setAttribute("aria-expanded", "true");
        	if(matchedLink.parentElement) matchedLink.parentElement.classList.add("active");
        	const ul = matchedLink.closest("ul");
        	if(ul) ul.classList.add('dropdown-active');
        } else {
        	 if(li) li.classList.add("active");
        }
    }
});

if(typeof PerfectScrollbar == 'function') {
    const container = document.querySelector(".nav-menu");
    if(container) {
        const ps = new PerfectScrollbar(container, {
            wheelPropagation: false
        });
    }
}