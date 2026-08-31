<template>
  <div class="post-detail">
    <header class="header">
      <router-link to="/community" class="back">← 社区</router-link>
      <h2>帖子详情</h2>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else class="content">
      <div class="post-card">
        <div class="post-header">
          <span class="post-type">{{ getTypeName(post.type) }}</span>
          <span class="post-time">{{ formatTime(post.createTime) }}</span>
        </div>
        <h2>{{ post.title || '无标题' }}</h2>
        <div class="post-content">{{ post.content }}</div>
        <div v-if="post.topicTag" class="tag">#{{ post.topicTag }}</div>
        <div class="post-footer">
          <button class="btn-like" @click="likePost">❤ {{ post.likeCount }}</button>
          <span>💬 {{ post.commentCount }}</span>
        </div>
      </div>

      <div class="comment-section">
        <h3>评论 ({{ post.commentCount }})</h3>
        <div class="comment-input">
          <textarea v-model="commentContent" placeholder="写下你的评论..." rows="3"></textarea>
          <button @click="submitComment" :disabled="!commentContent">发表</button>
        </div>
        <div v-for="comment in commentList" :key="comment.id" class="comment-item">
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
        </div>
        <div v-if="commentList.length === 0" class="empty">暂无评论</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../api/index.js'

const route = useRoute()
const post = ref({})
const commentList = ref([])
const commentContent = ref('')
const loading = ref(true)

const getTypeName = (type) => {
  const map = { 1: '渔获', 2: '装备', 3: '钓法', 4: '比赛', 5: '求助' }
  return map[type] || '其他'
}

const fetchDetail = async () => {
  try {
    const res = await request.get(`/post/detail/${route.params.id}`)
    if (res.code === 200) {
      post.value = res.data
    }
    const commentRes = await request.get('/post/comments', {
      params: { postId: route.params.id, page: 1, size: 20 }
    })
    if (commentRes.code === 200) {
      commentList.value = commentRes.data.list || []
    }
  } catch (e) {
    console.error('获取详情失败', e)
  } finally {
    loading.value = false
  }
}

const likePost = async () => {
  try {
    await request.post(`/post/like/${route.params.id}`)
    post.value.likeCount++
  } catch (e) {
    console.error('点赞失败', e)
  }
}

const submitComment = async () => {
  try {
    const res = await request.post('/post/comment', null, {
      params: { postId: route.params.id, content: commentContent.value }
    })
    if (res.code === 200) {
      commentContent.value = ''
      fetchDetail()
    }
  } catch (e) {
    console.error('评论失败', e)
  }
}

const formatTime = (time) => time ? time.substring(0, 16).replace('T', ' ') : ''

onMounted(fetchDetail)
</script>

<style scoped>
.post-detail { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.loading { text-align: center; padding: 48px; color: #999; }
.content { max-width: 800px; margin: 24px auto; padding: 0 16px; }
.post-card {
  background: #fff; border-radius: 12px; padding: 24px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.post-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.post-type {
  background: #e8f0fe; color: #1a73e8; padding: 2px 8px;
  border-radius: 4px; font-size: 12px;
}
.post-time { font-size: 12px; color: #999; }
.post-card h2 { margin: 0 0 16px; }
.post-content { line-height: 1.8; color: #333; white-space: pre-wrap; }
.tag {
  display: inline-block; background: #e8f0fe; color: #1a73e8;
  padding: 4px 12px; border-radius: 4px; margin-top: 12px; font-size: 13px;
}
.post-footer { margin-top: 16px; display: flex; gap: 16px; }
.btn-like {
  padding: 6px 16px; border: 1px solid #ff4444; background: #fff;
  color: #ff4444; border-radius: 6px; cursor: pointer;
}
.comment-section {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.comment-section h3 { margin: 0 0 16px; }
.comment-input { margin-bottom: 20px; }
.comment-input textarea {
  width: 100%; padding: 10px; border: 1px solid #ddd;
  border-radius: 6px; resize: vertical; box-sizing: border-box;
}
.comment-input button {
  margin-top: 8px; padding: 8px 24px; background: #1a73e8;
  color: #fff; border: none; border-radius: 6px; cursor: pointer;
}
.comment-item {
  padding: 12px 0; border-bottom: 1px solid #eee;
}
.comment-content { color: #333; margin-bottom: 4px; }
.comment-time { font-size: 12px; color: #999; }
.empty { text-align: center; padding: 24px; color: #999; }
</style>
