<template>
  <div class="venue-detail">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>{{ venue?.name || '钓场详情' }}</h2>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else class="content">
      <!-- 放鱼置顶信息 -->
      <div v-if="latestStockVideo" class="stock-section">
        <h3>📢 最新放鱼</h3>
        <video v-if="latestStockVideo.videoUrl" :src="latestStockVideo.videoUrl" controls class="stock-video"></video>
        <div class="stock-info">
          <span>鱼种: {{ latestStockVideo.fishSpecies }}</span>
          <span>数量: {{ latestStockVideo.fishCount }}斤</span>
          <span>正钓时间: {{ formatTime(latestStockVideo.fishingTime) }}</span>
          <span>票价: ¥{{ latestStockVideo.ticketPrice }}</span>
        </div>
      </div>

      <!-- 钓场基本信息 -->
      <div class="info-section">
        <h3>📍 钓场信息</h3>
        <div v-if="venue?.coverImage" class="cover-image-wrap">
          <img :src="venue.coverImage" :alt="venue.name" class="cover-image" />
        </div>
        <div v-if="imageList.length > 0" class="images-gallery">
          <img v-for="(url, idx) in imageList" :key="idx" :src="url" class="gallery-img" />
        </div>
        <p>地址: {{ venue?.address }}</p>
        <p>电话: {{ venue?.phone }}</p>
        <p>
          <button class="btn-navigate" @click="openNavigation">
            🧭 导航到这里
          </button>
        </p>
        <p>钓位数: {{ venue?.totalSeats }}个</p>
        <p>关注: {{ venue?.followerCount }}人</p>
        <p class="rule">{{ venue?.ruleDesc }}</p>
        <button class="btn-follow" @click="toggleFollow">
          {{ isFollowed ? '已关注' : '关注' }}
        </button>
      </div>

      <!-- 可报名场次 -->
      <div class="session-section">
        <h3>🎫 可报名场次</h3>
        <div v-for="session in sessionList" :key="session.id" class="session-card">
          <div class="session-info">
            <h4>{{ session.name }}</h4>
            <p>时间: {{ formatTime(session.startTime) }} - {{ formatTime(session.endTime) }}</p>
            <p>票价: ¥{{ session.ticketPrice }} | 剩余: {{ session.remainSeats }}/{{ session.totalSeats }}位</p>
          </div>
          <button class="btn-buy" @click="buyTicket(session.id)" :disabled="session.remainSeats <= 0">
            {{ getSessionBtnText(session.id) }}
          </button>
        </div>
      </div>

      <!-- 上鱼实况 -->
      <div class="catch-section">
        <h3>🎣 上鱼实况</h3>
        <div v-if="catchVideoList.length === 0" class="empty">暂无上鱼视频</div>
        <div v-for="video in catchVideoList" :key="video.id" class="video-card">
          <video v-if="video.videoUrl" :src="video.videoUrl" controls class="catch-video"></video>
          <div v-else class="video-placeholder">🎬 上鱼视频</div>
          <div class="video-info">
            <span>❤ {{ video.likeCount }}</span>
            <span>{{ formatTime(video.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/index.js'

const route = useRoute()
const router = useRouter()
const venue = ref(null)
const latestStockVideo = ref(null)
const catchVideoList = ref([])
const sessionList = ref([])
const loading = ref(true)
const isFollowed = ref(false)

const fetchDetail = async () => {
  try {
    const res = await request.get(`/venue/detail/${route.params.id}`)
    if (res.code === 200) {
      venue.value = res.data
      latestStockVideo.value = res.data.latestStockVideo
      catchVideoList.value = res.data.catchVideoList || []
      sessionList.value = res.data.sessionList || []
      await checkMyOrders()
    }
  } catch (e) {
    console.error('获取详情失败', e)
  } finally {
    loading.value = false
  }
}

const toggleFollow = async () => {
  try {
    const res = await request.post('/follow/toggle', null, {
      params: { targetType: 2, targetId: route.params.id }
    })
    if (res.code === 200) {
      isFollowed.value = res.data
      if (venue.value) {
        venue.value.followerCount += isFollowed.value ? 1 : -1
      }
    }
  } catch (e) {
    console.error('关注失败', e)
  }
}

const myOrders = ref({})

const checkMyOrders = async () => {
  const token = localStorage.getItem('token')
  if (!token || !sessionList.value.length) return
  for (const s of sessionList.value) {
    try {
      const res = await request.get('/order/my-session', { params: { sessionId: s.id } })
      if (res.code === 200 && res.data) {
        myOrders.value[s.id] = res.data
      }
    } catch (e) { /* ignore */ }
  }
}

const getSessionBtnText = (sessionId) => {
  const order = myOrders.value[sessionId]
  if (order) return order.seatNo ? `${order.seatNo}号` : '查看订单'
  const s = sessionList.value.find(x => x.id === sessionId)
  if (s && s.remainSeats <= 0) return '已满'
  return '购票'
}

const buyTicket = (sessionId) => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  router.push(`/order?sessionId=${sessionId}&venueId=${route.params.id}`)
}

const imageList = computed(() => {
  if (!venue.value?.images) return []
  return venue.value.images.split(',').filter(Boolean)
})

const openNavigation = () => {
  if (!venue.value) return
  const { longitude, latitude, name, address } = venue.value
  const lon = parseFloat(longitude)
  const lat = parseFloat(latitude)
  if (isNaN(lon) || isNaN(lat) || (lon === 0 && lat === 0)) {
    if (address) {
      window.open(`https://uri.amap.com/navigation?to=${encodeURIComponent(address)}&mode=car&callnative=1`, '_blank')
    } else {
      alert('该钓场暂无坐标信息')
    }
    return
  }
  window.open(`https://uri.amap.com/navigation?to=${lon},${lat},${encodeURIComponent(name)}&mode=car&callnative=1`, '_blank')
}

const formatTime = (time) => {
  if (!time) return ''
  return time.substring(0, 16).replace('T', ' ')
}

onMounted(fetchDetail)
</script>

<style scoped>
.venue-detail { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; font-size: 20px; }
.loading { text-align: center; padding: 48px; color: #999; }
.content { max-width: 800px; margin: 24px auto; padding: 0 16px; }
.stock-section, .info-section, .session-section, .catch-section {
  background: #fff; border-radius: 12px; padding: 20px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
h3 { margin: 0 0 16px; font-size: 18px; color: #333; }
.stock-info { display: flex; flex-wrap: wrap; gap: 12px; }
.stock-info span { background: #e8f0fe; padding: 4px 12px; border-radius: 4px; font-size: 14px; }
.info-section p { margin: 8px 0; color: #555; font-size: 14px; }
.rule { background: #f9f9f9; padding: 12px; border-radius: 8px; margin-top: 12px; }
.btn-follow {
  margin-top: 12px; padding: 8px 24px; border: 1px solid #1a73e8;
  background: #fff; color: #1a73e8; border-radius: 6px; cursor: pointer;
}
.btn-navigate {
  padding: 8px 24px; background: #4caf50; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 14px;
}
.btn-navigate:hover { background: #388e3c; }
.session-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px;
}
.session-info h4 { margin: 0 0 8px; }
.session-info p { margin: 4px 0; font-size: 13px; color: #666; }
.btn-buy {
  padding: 8px 24px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer;
}
.btn-buy:disabled { background: #ccc; cursor: not-allowed; }
.video-card {
  display: flex; align-items: center; gap: 16px;
  padding: 12px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px;
}
.stock-video { width: 100%; max-height: 300px; border-radius: 8px; margin-bottom: 12px; }
.catch-video { width: 120px; height: 80px; border-radius: 8px; object-fit: cover; }
.video-placeholder {
  width: 120px; height: 80px; background: #e8f0fe;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px; font-size: 24px; flex-shrink: 0;
}
.video-info { display: flex; gap: 16px; font-size: 13px; color: #666; }
.empty { text-align: center; padding: 24px; color: #999; }
.cover-image-wrap { margin-bottom: 16px; border-radius: 8px; overflow: hidden; }
.cover-image { width: 100%; max-height: 300px; object-fit: cover; display: block; }
.images-gallery { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.gallery-img { width: 100px; height: 75px; object-fit: cover; border-radius: 6px; cursor: pointer; }
</style>
