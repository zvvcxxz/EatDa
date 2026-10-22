const InquiryPagination = {
    template: '#inquiry-pagination-template',
    props: {
        currentPage: { type: Number, required: true },
        totalPage: { type: Number, required: true }
    },
    emits: ['change-page'],
    computed: {
        pages() {
            const list = [];
            const start = Math.max(1, this.currentPage - 2);
            const end = Math.min(this.totalPage, start + 4);
            for (let i = start; i <= end; i++) {
                list.push(i);
            }
            return list;
        }
    }
};