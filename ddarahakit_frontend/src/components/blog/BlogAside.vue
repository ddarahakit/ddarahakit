<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/blog'

const route = useRoute()
const router = useRouter()

// aside 데이터 (카테고리별 글 수 / 최신글) — 컴포넌트가 스스로 조회
const categories = ref([])
const recentPosts = ref([])
const isLoading = ref(true)

// 목록 페이지에서 현재 선택된 카테고리(상세 페이지에선 없음 → 강조 없음)
const activeCategory = computed(() => route.query.category || '')

const fetchAside = async () => {
    isLoading.value = true
    const [cat, recent] = await Promise.all([api.getCategories(), api.getRecent(5)])
    if (cat && cat.success) categories.value = cat.results || []
    if (recent && recent.success) recentPosts.value = recent.results || []
    isLoading.value = false
}
fetchAside()

/**
 * 카테고리 선택 (null → 전체). 항상 목록 페이지로 이동.
 */
const selectCategory = (cat) => {
    const q = {}
    if (cat) q.category = cat
    if (route.query.keyword) q.keyword = route.query.keyword
    router.push({ name: 'blogList', query: q })
}

const goDetail = (idx) => router.push({ name: 'blogDetail', params: { idx } })
</script>

<template>
    <aside class="lg:w-64 lg:shrink-0 space-y-6">
        <!-- 카테고리 -->
        <div class="bg-white border border-slate-100 rounded-2xl p-5 shadow-sm">
            <h3 class="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-folder-tree text-brand"></i>카테고리
            </h3>
            <!-- 로딩 (스켈레톤) -->
            <ul v-if="isLoading" class="space-y-1">
                <li v-for="n in 5" :key="n" class="px-3 py-2">
                    <div class="h-4 rounded bg-slate-100 skeleton" :style="{ width: 55 + (n % 3) * 12 + '%' }"></div>
                </li>
            </ul>
            <ul v-else class="space-y-1">
                <li>
                    <button @click="selectCategory('')"
                        class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-sm transition"
                        :class="!activeCategory ? 'bg-blue-50 text-brand font-semibold' : 'text-slate-600 hover:bg-slate-50'">
                        <span>전체</span>
                    </button>
                </li>
                <li v-for="c in categories" :key="c.category">
                    <button @click="selectCategory(c.category)"
                        class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-sm transition"
                        :class="activeCategory === c.category ? 'bg-blue-50 text-brand font-semibold' : 'text-slate-600 hover:bg-slate-50'">
                        <span>{{ c.category }}</span>
                        <span class="text-xs text-slate-400">{{ c.count }}</span>
                    </button>
                </li>
                <li v-if="categories.length === 0" class="px-3 py-2 text-xs text-slate-400">
                    아직 카테고리가 없습니다.
                </li>
            </ul>
        </div>

        <!-- 최신글 -->
        <div class="bg-white border border-slate-100 rounded-2xl p-5 shadow-sm">
            <h3 class="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-clock-rotate-left text-brand"></i>최신글
            </h3>
            <!-- 로딩 (스켈레톤) -->
            <ul v-if="isLoading" class="space-y-3">
                <li v-for="n in 5" :key="n" class="space-y-1.5">
                    <div class="w-full h-3.5 rounded bg-slate-100 skeleton"></div>
                    <div class="w-1/3 h-3 rounded bg-slate-100 skeleton"></div>
                </li>
            </ul>
            <ul v-else class="space-y-3">
                <li v-for="r in recentPosts" :key="r.idx">
                    <button @click="goDetail(r.idx)" class="text-left w-full group">
                        <p class="text-sm text-slate-700 group-hover:text-brand line-clamp-2 transition-colors">{{ r.title }}</p>
                        <p class="text-xs text-slate-400 mt-1">{{ r.createdAt }}</p>
                    </button>
                </li>
                <li v-if="recentPosts.length === 0" class="text-xs text-slate-400">
                    아직 글이 없습니다.
                </li>
            </ul>
        </div>
    </aside>
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
