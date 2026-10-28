import { useInquiryStore } from './InquiryStore.js';

const { ref, reactive, computed, onMounted, onBeforeUnmount } = window.Vue;

export default {
    template: '#inquiry-form-template',

    props: {
        mode: {
            type: String,
            default: 'create'
        },
        inquiryId: {
            type: Number,
            default: null
        },
        parentId: {
            type: Number,
            default: null
        }
    },

    emits: ['saved', 'cancel'],

    setup(props, { emit }) {
        const store = useInquiryStore();

        const form = reactive({
            inquiryType: 0,
            title: '',
            content: ''
        });

        const saving = ref(false);
        const quillElement = ref(null);
        const existingFiles = ref([]);
        const newFiles = ref([]);

        let quill = null;
        let fileSequence = 0;

        const formTitle = computed(() => {
            if (props.mode === 'edit') {
                return '문의 수정';
            }

            if (props.mode === 'reply') {
                return '추가 문의 작성';
            }

            return '문의 작성';
        });

        const initQuill = () => {// Quill 초기화
            if (!window.Quill || !quillElement.value) {
                console.error('Quill 에디터를 초기화할 수 없습니다.');
                return;
            }

            quill = new window.Quill(quillElement.value, {
                theme: 'snow',
                placeholder: '문의 내용을 입력하세요.',
                modules: {
                    toolbar: [
                        [{ header: [1, 2, false] }],
                        ['bold', 'italic', 'underline'],
                        [{ list: 'ordered' }, { list: 'bullet' }],
                        ['link'],
                        ['clean']
                    ]
                }
            });
        };

        // 수정할 문의 내용 불러오기
        const loadEditData = async () => {
            if (props.mode !== 'edit' || !props.inquiryId) {
                return;
            }

            try {
                const detail = await store.fetchDetail(props.inquiryId);

                form.inquiryType = detail.inquiryType;
                form.title = detail.title;
                form.content = detail.content ?? '';

                existingFiles.value = detail.files ?? [];

                // Quill에 기존 HTML 표시
                if (quill) {
                    quill.clipboard.dangerouslyPasteHTML(
                        detail.content ?? ''
                    );
                }
            } catch (error) {
                console.error('수정할 문의 조회 실패:', error);
                alert('수정할 문의를 불러오지 못했습니다.');
                emit('cancel');
            }
        };

        // 파일 선택
        const selectFiles = (event) => {
            const selected = Array.from(event.target.files ?? []);

            for (const file of selected) {
                newFiles.value.push({
                    id: ++fileSequence,
                    file
                });
            }

            // 같은 파일을 다시 선택할 수 있도록 초기화
            event.target.value = '';
        };

        // 아직 업로드하지 않은 파일 제거
        const removeSelectedFile = (index) => {
            newFiles.value.splice(index, 1);
        };

        // 기존 파일은 서버에서도 즉시 삭제
        const deleteExistingFile = async (file) => {
            if (!confirm(`'${file.originalFileName}' 파일을 삭제할까요?`)) {
                return;
            }

            try {
                await window.axios.delete(
                    `/api/inquiries/${props.inquiryId}/files/${file.inquiryFileId}`
                );

                existingFiles.value = existingFiles.value.filter(
                    item => item.inquiryFileId !== file.inquiryFileId
                );
            } catch (error) {
                console.error('첨부파일 삭제 실패:', error);
                alert(
                    error.response?.data ||
                    '첨부파일 삭제에 실패했습니다.'
                );
            }
        };

        // FormData 공통 항목 구성
        const makeFormData = (includeType = true) => {
            const data = new FormData();

            if (includeType) {
                data.append('inquiryType', form.inquiryType);
            }

            data.append('title', form.title.trim());
            data.append('content', quill.root.innerHTML);

            return data;
        };

        // 신규 등록 또는 추가 문의에서 파일도 함께 전송
        const appendUploadFiles = (data) => {
            for (const item of newFiles.value) {
                data.append('uploadFiles', item.file);
            }
        };

        // 수정 화면에서는 파일별 API로 업로드
        const uploadFilesAfterUpdate = async () => {
            for (const item of newFiles.value) {
                const data = new FormData();
                data.append('file', item.file);

                await window.axios.post(
                    `/api/inquiries/${props.inquiryId}/files`,
                    data
                );
            }
        };

        // 저장 버튼
        const submit = async () => {
            if (saving.value) {
                return;
            }

            const title = form.title.trim();
            const content = quill?.root?.innerHTML ?? '';
            const plainText = quill?.getText()?.trim() ?? '';

            if (!title) {
                alert('제목을 입력해 주세요.');
                return;
            }

            if (!plainText) {
                alert('문의 내용을 입력해 주세요.');
                return;
            }

            saving.value = true;

            try {
                let response;

                if (props.mode === 'edit') {
                    // 기존 문의 내용 수정
                    const data = makeFormData(true);

                    response = await window.axios.post(
                        `/api/inquiries/${props.inquiryId}`,
                        data
                    );

                    // 새 파일은 별도 API로 업로드
                    await uploadFilesAfterUpdate();

                    alert('문의가 수정되었습니다.');
                } else if (props.mode === 'reply') {
                    // 추가 문의 등록
                    const data = makeFormData(false);
                    appendUploadFiles(data);

                    response = await window.axios.post(
                        `/api/inquiries/${props.parentId}/follow-ups`,
                        data
                    );

                    alert('추가 문의가 등록되었습니다.');
                } else {
                    // 새 문의 등록
                    const data = makeFormData(true);
                    appendUploadFiles(data);

                    response = await window.axios.post(
                        '/api/inquiries',
                        data
                    );

                    alert('문의가 등록되었습니다.');
                }

                emit('saved', response?.data);
                emit('cancel');
            } catch (error) {
                console.error('문의 저장 실패:', error);

                alert(
                    error.response?.data ||
                    '문의 저장 중 오류가 발생했습니다.'
                );
            } finally {
                saving.value = false;
            }
        };

        onMounted(async () => {
            initQuill();
            await loadEditData();
        });

        onBeforeUnmount(() => {
            quill = null;
        });  // Quill은 컴포넌트가 사라지면 더 이상 참조하지 않음

        return {
            form,
            formTitle,
            saving,
            quillElement,
            existingFiles,
            newFiles,
            selectFiles,
            removeSelectedFile,
            deleteExistingFile,
            submit
        };
    }
};