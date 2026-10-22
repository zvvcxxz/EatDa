const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            inquiries: [],
            selectedType: null,
            schType: 'all',
            kwd: '',
            currentPage: 1,
            totalPage: 0,
            showModal: false,
            modalMode: 'create', // 'create', 'edit', 'followUp'
            modalData: null
        };
    },
    mounted() {
        this.fetchInquiries(1);
    },
    methods: {
        async fetchInquiries(page = 1) {
            this.currentPage = page;
            try {
                const params = {
                    page: this.currentPage,
                    schType: this.schType,
                    kwd: this.kwd
                };
                if (this.selectedType !== null) {
                    params.inquiryType = this.selectedType;
                }

                const res = await axios.get('/api/inquiries', { params });
                this.inquiries = res.data.list;
                this.totalPage = res.data.totalPage;
            } catch (err) {
                console.error(err);
                alert(err.response?.data || '목록 로드 실패');
            }
        },
        changeType(type) {
            this.selectedType = type;
            this.fetchInquiries(1);
        },
        search() {
            this.fetchInquiries(1);
        },
        openCreateModal() {
            this.modalMode = 'create';
            this.modalData = null;
            this.showModal = true;
        },
        openEditModal(data) {
            this.modalMode = 'edit';
            this.modalData = data;
            this.showModal = true;
        },
        openFollowUpModal(parentData) {
            this.modalMode = 'followUp';
            this.modalData = parentData;
            this.showModal = true;
        },
        handleSaved() {
            this.fetchInquiries(this.currentPage);
        }
    }
});

// 전역 컴포넌트 등록
app.component('inquiry-pagination', InquiryPagination);
app.component('inquiry-child-item', InquiryChildItem);
app.component('inquiry-item', InquiryItem);
app.component('inquiry-form', InquiryForm);


app.mount('#app');