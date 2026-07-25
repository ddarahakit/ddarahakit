<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/blog'
import useAuthStore from '@/stores/useAuthStore'
import QuillEditor from '@/components/base/QuillEditor.vue'
import BlogAside from '@/components/blog/BlogAside.vue'
import { userImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.getUserRole() === 'ROLE_ADMIN')
const isLogin = computed(() => authStore.isLogin)
const myIdx = computed(() => Number(authStore.getUserIdx()))

const post = ref(null)
const isLoading = ref(true)
const isError = ref(false)

// 댓글 상태
const newComment = ref('')
const submitting = ref(false)
const editingId = ref(null)
const editContent = ref('')

const resolveImg = (v) => userImageUrl(v)
const canEdit = (c) => isLogin.value && myIdx.value === Number(c.userIdx)
const canDelete = (c) => isLogin.value && (myIdx.value === Number(c.userIdx) || isAdmin.value)

/**
 * 상세 조회
 */
const fetchDetail = async () => {
    isLoading.value = true
    isError.value = false
    const data = await api.getBlogDetail(route.params.idx)
    if (data && data.success && data.results) {
        post.value = data.results
    } else {
        isError.value = true
    }
    isLoading.value = false
}

watch(() => route.params.idx, fetchDetail, { immediate: true })

const goList = () => router.push({ name: 'blogList' })
const goWrite = () => router.push({ name: 'blogReg' })
const goEdit = () => router.push({ name: 'blogEdit', params: { idx: route.params.idx } })

/**
 * 삭제 (관리자)
 */
const remove = async () => {
    if (!confirm('이 글을 삭제할까요? 되돌릴 수 없습니다.')) return
    const data = await api.blogDelete(route.params.idx)
    if (data && data.success) {
        alert('삭제되었습니다.')
        router.push({ name: 'blogList' })
    } else {
        alert(data?.errorMessage || '삭제에 실패했습니다.')
    }
}

/**
 * 댓글 작성 (낙관적: 응답 댓글을 로컬 목록에 추가)
 */
const submitComment = async () => {
    const content = newComment.value.trim()
    if (!content || submitting.value) return
    submitting.value = true
    const data = await api.commentCreate(route.params.idx, content)
    submitting.value = false
    if (data && data.success && data.results) {
        post.value.comments.push(data.results)
        newComment.value = ''
    } else {
        alert(data?.errorMessage || '댓글 등록에 실패했습니다.')
    }
}

const startEdit = (c) => {
    editingId.value = c.idx
    editContent.value = c.content
}
const cancelEdit = () => {
    editingId.value = null
    editContent.value = ''
}
const saveEdit = async (c) => {
    const content = editContent.value.trim()
    if (!content) return
    const data = await api.commentUpdate(c.idx, content)
    if (data && data.success && data.results) {
        const i = post.value.comments.findIndex((x) => x.idx === c.idx)
        if (i !== -1) post.value.comments[i] = data.results
        cancelEdit()
    } else {
        alert(data?.errorMessage || '댓글 수정에 실패했습니다.')
    }
}
const removeComment = async (c) => {
    if (!confirm('댓글을 삭제할까요?')) return
    const data = await api.commentDelete(c.idx)
    if (data && data.success) {
        post.value.comments = post.value.comments.filter((x) => x.idx !== c.idx)
    } else {
        alert(data?.errorMessage || '댓글 삭제에 실패했습니다.')
    }
}
</script>

<template>
    <div class="max-w-7xl mx-auto px-6 pt-28 pb-24">
        <!-- 헤더 (목록과 동일) -->
        <div class="flex items-end justify-between mb-8 gap-4">
            <div>
                <h1 class="text-3xl font-extrabold text-slate-900">블로그</h1>
                <p class="text-slate-500 mt-2">소개하고 싶은 이야기와 소식을 전합니다.</p>
            </div>
            <button v-if="isAdmin" @click="goWrite"
                class="shrink-0 px-5 py-2.5 bg-brand text-white rounded-full text-sm font-bold shadow-lg shadow-blue-100 hover:opacity-90 transition">
                <i class="fa-solid fa-pen mr-1.5"></i>글쓰기
            </button>
        </div>

        <div class="flex flex-col lg:flex-row gap-8">
            <!-- ── 좌측 aside (목록·상세 공용) ──────────── -->
            <BlogAside />

            <!-- ── 본문 ─────────────────────────────── -->
            <div class="flex-1 min-w-0 max-w-3xl">
                <!-- 로딩 (스켈레톤) -->
                <div v-if="isLoading">
                    <div class="w-20 h-6 rounded-full bg-slate-100 skeleton mb-4"></div>
                    <div class="w-11/12 h-8 rounded bg-slate-100 skeleton mb-2"></div>
                    <div class="w-2/3 h-8 rounded bg-slate-100 skeleton"></div>
                    <div class="flex items-center gap-3 mt-5 pb-6 border-b border-slate-100">
                        <div class="w-20 h-4 rounded bg-slate-100 skeleton"></div>
                        <div class="w-28 h-4 rounded bg-slate-100 skeleton"></div>
                    </div>
                    <div class="w-full h-56 rounded-2xl bg-slate-100 skeleton mt-8"></div>
                    <div class="space-y-3 mt-8">
                        <div class="w-full h-4 rounded bg-slate-100 skeleton"></div>
                        <div class="w-full h-4 rounded bg-slate-100 skeleton"></div>
                        <div class="w-5/6 h-4 rounded bg-slate-100 skeleton"></div>
                        <div class="w-full h-4 rounded bg-slate-100 skeleton"></div>
                        <div class="w-3/4 h-4 rounded bg-slate-100 skeleton"></div>
                    </div>
                </div>

                <!-- 에러 -->
                <div v-else-if="isError || !post" class="text-center text-slate-400 py-24">
                    글을 불러오지 못했습니다.
                    <div class="mt-6">
                        <button @click="goList" class="text-brand font-medium">← 목록으로</button>
                    </div>
                </div>

                <template v-else>
            <!-- 상단 네비 + 관리 버튼 -->
            <div class="flex items-center justify-between mb-6">
                <button @click="goList" class="text-sm text-slate-400 hover:text-brand transition">
                    <i class="fa-solid fa-chevron-left text-xs mr-1"></i>블로그 목록
                </button>
                <div v-if="isAdmin" class="flex items-center gap-2">
                    <button @click="goEdit"
                        class="px-3 py-1.5 text-xs font-medium text-slate-500 border border-slate-200 rounded-full hover:border-brand hover:text-brand transition">
                        <i class="fa-solid fa-pen mr-1"></i>수정
                    </button>
                    <button @click="remove"
                        class="px-3 py-1.5 text-xs font-medium text-red-500 border border-red-100 rounded-full hover:bg-red-50 transition">
                        <i class="fa-solid fa-trash mr-1"></i>삭제
                    </button>
                </div>
            </div>

            <!-- 카테고리 -->
            <RouterLink v-if="post.category" :to="{ name: 'blogList', query: { category: post.category } }"
                class="inline-block text-xs font-medium text-brand bg-blue-50 px-3 py-1 rounded-full mb-3 hover:bg-blue-100 transition">
                <i class="fa-solid fa-tag text-[10px] mr-1"></i>{{ post.category }}
            </RouterLink>

            <!-- 제목 -->
            <h1 class="text-3xl font-extrabold text-slate-900 leading-snug">{{ post.title }}</h1>

            <!-- 메타 -->
            <div class="flex items-center gap-3 text-sm text-slate-400 mt-4 pb-6 border-b border-slate-100">
                <span class="font-medium text-slate-600">{{ post.userName }}</span>
                <span>·</span>
                <span>{{ post.createdAt }}</span>
                <span>·</span>
                <span>조회 {{ post.viewCount }}</span>
            </div>

            <!-- 대표 이미지 -->
            <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title"
                class="w-full rounded-2xl mt-8 object-cover" />

            <!-- 본문 -->
            <div class="mt-8 blog-content">
                <QuillEditor :read-only="true" :initial-content="post.content" />
            </div>

            <!-- 댓글 -->
            <section class="mt-16 border-t border-slate-100 pt-10">
                <h2 class="text-lg font-bold text-slate-900 mb-6">
                    댓글 <span class="text-brand">{{ post.comments.length }}</span>
                </h2>

                <!-- 작성 폼 -->
                <div v-if="isLogin" class="flex gap-3 mb-8">
                    <textarea v-model="newComment" rows="2" maxlength="1000" placeholder="댓글을 남겨보세요"
                        class="flex-1 px-4 py-3 border border-slate-200 rounded-xl outline-none focus:border-brand transition resize-none text-sm"></textarea>
                    <button @click="submitComment" :disabled="submitting || !newComment.trim()"
                        class="shrink-0 px-5 bg-brand text-white rounded-xl text-sm font-bold disabled:opacity-40 hover:opacity-90 transition">
                        등록
                    </button>
                </div>
                <div v-else class="mb-8 text-center bg-slate-50 rounded-xl py-6 text-sm text-slate-500">
                    댓글을 작성하려면
                    <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath } }" class="text-brand font-semibold">로그인</RouterLink>
                    이 필요합니다.
                </div>

                <!-- 목록 -->
                <ul class="space-y-6">
                    <li v-for="c in post.comments" :key="c.idx" class="flex gap-3">
                        <div class="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 shrink-0 overflow-hidden flex items-center justify-center">
                            <img v-if="c.userProfileImageUrl" :src="resolveImg(c.userProfileImageUrl)" :alt="c.userName"
                                class="w-full h-full object-cover" />
                            <i v-else class="fa-solid fa-user text-slate-300 text-sm"></i>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2">
                                <span class="text-sm font-semibold text-slate-700">{{ c.userName }}</span>
                                <span class="text-xs text-slate-400">{{ c.createdAt }}</span>
                            </div>

                            <!-- 수정 모드 -->
                            <div v-if="editingId === c.idx" class="mt-2">
                                <textarea v-model="editContent" rows="2" maxlength="1000"
                                    class="w-full px-3 py-2 border border-slate-200 rounded-lg outline-none focus:border-brand text-sm resize-none"></textarea>
                                <div class="flex gap-2 mt-2">
                                    <button @click="saveEdit(c)" class="px-3 py-1 bg-brand text-white text-xs rounded-full">저장</button>
                                    <button @click="cancelEdit" class="px-3 py-1 text-slate-500 text-xs">취소</button>
                                </div>
                            </div>

                            <!-- 일반 -->
                            <template v-else>
                                <p class="text-sm text-slate-700 mt-1 whitespace-pre-wrap break-words">{{ c.content }}</p>
                                <div v-if="canEdit(c) || canDelete(c)" class="flex gap-3 mt-1.5">
                                    <button v-if="canEdit(c)" @click="startEdit(c)" class="text-xs text-slate-400 hover:text-brand">수정</button>
                                    <button v-if="canDelete(c)" @click="removeComment(c)" class="text-xs text-slate-400 hover:text-red-500">삭제</button>
                                </div>
                            </template>
                        </div>
                    </li>
                    <li v-if="post.comments.length === 0" class="text-center text-sm text-slate-400 py-8">
                        첫 댓글을 남겨보세요.
                    </li>
                </ul>
            </section>
                </template>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 스켈레톤 shimmer 애니메이션 */
.skeleton {
    background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}

.blog-content :deep(.ql-editor) {
    font-size: 1.05rem;
    line-height: 1.9;
    color: #1e293b;
}
</style>
