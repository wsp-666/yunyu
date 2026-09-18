<template>
  <div class="home">
    <AppHeader v-model:keyword="keyword" @search="search" />

    <section class="hero">
      <div class="hero-bg" aria-hidden="true"></div>
      <div class="hero-inner">
        <div class="hero-copy">
          <p class="eyebrow">智能钓场平台</p>
          <h1>云渔</h1>
          <p class="lead">发现身边好钓场，跟竿友分享渔获，让每一次出钓更有把握。</p>
          <div class="hero-actions">
            <button class="yy-btn" type="button" @click="scrollToList">浏览钓场</button>
            <router-link class="yy-btn yy-btn-ghost light" to="/ai-chat">问问 AI 大师</router-link>
          </div>
        </div>
        <div class="hero-visual" aria-hidden="true">
          <div class="ripple r1"></div>
          <div class="ripple r2"></div>
          <div class="ripple r3"></div>
          <div class="lake">
            <svg viewBox="0 0 420 280" preserveAspectRatio="xMidYMid slice">
              <defs>
                <linearGradient id="water" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#1a9aaa" />
                  <stop offset="100%" stop-color="#0c4a56" />
                </linearGradient>
              </defs>
              <rect width="420" height="280" fill="url(#water)" />
              <path d="M0 170 Q70 140 140 165 T280 160 T420 150 V280 H0Z" fill="rgba(255,255,255,0.12)" />
              <path d="M0 200 Q105 175 210 195 T420 185 V280 H0Z" fill="rgba(255,255,255,0.08)" />
              <circle cx="320" cy="70" r="28" fill="#fbbf24" opacity="0.85" />
            </svg>
          </div>
        </div>
      </div>
    </section>

    <section class="feature-strip">
      <div class="feature-inner">
        <router-link v-for="f in features" :key="f.to" :to="f.to" class="feature-item">
          <span class="feature-title">{{ f.title }}</span>
          <span class="feature-desc">{{ f.desc }}</span>
        </router-link>
      </div>
    </section>

    <main class="main" ref="listRef">
      <div class="section-head">
        <div>
          <h2>附近钓场</h2>
          <p>关注放鱼动态，挑准下竿时机</p>
        </div>
        <router-link to="/spot-share" class="section-link">查看钓点 →</router-link>
      </div>

      <div v-if="venueList.length" class="venue-list">
        <article
          v-for="(venue, index) in venueList"
          :key="venue.id"
          class="venue-card"
          :style="{ animationDelay: `${index * 40}ms` }"
          @click="goDetail(venue.id)"
        >
          <div class="card-image">
            <img :src="venue.coverImage || placeholder" :alt="venue.name" />
            <span v-if="venue.isNew" class="badge-new">新开</span>
          </div>
          <div class="card-info">
            <h3>{{ venue.name }}</h3>
            <p class="address">{{ venue.address }}</p>
            <div class="tags">
              <span v-if="venue.latestFishSpecies" class="tag accent">{{ venue.latestFishSpecies }}</span>
              <span class="tag">钓位 {{ venue.totalSeats }}</span>
            </div>
            <div class="card-footer">
              <span class="followers">{{ venue.followerCount || 0 }} 人关注</span>
              <span v-if="venue.latestStockTime" class="stock-time">
                放鱼 {{ formatTime(venue.latestStockTime) }}
              </span>
            </div>
          </div>
        </article>
      </div>
      <div v-else class="yy-empty">暂无钓场，试试换个关键词</div>

      <div v-if="totalCount > pageSize" class="pagination">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>{{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </main>

    <SiteFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index.js'
import AppHeader from '../components/AppHeader.vue'
import SiteFooter from '../components/SiteFooter.vue'

const features = [
  { to: '/community', title: '渔获社区', desc: '晒渔获、聊钓法' },
  { to: '/weather', title: '出钓天气', desc: '气压风力一览' },
  { to: '/ai/identify', title: 'AI 识鱼', desc: '拍照识别鱼种' },
  { to: '/shop', title: '渔具商城', desc: '积分也能换' }
]

const router = useRouter()
const venueList = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const listRef = ref(null)

const placeholder =
  'data:image/svg+xml,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#127a8a"/><stop offset="1" stop-color="#0c4a56"/></linearGradient></defs><rect width="100%" height="100%" fill="url(#g)"/><text x="50%" y="52%" fill="rgba(255,255,255,.7)" font-size="28" text-anchor="middle" font-family="sans-serif">云渔钓场</text></svg>`
  )

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize.value)))

const fetchVenues = async () => {
  try {
    const res = await request.get('/venue/list', {
      params: { currentPage: currentPage.value, pageSize: pageSize.value, keyword: keyword.value }
    })
    if (res.code === 200) {
      venueList.value = res.data.list || []
      totalCount.value = res.data.totalCount || 0
    }
  } catch (e) {
    console.error('获取钓场列表失败', e)
  }
}

const search = () => {
  currentPage.value = 1
  fetchVenues()
  listRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const changePage = (page) => {
  currentPage.value = page
  fetchVenues()
}

const goDetail = (id) => router.push(`/venue/${id}`)

const formatTime = (time) => {
  if (!time) return ''
  return time.substring(0, 16).replace('T', ' ')
}

const scrollToList = () => {
  listRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(fetchVenues)
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.hero {
  position: relative;
  overflow: hidden;
  animation: yy-fade-up 0.5s ease;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 420px at 85% 20%, rgba(251, 191, 36, 0.18), transparent 55%),
    linear-gradient(135deg, #0b2a32 0%, #0c4a56 45%, #127a8a 100%);
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 1240px;
  margin: 0 auto;
  padding: 56px 20px 48px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 28px;
  align-items: center;
}

.eyebrow {
  color: #fbbf24;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  margin-bottom: 10px;
}

.hero-copy h1 {
  font-family: var(--yy-display);
  font-size: clamp(52px, 9vw, 80px);
  color: #fff;
  line-height: 1;
  margin-bottom: 14px;
}

.lead {
  max-width: 420px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 16px;
  line-height: 1.7;
  margin-bottom: 22px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.yy-btn-ghost.light {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.06);
}

.yy-btn-ghost.light:hover {
  background: rgba(255, 255, 255, 0.14);
  box-shadow: none;
}

.feature-strip {
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid var(--yy-line);
  backdrop-filter: blur(8px);
}

.feature-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 12px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.feature-item {
  padding: 22px 18px;
  border-right: 1px solid var(--yy-line);
  transition: background 0.2s;
}

.feature-item:last-child { border-right: none; }
.feature-item:hover { background: rgba(18, 122, 138, 0.06); }

.feature-title {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: var(--yy-deep);
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 12px;
  color: var(--yy-muted);
}

.hero-visual {
  position: relative;
  min-height: 260px;
  display: grid;
  place-items: center;
}

.lake {
  width: min(100%, 420px);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: var(--yy-shadow-lg);
  transform: rotate(-1.5deg);
  animation: yy-fade-up 0.7s ease 0.1s both;
}

.ripple {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 50%;
  animation: yy-ripple 3.2s ease-out infinite;
}

.r1 { width: 180px; height: 180px; }
.r2 { width: 260px; height: 260px; animation-delay: 0.8s; }
.r3 { width: 340px; height: 340px; animation-delay: 1.6s; }

.main {
  max-width: 1240px;
  margin: 8px auto 0;
  padding: 0 20px;
  flex: 1;
  width: 100%;
}

.section-head {
  margin: 28px 0 20px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.section-head h2 {
  font-size: 22px;
  color: var(--yy-deep);
  margin-bottom: 4px;
}

.section-head p {
  color: var(--yy-muted);
  font-size: 13px;
}

.section-link {
  color: var(--yy-primary);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.venue-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.venue-card {
  background: var(--yy-surface);
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--yy-shadow);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  animation: yy-fade-up 0.45s ease both;
}

.venue-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--yy-shadow-lg);
}

.card-image {
  position: relative;
  height: 176px;
  overflow: hidden;
  background: var(--yy-deep);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.venue-card:hover .card-image img {
  transform: scale(1.04);
}

.badge-new {
  position: absolute;
  top: 12px;
  left: 12px;
  background: var(--yy-accent);
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.card-info {
  padding: 16px 16px 18px;
}

.card-info h3 {
  font-size: 17px;
  margin-bottom: 6px;
  color: var(--yy-ink);
}

.address {
  color: var(--yy-muted);
  font-size: 13px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  background: rgba(18, 122, 138, 0.1);
  color: var(--yy-primary);
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.tag.accent {
  background: var(--yy-accent-soft);
  color: #92400e;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--yy-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid var(--yy-line);
  background: var(--yy-surface);
  border-radius: 999px;
  cursor: pointer;
  color: var(--yy-deep);
}

.pagination button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 860px) {
  .hero-inner {
    grid-template-columns: 1fr;
    padding: 36px 20px 40px;
  }
  .hero-visual {
    order: -1;
    min-height: 200px;
  }
  .lake {
    width: min(100%, 340px);
    transform: none;
  }
  .feature-inner {
    grid-template-columns: 1fr 1fr;
  }
  .feature-item {
    border-right: none;
    border-bottom: 1px solid var(--yy-line);
  }
}
</style>
