import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

// 블로그 글 등록/수정 공통 바디
const toBlogInfo = (req) => ({
    title: req.title,
    category: req.category || '',
    summary: req.summary || '',
    thumbnailUrl: req.thumbnailUrl || '',
    content: req.content
})

/** 블로그 글 목록 조회 (page 는 1-base 입력 → 0-base 로 변환) */
const blogList = (req = {}) => {
    const page = req.page || 1
    const size = req.size || 9
    const params = {
        page: page - 1,
        size,
        category: req.category || undefined,
        keyword: req.keyword || undefined
    }
    return request($axios.get('/blog/list', { params }))
}

/** 카테고리별 글 수 (aside) */
const getCategories = () => request($axios.get('/blog/categories'))

/** 최신 글 목록 (aside) */
const getRecent = (limit = 5) => request($axios.get('/blog/recent', { params: { limit } }))

/** 블로그 글 상세 조회 */
const getBlogDetail = (idx) => request($axios.get(`/blog/${idx}`))

/** 블로그 글 작성하기 (관리자) */
const blogCreate = (req) => request($axios.post('/blog', toBlogInfo(req)))

/** 블로그 글 수정하기 (관리자) */
const blogUpdate = (idx, req) => request($axios.put(`/blog/${idx}`, toBlogInfo(req)))

/** 블로그 글 삭제하기 (관리자) */
const blogDelete = (idx) => request($axios.delete(`/blog/${idx}`))

/** 댓글 작성 (로그인 사용자) */
const commentCreate = (blogIdx, content) => request($axios.post(`/blog/${blogIdx}/comment`, { content }))

/** 댓글 수정 (작성자) */
const commentUpdate = (commentIdx, content) => request($axios.put(`/blog/comment/${commentIdx}`, { content }))

/** 댓글 삭제 (작성자 또는 관리자) */
const commentDelete = (commentIdx) => request($axios.delete(`/blog/comment/${commentIdx}`))

export default {
    blogList,
    getCategories,
    getRecent,
    getBlogDetail,
    blogCreate,
    blogUpdate,
    blogDelete,
    commentCreate,
    commentUpdate,
    commentDelete
}
