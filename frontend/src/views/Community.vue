<template>
  <div class="community">
    <PageBar title="社区">
      <router-link to="/post/create" class="yy-btn btn-publish">发布</router-link>
    </PageBar>

    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="String(tab.value)"
        :class="{ active: currentType === tab.value }"
        @click="currentType = tab.value; fetchPosts()"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="post-list">
      <article
        v-for="post in postList"
        :key="post.id"
        class="post-card"
        @click="goDetail(post.id)"
      >
        <div class="post-header">
          <span class="post-type">{{ getTypeName(post.type) }}</span>
          <span class="post-time">{{ formatTime(post.createTime) }}</span>
        </div>
        <h3>{{ post.title || '无标题' }}</h3>
        <p class="post-content">
          {{ post.content.substring(0, 100) }}{{ post.content.length > 100 ? '...' : '' }}
        </p>
        <div class="post-footer">
          <span>{{ post.likeCount }} 赞</span>
          <span>{{ post.commentCount }} 评论</span>
          <span v-if="post.topicTag">#{{ post.topicTag }}</span>
        </div>
      </article>
    </div>

    <div v-if="postList.length === 0" class="yy-empty">暂无帖子，来发第一条吧</div>
    <SiteFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index.js'
import PageBar from '../components/PageBar.vue'
import SiteFooter from '../components/SiteFooter.vue'

const router = useRouter()
const postList = ref([])
const currentType = ref(null)

const tabs = [
  { label: '全部', value: null },
  { label: '渔获', value: 1 },
  { label: '装备', value: 2 },
  { label: '钓法', value: 3 },
  { label: '比赛', value: 4 },
  { label: '求助', value: 5 }
]

const getTypeName = (type) => {
  const map = { 1: '渔获', 2: '装备', 3: '钓法', 4: '比赛', 5: '求助' }
  return map[type] || '其他'
}

const fetchPosts = async () => {
  try {
    const res = await request.get('/post/list', {
      params: { type: currentType.value, page: 1, size: 20 }
    })
    if (res.code === 200) {
      postList.value = res.data.list || []
    }
  } catch (e) {
    console.error('获取帖子列表失败', e)
  }
}

const goDetail = (id) => router.push(`/post/${id}`)
const formatTime = (time) => (time ? time.substring(0, 16).replace('T', ' ') : '')

onMounted(fetchPosts)
</script>

<style scoped>
.community {
  min-height: 100vh;
}

.btn-publish {
  padding: 8px 16px;
  font-size: 13px;
}

.tabs {
  display: flex;
  gap: 8px;
  padding: 14px 20px;
  overflow-x: auto;
  border-bottom: 1px solid var(--yy-line);
  background: rgba(255, 255, 255, 0.65);
}

.tabs button {
  padding: 7px 16px;
  border: 1px solid var(--yy-line);
  background: var(--yy-surface);
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  color: var(--yy-muted);
  font-size: 13px;
}

.tabs button.active {
  background: var(--yy-primary);
  color: #fff;
  border-color: var(--yy-primary);
}

.post-list {
  max-width: 800px;
  margin: 16px auto;
  padding: 0 16px 40px;
}

.post-card {
  background: var(--yy-surface);
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius);
  padding: 18px;
  margin-bottom: 12px;
  box-shadow: var(--yy-shadow);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  animation: yy-fade-up 0.35s ease both;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--yy-shadow-lg);
}

.post-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.post-type {
  background: rgba(18, 122, 138, 0.1);
  color: var(--yy-primary);
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.post-time {
  font-size: 12px;
  color: var(--yy-muted);
}

.post-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--yy-ink);
}

.post-content {
  color: var(--yy-muted);
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px;
}

.post-footer {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--yy-muted);
}
</style>
