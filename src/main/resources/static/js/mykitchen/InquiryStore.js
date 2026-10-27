const { defineStore } = window.Pinia;
const axios = window.axios;

export const useInquiryStore = defineStore('inquiry', {
	state: () => ({ //목록
		inquiries: [],
		page: 1,
		totalPage: 0,
		totalCount: 0,
		
		filters: {
			inquiryType: null,
			schType: 'all',
			kwd:''
		},
		
		loading: false,
		errorMessage:''
	}),
	
	actions: {
		async fetchInquiries(page=1) {
			this.loading = true;
			this.errorMessage ='';
			
			try {
				const response = await axios.get('/api/inquiries', {
					params: {
						page, 
						inquiryType: this.filters.inquiryType,
						schType: this.filters.schType,
						kwd: this.fjfilters.kwd
					}
				});
				
				const data = response.data;
				
				this.inquiries = data.list ?? [];
				this.page = data.page ?? 1;
				this.totalPage = data.totalPage ?? 0;
				this.totalCount = data.totalCount ?? 0;
			} catch(error) {
				console.error('문의 목록 조회 실패: ', error);
				
				this.inquiries = [];
				this.errorMessage = error.response?.data || '문의 목록을 불러오지 못했습니다.';
			} finally {
				this.loading = false;
			}
		},
		
		async fetchDetail(inquiryId) { //문의상세
			const response = await axios.get(
				`/api/inquiries/${inquiryId}`
			);
			return response.data;
		},
		
		async fetchFollowUpDetail(inquiryId, followUpId){
			const response = await axios.get(
				`/api/inquiries/${inquiryId}/follow-ups/${followUpId}`
			);
			return response.data;
		},
		
		resetPage() { //검색조건 변경 시
			this.page = 1;
		},
		resetFilters() {
			this.filters.inquiryType = null;
			this.filters.schType = 'all';
			this.filters.kwd = '';
			this.page = 1;
		}
	}
});