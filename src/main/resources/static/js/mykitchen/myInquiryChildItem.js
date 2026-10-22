const InquiryChildItem = {
    template: '#inquiry-child-item-template',
    props: {
        parentId: { type: Number, required: true },
        childInfo: { type: Object, required: true }
    },
    emits: ['refresh-all', 'open-child-edit'],
    data() {
        return {
            isOpen: false,
            loaded: false,
            childDetail: {}
        };
    },
    watch: {
        childInfo: {
            deep: true,
            handler() {
                if (this.isOpen) {
                    this.loadChildDetail();
                } else {
                    this.loaded = false;
                }
            }
        }
    },
    methods: {
        async onToggle(e) {
            this.isOpen = e.target.open;
            if (this.isOpen && !this.loaded) {
                await this.loadChildDetail();
                this.loaded = true;
            }
        },
        async loadChildDetail() {
            try {
                const res = await axios.get(`/api/inquiries/${this.parentId}/follow-ups/${this.childInfo.inquiryId}`);
                this.childDetail = res.data;
            } catch (err) {
                alert(err.response?.data || '추가 문의 상세 조회에 실패했습니다.');
            }
        },
        async deleteChild() {
            if (!confirm('작성하신 추가 문의를 삭제하시겠습니까?')) return;
            try {
                await axios.delete(`/api/inquiries/${this.childInfo.inquiryId}`);
                alert('삭제되었습니다.');
                this.$emit('refresh-all');
            } catch (err) {
                alert(err.response?.data || '삭제 실패');
            }
        },
        formatDate(str) {
            if (!str) return '';
            return str.replace('T', ' ').substring(0, 16);
        }
    }
};