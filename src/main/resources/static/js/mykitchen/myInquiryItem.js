const InquiryItem = {
    template: '#inquiry-item-template',
    props: {
        inquiry: { type: Object, required: true }
    },
    emits: ['refresh', 'open-edit', 'open-follow-up'],
    data() {
        return {
            isOpen: false,
            loaded: false,
            detail: {},
            followUps: []
        };
    },
    methods: {
        async onToggle(e) {
            this.isOpen = e.target.open;
            if (this.isOpen && !this.loaded) {
                await this.loadDetail();
                await this.loadFollowUps();
                this.loaded = true;
            }
        },
        async loadDetail() {
            try {
                const res = await axios.get(`/api/inquiries/${this.inquiry.inquiryId}`);
                this.detail = res.data;
            } catch (err) {
                alert(err.response?.data || '상세 조회에 실패했습니다.');
            }
        },
        async loadFollowUps() {
            try {
                const res = await axios.get(`/api/inquiries/${this.inquiry.inquiryId}/follow-ups`);
                this.followUps = res.data;
            } catch (err) {
                console.error('Follow-ups load error:', err);
            }
        },
        async deleteInquiry() {
            if (!confirm('정말 삭제하시겠습니까? 모든 추가 문의 내역도 삭제됩니다.')) return;
            try {
                await axios.delete(`/api/inquiries/${this.inquiry.inquiryId}`);
                alert('삭제되었습니다.');
                this.$emit('refresh');
            } catch (err) {
                alert(err.response?.data || '삭제 실패');
            }
        },
        getTypeName(type) {
            const types = { 0: '서비스', 1: '레시피', 2: '커뮤니티', 3: '기타' };
            return types[type] || '기타';
        },
        formatDate(str) {
            if (!str) return '';
            return str.replace('T', ' ').substring(0, 16);
        }
    }
};