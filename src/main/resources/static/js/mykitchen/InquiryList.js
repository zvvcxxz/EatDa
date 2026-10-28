import { useInquiryStore } from './InquiryStore.js';

const { ref, computed, onMounted } = window.Vue;
const { useRouter } = window.VueRouter;

export default {
    template: '#inquiry-list-template',

    emits: ['edit', 'refresh'],

    setup(props, { emit }) {
        const store = useInquiryStore();
		const router = useRouter();

        // 템플릿에서 바로 사용할 데이터
        const inquiries = computed(() => store.inquiries);
        const filters = store.filters;
        const page = computed(() => store.page);
        const totalPage = computed(() => store.totalPage);
        const loading = computed(() => store.loading);
        const errorMessage = computed(() => store.errorMessage);

        // 검색어를 입력한 후 검색 버튼을 눌렀을 때
        const search = async () => {
            store.resetPage();
            await store.fetchInquiries(1);
        };

        // 유형 탭 변경
        const changeType = async (type) => {
            store.filters.inquiryType = type;
            store.resetPage();
            await store.fetchInquiries(1);
        };

        const changePage = async (nextPage) => {
            if (nextPage < 1 || nextPage > store.totalPage) {
                return;
            }
            await store.fetchInquiries(nextPage);
        };

        // 목록 새로고침
        const loadList = async () => {
            await store.fetchInquiries(store.page);
        };

        // 수정 버튼을 누르면 수정 화면으로 이동하도록 부모에 알림
		const goEdit = (inquiryId) => {
		    router.push(`/edit/${inquiryId}`);
		};

        onMounted(() => {
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