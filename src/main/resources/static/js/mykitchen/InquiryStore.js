const { defineStore } = window.Pinia;
const axios = window.axios;

export const useInquiryStore = defineStore('inquiry', {
    state: () => ({
        // 목록
        inquiries: [],
        page: 1,
        totalPage: 0,
        totalCount: 0,

        filters: {
            inquiryType: null,
            schType: 'all',
            kwd: ''
        },

        // 화면 상태
        loading: false,
        errorMessage: ''
    }),

    actions: {
        // 문의 목록 조회
        async fetchInquiries(page = 1) {
            this.loading = true;
            this.errorMessage = '';

            try {
                const response = await axios.get('/api/inquiries', {
                    params: {
                        page,
                        inquiryType: this.filters.inquiryType,
                        schType: this.filters.schType,
                        kwd: this.filters.kwd
                    }
                });

                const data = response.data;

                this.inquiries = data.list ?? [];
                this.page = data.page ?? 1;
                this.totalPage = data.totalPage ?? 0;
                this.totalCount = data.totalCount ?? 0;
            } catch (error) {
                console.error('문의 목록 조회 실패:', error);

                this.inquiries = [];
                this.errorMessage =
                    error.response?.data || '문의 목록을 불러오지 못했습니다.';
            } finally {
                this.loading = false;
            }
        },

        // 문의 상세 조회
        async fetchDetail(inquiryId) {
            const response = await axios.get(
                `/api/inquiries/${inquiryId}`
            );

            return response.data;
        },

        // 추가 문의 목록 조회
        async fetchFollowUps(inquiryId) {
            const response = await axios.get(
                `/api/inquiries/${inquiryId}/follow-ups`
            );

            return response.data;
        },

        // 추가 문의 상세 조회
        async fetchFollowUpDetail(inquiryId, followUpId) {
            const response = await axios.get(
                `/api/inquiries/${inquiryId}/follow-ups/${followUpId}`
            );

            return response.data;
        },

        // 검색 조건 변경
        resetPage() {
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