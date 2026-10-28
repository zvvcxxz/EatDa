import { useInquiryStore } from './InquiryStore.js';
import InquiryReply from './InquiryReply.js';
import InquiryForm from './InquiryForm.js';

const { ref, computed } = window.Vue;

export default {
    template: '#inquiry-detail-template',

    components: { InquiryReply, InquiryForm },

    props: {
        inquiry: {
            type: Object,
            required: true
        }
    },

    emits: ['edit', 'deleted'],

    setup(props, { emit }) {
        const store = useInquiryStore();

        const loading = ref(false);
        const detail = ref({});
        const replies = ref([]);
        const showReplyForm = ref(false);

        // 상세 조회가 이미 이루어졌는지 확인
        const detailLoaded = ref(false);
        const repliesLoaded = ref(false);

        const typeLabel = computed(() => {
            const labels = {
                0: '서비스',
                1: '레시피',
                2: '커뮤니티',
                3: '기타'
            };

            return labels[detail.value.inquiryType] ?? '기타';
        });

        // 첨부파일 다운로드 URL
        const fileDownloadUrl = (fileId) => {
            return `/api/inquiries/${props.inquiry.inquiryId}/files/${fileId}`;
        };

        // 상세 내용과 추가 문의 목록 조회
        const toggleDetail = async (event) => {
            // 아코디언을 닫았을 때는 조회하지 않음
            if (!event.target.open) {
                return;
            }

            if (detailLoaded.value) {
                return;
            }

            loading.value = true;

            try {
                detail.value = await store.fetchDetail(
                    props.inquiry.inquiryId
                );

                detailLoaded.value = true;

                if (detail.value.status === 1) {
                    replies.value = await store.fetchFollowUps(
                        props.inquiry.inquiryId
                    );

                    repliesLoaded.value = true;
                }
            } catch (error) {
                console.error('문의 상세 조회 실패:', error);
                alert('문의 상세 내용을 불러오지 못했습니다.');
            } finally {
                loading.value = false;
            }
        };

        // 문의 삭제
        const removeInquiry = async () => {
            const inquiryId = props.inquiry.inquiryId;

            if (!confirm('이 문의를 삭제하시겠습니까?')) {
                return;
            }

            try {
                await window.axios.delete(
                    `/api/inquiries/${inquiryId}`
                );

                alert('문의가 삭제되었습니다.');

                // 부모 목록 컴포넌트에 삭제 완료 알림
                emit('deleted', inquiryId);
            } catch (error) {
                console.error('문의 삭제 실패:', error);

                alert(
                    error.response?.data ||
                    '문의 삭제에 실패했습니다.'
                );
            }
        };

        // 추가 문의 등록 성공 후 목록 다시 조회
        const onReplySaved = async () => {
            showReplyForm.value = false;

            try {
                replies.value = await store.fetchFollowUps(
                    props.inquiry.inquiryId
                );

                repliesLoaded.value = true;
            } catch (error) {
                console.error('추가 문의 목록 조회 실패:', error);
                alert('추가 문의 목록을 새로 불러오지 못했습니다.');
            }
        };

        return {
            loading,
            detail,
            replies,
            showReplyForm,
            typeLabel,
            fileDownloadUrl,
            toggleDetail,
            removeInquiry,
            onReplySaved
        };
    }
};