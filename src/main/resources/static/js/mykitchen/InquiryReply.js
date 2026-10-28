import { useInquiryStore } from './InquiryStore.js';

const { ref } = window.Vue;

export default {
    template: '#inquiry-reply-template',

    props: {
        reply: {
            type: Object,
            required: true
        },
        parentId: {
            type: Number,
            required: true
        }
    },

    setup(props) {
        const store = useInquiryStore();

        const loading = ref(false);
        const detail = ref(null);
        const detailLoaded = ref(false);

        // 추가 문의 아코디언 열기
        const toggleReply = async (event) => {
            if (!event.target.open || detailLoaded.value) {
                return;
            }

            loading.value = true;

            try {
                detail.value = await store.fetchFollowUpDetail(
                    props.parentId,
                    props.reply.inquiryId
                );

                detailLoaded.value = true;
            } catch (error) {
                console.error('추가 문의 상세 조회 실패:', error);
                alert('추가 문의 내용을 불러오지 못했습니다.');
            } finally {
                loading.value = false;
            }
        };

        return {
            loading,
            detail,
            toggleReply
        };
    }
};