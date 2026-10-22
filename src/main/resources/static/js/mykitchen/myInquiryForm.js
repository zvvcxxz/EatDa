const InquiryForm = {
    template: '#inquiry-form-template',
    props: {
        mode: { type: String, default: 'create' },
        targetData: { type: Object, default: null }
    },
    emits: ['close', 'saved'],
    data() {
        return {
            form: {
                inquiryType: 0,
                title: '',
                content: ''
            },
            existingFiles: [],
            newFiles: [],
            quill: null,
            submitting: false
        };
    },
    computed: {
        getModalTitle() {
            if (this.mode === 'edit') return '문의글 수정';
            if (this.mode === 'followUp') return '추가 문의 작성';
            return '1:1 문의 작성';
        }
    },
    mounted() {
        this.initQuill();
        if (this.mode === 'edit' && this.targetData) {
            this.form.inquiryType = this.targetData.inquiryType;
            this.form.title = this.targetData.title;
            this.form.content = this.targetData.content;
            this.existingFiles = [...(this.targetData.files || [])];
            this.quill.root.innerHTML = this.form.content;
        }
    },
    methods: {
        initQuill() {
            this.quill = new Quill(this.$refs.editorRef, {
                theme: 'snow',
                placeholder: '문의하실 내용을 입력하세요.',
                modules: {
                    toolbar: {
                        container: [
                            [{ 'header': [1, 2, false] }],
                            ['bold', 'italic', 'underline', 'strike'],
                            [{ 'color': [] }, { 'background': [] }],
                            ['image', 'link'],
                            ['clean']
                        ],
                        handlers: {
                            image: this.quillImageHandler
                        }
                    }
                }
            });

            this.quill.on('text-change', () => {
                this.form.content = this.quill.root.innerHTML;
            });
        },
        // Quill 에디터 이미지 업로드 처리 (서버 controller: /api/editor/upload)
        quillImageHandler() {
            const input = document.createElement('input');
            input.setAttribute('type', 'file');
            input.setAttribute('accept', 'image/*');
            input.click();

            input.onchange = async () => {
                const file = input.files[0];
                if (!file) return;

                const formData = new FormData();
                formData.append('imageFile', file);

                try {
                    const res = await axios.post('/api/editor/upload', formData);
                    const range = this.quill.getSelection(true);
                    this.quill.insertEmbed(range.index, 'image', res.data.imageUrl);
                    this.quill.setSelection(range.index + 1);
                } catch (err) {
                    alert('에디터 이미지 업로드 실패');
                }
            };
        },
        handleFileSelect(e) {
            this.newFiles = Array.from(e.target.files);
        },
        // 수정 모드 시 개별 파일 삭제 API
        async deleteExistingFile(fileId) {
            if (!confirm('이 파일을 삭제하시겠습니까?')) return;
            try {
                await axios.delete(`/api/inquiries/${this.targetData.inquiryId}/files/${fileId}`);
                this.existingFiles = this.existingFiles.filter(f => f.inquiryFileId !== fileId);
                alert('파일이 삭제되었습니다.');
            } catch (err) {
                alert(err.response?.data || '파일 삭제 실패');
            }
        },
        async submitForm() {
            if (!this.form.title.trim()) {
                alert('제목을 입력하세요.');
                return;
            }
            if (this.submitting) return;
            this.submitting = true;

            try {
                if (this.mode === 'create') {
                    const formData = new FormData();
                    formData.append('inquiryType', this.form.inquiryType);
                    formData.append('title', this.form.title);
                    formData.append('content', this.form.content);
                    this.newFiles.forEach(f => formData.append('uploadFiles', f));

                    await axios.post('/api/inquiries', formData);
                } 
                else if (this.mode === 'edit') {
                    // 본문 수정
                    const formData = new FormData();
                    formData.append('inquiryType', this.form.inquiryType);
                    formData.append('title', this.form.title);
                    formData.append('content', this.form.content);

                    await axios.post(`/api/inquiries/${this.targetData.inquiryId}`, formData);

                    // 새 파일 개별 추가 업로드
                    for (const file of this.newFiles) {
                        const fileForm = new FormData();
                        fileForm.append('file', file);
                        await axios.post(`/api/inquiries/${this.targetData.inquiryId}/files`, fileForm);
                    }
                } 
                else if (this.mode === 'followUp') {
                    const formData = new FormData();
                    formData.append('title', this.form.title);
                    formData.append('content', this.form.content);
                    this.newFiles.forEach(f => formData.append('uploadFiles', f));

                    await axios.post(`/api/inquiries/${this.targetData.inquiryId}/follow-ups`, formData);
                }

                alert('저장되었습니다.');
                this.$emit('saved');
                this.$emit('close');
            } catch (err) {
                alert(err.response?.data || '저장 중 오류 발생');
            } finally {
                this.submitting = false;
            }
        }
    }
};