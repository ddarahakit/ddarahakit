<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/blog'
import useAuthStore from '@/stores/useAuthStore'
import QuillEditor from '@/components/base/QuillEditor.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const backend = import.meta.env.VITE_IMG_BASE_URL

// 수정 모드 여부
const editIdx = computed(() => route.params.idx || null)
const isEdit = computed(() => !!editIdx.value)

const isAdmin = computed(() => authStore.getUserRole() === 'ROLE_ADMIN')

// 폼 상태
const form = reactive({
    title: '',
    category: '',
    summary: '',
    thumbnailUrl: ''
})

// 기존 카테고리 제안(입력 자동완성)
const categorySuggestions = ref([])

// 에디터 모델 { text, content(Delta JSON) }
const editorModel = ref({ text: '', content: '' })
const initialEditorContent = ref(null)

const errorMessage = ref('')
const isSaving = ref(false)

/**
 * 유효성 검사
 */
const isValid = computed(() =>
    form.title.trim().length > 0 && editorModel.value.text.trim().length > 0
)

/**
 * 저장 (작성/수정)
 */
const save = async () => {
    errorMessage.value = ''
    if (!form.title.trim()) {
        errorMessage.value = '제목을 입력해주세요.'
        return
    }
    if (!editorModel.value.text.trim()) {
        errorMessage.value = '내용을 입력해주세요.'
        return
    }

    isSaving.value = true
    const payload = {
        title: form.title.trim(),
        category: form.category.trim(),
        summary: form.summary.trim(),
        thumbnailUrl: form.thumbnailUrl.trim(),
        content: editorModel.value.content
    }
    const data = isEdit.value
        ? await api.blogUpdate(editIdx.value, payload)
        : await api.blogCreate(payload)
    isSaving.value = false

    if (data && data.success && data.results) {
        router.push({ name: 'blogDetail', params: { idx: data.results.idx } })
    } else {
        errorMessage.value = data?.errorMessage || '저장에 실패했습니다.'
    }
}

const cancel = () => {
    if (isEdit.value) {
        router.push({ name: 'blogDetail', params: { idx: editIdx.value } })
    } else {
        router.push({ name: 'blogList' })
    }
}

/**
 * 수정 모드: 기존 글 로드 후 프리필
 */
const loadForEdit = async () => {
    const data = await api.getBlogDetail(editIdx.value)
    if (data && data.success && data.results) {
        form.title = data.results.title || ''
        form.category = data.results.category || ''
        form.summary = data.results.summary || ''
        form.thumbnailUrl = data.results.thumbnailUrl || ''
        initialEditorContent.value = data.results.content || ''
    } else {
        alert('글을 불러오지 못했습니다.')
        router.replace({ name: 'blogList' })
    }
}

onMounted(async () => {
    // 관리자만 접근 가능 (백엔드도 최종 강제)
    if (!isAdmin.value) {
        alert('관리자만 작성할 수 있습니다.')
        router.replace({ name: 'blogList' })
        return
    }
    // 카테고리 자동완성 제안 로드
    const cat = await api.getCategories()
    if (cat && cat.success) categorySuggestions.value = (cat.results || []).map(c => c.category)

    if (isEdit.value) {
        await loadForEdit()
    }
})
</script>

<template>
    <div class="max-w-3xl mx-auto px-6 pt-28 pb-24">
        <h1 class="text-2xl font-extrabold text-slate-900 mb-8">
            {{ isEdit ? '블로그 글 수정' : '블로그 글쓰기' }}
        </h1>

        <div class="space-y-6">
            <!-- 제목 -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">제목</label>
                <input v-model="form.title" type="text" maxlength="150" placeholder="제목을 입력하세요"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl outline-none focus:border-brand transition" />
            </div>

            <!-- 카테고리 -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">카테고리 <span class="text-slate-400 font-normal">(선택)</span></label>
                <input v-model="form.category" type="text" maxlength="50" list="blog-category-suggestions"
                    placeholder="예: 네트워크, 개발, 인프라"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl outline-none focus:border-brand transition" />
                <datalist id="blog-category-suggestions">
                    <option v-for="c in categorySuggestions" :key="c" :value="c" />
                </datalist>
            </div>

            <!-- 요약 -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">요약 <span class="text-slate-400 font-normal">(선택 · 목록 미리보기)</span></label>
                <textarea v-model="form.summary" rows="2" maxlength="500" placeholder="목록에 보일 한두 줄 요약"
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl outline-none focus:border-brand transition resize-none"></textarea>
            </div>

            <!-- 대표 이미지 URL -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">대표 이미지 URL <span class="text-slate-400 font-normal">(선택)</span></label>
                <input v-model="form.thumbnailUrl" type="text" maxlength="500" placeholder="https://..."
                    class="w-full px-4 py-3 border border-slate-200 rounded-xl outline-none focus:border-brand transition" />
            </div>

            <!-- 본문 -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">내용</label>
                <QuillEditor v-model="editorModel" :enable-image-upload="true" :image-base-url="backend"
                    :initial-content="initialEditorContent" min-height="360px"
                    placeholder="내용을 작성해주세요. 이미지는 드래그·붙여넣기로 삽입할 수 있습니다." />
            </div>

            <!-- 에러 -->
            <p v-if="errorMessage" class="text-red-500 text-sm">
                <i class="fa-solid fa-circle-exclamation mr-1"></i>{{ errorMessage }}
            </p>

            <!-- 액션 -->
            <div class="flex items-center justify-end gap-3 pt-4">
                <button @click="cancel" type="button"
                    class="px-5 py-2.5 text-sm font-medium text-slate-500 hover:text-slate-700 transition">
                    취소
                </button>
                <button @click="save" type="button" :disabled="!isValid || isSaving"
                    class="px-6 py-2.5 bg-brand text-white rounded-full text-sm font-bold shadow-lg shadow-blue-100 disabled:opacity-40 hover:opacity-90 transition">
                    <i v-if="isSaving" class="fa-solid fa-spinner fa-spin mr-1.5"></i>
                    {{ isEdit ? '수정 완료' : '등록' }}
                </button>
            </div>
        </div>
    </div>
</template>
