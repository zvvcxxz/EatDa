import { useInquiryStore } from '../store/inquiryStore.js';

const { ref, computed, onMounted } = window.Vue;

export default {
	template: '#inquiry-list-template',
	
	emits: ['edit', 'refresh'],
	
	setup(props, { emit }) {
		const store = useInquiryStore();
		
		const inquiries = computed(() => store.inquiries);
		const filters = store.filters;
		const page = computed(()=> store.page);
		const totalPage = computed( () => store.totalPage);
		const loading = computed(() => store.loading);
		const errorMessage = computed(() => store.errorMessage);
		
		const search = async() => { //검색어 입력하고 검색버튼 눌렀을 때
			store.resetPage();
			await store.fetchInquiries(1);
		};
		
		const changeType = async (type) => {
			store.filters.inquiryType = type;
			store.resetPage();
			await store.fetchInquiries(1);
		};
		
		const changePage = async (nextPage) => {
			if (nextPage <1 || nextPage > store.totalPage) {
				return;
			}
			
			await store.fetchInquiries(nextPage);
		};
		
		const loadList = async () => { //목록 새로고침
			await store.fetchInquiries(store.page);
		};
		
		const goEdit = (inquiryId) => {
			emit('edit', inquiryId);
		};
		
		onMounted( () => {
			store.fetchInquiries(1);
		});
		
		return {
			inquiries,
			filters,
			page,
			totalPage,
			loading,
			errorMessage,
			search,
			changeType,
			changePage,
			loadList,
			goEdit
		};
	}
};