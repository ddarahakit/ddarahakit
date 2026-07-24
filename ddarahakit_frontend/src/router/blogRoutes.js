/**
 * 블로그 라우트
 *
 * 목록/상세는 공개, 작성/수정은 로그인 필요(실제 권한은 관리자 — 뷰·백엔드에서 강제).
 */
const blogRoutes = {
    path: '/blog',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
        {
            name: 'blogList',
            path: '',
            component: () => import('@/views/blog/BlogList.vue'),
            meta: {
                title: '블로그 | 따라학잇',
                requiresAuth: false
            }
        },
        {
            name: 'blogReg',
            path: 'reg',
            component: () => import('@/views/blog/BlogReg.vue'),
            meta: {
                title: '블로그 글쓰기 | 따라학잇',
                requiresAuth: true
            }
        },
        {
            name: 'blogEdit',
            path: 'edit/:idx(\\d+)',
            component: () => import('@/views/blog/BlogReg.vue'),
            meta: {
                title: '블로그 글수정 | 따라학잇',
                requiresAuth: true
            }
        },
        {
            name: 'blogDetail',
            path: ':idx(\\d+)',
            component: () => import('@/views/blog/BlogDetail.vue'),
            meta: {
                requiresAuth: false
            }
        }
    ]
}

export default blogRoutes
