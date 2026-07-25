<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/blog'
import useAuthStore from '@/stores/useAuthStore'
import BlogAside from '@/components/blog/BlogAside.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 관리자 여부 → 글쓰기/수정/삭제 노출 제어 (실제 권한은 백엔드가 최종 강제)
const isAdmin = computed(() => authStore.getUserRole() === 'ROLE_ADMIN')

const posts = ref([])
const pageInfo = ref({ page: 0, totalPages: 0, hasNext: false, hasPrev: false, totalPosts: 0 })
const isLoading = ref(true)
const isError = ref(false)

// 현재 페이지(1-base) / 카테고리 / 검색어 — URL 쿼리와 동기화
const currentPage = computed(() => Number(route.query.page) || 1)
const activeCategory = computed(() => route.query.category || '')
const keyword = ref(route.query.keyword || '')

/**
 * 블로그 글 목록 조회
 */
const fetchList = async () => {
    isLoading.value = true
    isError.value = false
    const data = await api.blogList({
        page: currentPage.value,
        size: 9,
        category: route.query.category,
        keyword: route.query.keyword
    })
    if (data && data.success && data.results) {
        posts.value = data.results.posts || []
        pageInfo.value = {
            page: data.results.page,
            totalPages: data.results.totalPages,
            hasNext: data.results.hasNext,
            hasPrev: data.results.hasPrev,
            totalPosts: data.results.totalPosts
        }
    } else {
        isError.value = true
    }
    isLoading.value = false
}

// 페이지/카테고리/검색어 변경 시 재조회
watch(() => route.query, fetchList, { immediate: true })

/**
 * 페이지 이동
 */
const goPage = (p) => {
    if (p < 1 || (pageInfo.value.totalPages && p > pageInfo.value.totalPages)) return
    router.push({ name: 'blogList', query: { ...route.query, page: p } })
}

/**
 * 카테고리 선택 (null → 전체)
 */
const selectCategory = (cat) => {
    const q = {}
    if (cat) q.category = cat
    if (route.query.keyword) q.keyword = route.query.keyword
    router.push({ name: 'blogList', query: q })
}

/**
 * 검색
 */
const search = () => {
    const kw = keyword.value.trim()
    const q = {}
    if (activeCategory.value) q.category = activeCategory.value
    if (kw) q.keyword = kw
    router.push({ name: 'blogList', query: q })
}

const goWrite = () => router.push({ name: 'blogReg' })
const goDetail = (idx) => router.push({ name: 'blogDetail', params: { idx } })
</script>

<template>
    <div class="max-w-7xl mx-auto px-6 pt-28 pb-24">
        <!-- 헤더 -->
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
            <div class="flex-1 min-w-0">
                <!-- 검색 + 현재 필터 -->
                <div class="flex items-center gap-3 mb-8 flex-wrap">
                    <form @submit.prevent="search" class="flex items-center bg-gray-100/80 rounded-full px-4 py-2.5 flex-1 min-w-[220px]">
                        <i class="fa-solid fa-magnifying-glass text-gray-400 text-sm"></i>
                        <input v-model="keyword" type="search" placeholder="제목·내용 검색"
                            class="bg-transparent outline-none text-sm ml-2 w-full text-gray-700 placeholder-gray-400" />
                    </form>
                    <span v-if="activeCategory"
                        class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-50 text-brand text-sm rounded-full">
                        <i class="fa-solid fa-tag text-xs"></i>{{ activeCategory }}
                        <button @click="selectCategory('')" class="hover:text-blue-700"><i class="fa-solid fa-xmark"></i></button>
                    </span>
                </div>

                <!-- 로딩 (스켈레톤) -->
                <div v-if="isLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    <div v-for="n in 6" :key="n"
                        class="bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-sm">
                        <div class="h-40 bg-slate-100 skeleton"></div>
                        <div class="p-5">
                            <div class="w-16 h-5 rounded-full bg-slate-100 skeleton mb-3"></div>
                            <div class="w-3/4 h-5 rounded bg-slate-100 skeleton mb-2"></div>
                            <div class="w-1/2 h-5 rounded bg-slate-100 skeleton mb-4"></div>
                            <div class="space-y-2 mb-5">
                                <div class="w-full h-3.5 rounded bg-slate-100 skeleton"></div>
                                <div class="w-2/3 h-3.5 rounded bg-slate-100 skeleton"></div>
                            </div>
                            <div class="flex items-center justify-between">
                                <div class="w-16 h-3 rounded bg-slate-100 skeleton"></div>
                                <div class="w-24 h-3 rounded bg-slate-100 skeleton"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 에러 -->
                <div v-else-if="isError" class="text-center text-slate-400 py-24">
                    글을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
                </div>

                <!-- 빈 목록 -->
                <div v-else-if="posts.length === 0" class="text-center text-slate-400 py-24">
                    <i class="fa-regular fa-newspaper text-4xl mb-4 block"></i>
                    {{ activeCategory ? '이 카테고리에 글이 없습니다.' : '아직 등록된 글이 없습니다.' }}
                </div>

                <!-- 목록 -->
                <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    <article v-for="post in posts" :key="post.idx" @click="goDetail(post.idx)"
                        class="group cursor-pointer bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-sm hover:shadow-lg hover:-translate-y-0.5 transition">
                        <div class="h-40 bg-slate-100 overflow-hidden">
                            <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title"
                                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                            <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 text-brand">
                                <i class="fa-regular fa-newspaper text-3xl opacity-60"></i>
                            </div>
                        </div>
                        <div class="p-5">
                            <span v-if="post.category"
                                class="inline-block text-xs font-medium text-brand bg-blue-50 px-2 py-0.5 rounded-full mb-2">
                                {{ post.category }}
                            </span>
                            <h2 class="font-bold text-slate-900 line-clamp-2 group-hover:text-brand transition-colors">
                                {{ post.title }}
                            </h2>
                            <p v-if="post.summary" class="text-sm text-slate-500 mt-2 line-clamp-2">{{ post.summary }}</p>
                            <div class="flex items-center justify-between text-xs text-slate-400 mt-4">
                                <span>{{ post.userName }}</span>
                                <span>{{ post.createdAt }} · 조회 {{ post.viewCount }}</span>
                            </div>
                        </div>
                    </article>
                </div>

                <!-- 페이지네이션 -->
                <div v-if="!isLoading && pageInfo.totalPages > 1" class="flex items-center justify-center gap-2 mt-12">
                    <button @click="goPage(currentPage - 1)" :disabled="!pageInfo.hasPrev"
                        class="w-9 h-9 rounded-full border border-slate-200 text-slate-500 disabled:opacity-30 hover:border-brand hover:text-brand transition">
                        <i class="fa-solid fa-chevron-left text-xs"></i>
                    </button>
                    <span class="text-sm text-slate-500 px-3">{{ currentPage }} / {{ pageInfo.totalPages }}</span>
                    <button @click="goPage(currentPage + 1)" :disabled="!pageInfo.hasNext"
                        class="w-9 h-9 rounded-full border border-slate-200 text-slate-500 disabled:opacity-30 hover:border-brand hover:text-brand transition">
                        <i class="fa-solid fa-chevron-right text-xs"></i>
                    </button>
                </div>
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
</style>
