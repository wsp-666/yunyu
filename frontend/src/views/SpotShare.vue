<template>
  <div class="spot-share">
    <header class="header">
      <router-link to="/" class="back">← 首页</router-link>
      <h2>钓点分享</h2>
      <button class="btn-toggle" @click="showForm = !showForm">
        {{ showForm ? '查看钓点' : '分享钓点' }}
      </button>
    </header>

    <!-- 分享钓点表单 -->
    <div v-if="showForm" class="form-section">
      <h3>分享新钓点</h3>
      <div class="form-group">
        <label>钓点名称 *</label>
        <input v-model="form.name" placeholder="如：小清河野钓点" />
      </div>
      <div class="form-group">
        <label>类型</label>
        <select v-model="form.type">
          <option :value="1">野钓</option>
          <option :value="2">黑坑</option>
        </select>
      </div>
      <div class="form-group">
        <label>地图选点（点击地图设置经纬度）</label>
        <div id="spot-map-picker" class="map-picker"></div>
        <p class="map-hint">点击地图自动填入经纬度</p>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>经度 *</label>
          <input v-model="form.longitude" placeholder="如：116.397428" type="number" step="0.000001" />
        </div>
        <div class="form-group">
          <label>纬度 *</label>
          <input v-model="form.latitude" placeholder="如：39.90923" type="number" step="0.000001" />
        </div>
      </div>
      <div class="form-group">
        <label>主要鱼种</label>
        <input v-model="form.fishSpecies" placeholder="如：鲫鱼,鲤鱼,草鱼" />
      </div>
      <div class="form-group">
        <label>水质描述</label>
        <input v-model="form.waterQuality" placeholder="如：水质清澈" />
      </div>
      <div class="form-group">
        <label>水深描述</label>
        <input v-model="form.depthDesc" placeholder="如：1.5-3米" />
      </div>
      <div class="form-group">
        <label>收费描述</label>
        <input v-model="form.feeDesc" placeholder="如：免费 / 50元/天" />
      </div>
      <div class="form-group">
        <label>环境描述</label>
        <textarea v-model="form.envDesc" placeholder="描述一下钓点的环境..." rows="3"></textarea>
      </div>
      <div class="form-group">
        <label>导航指引</label>
        <textarea v-model="form.navigation" placeholder="如何到达该钓点的文字说明..." rows="2"></textarea>
      </div>
      <div class="form-group">
        <label>上传图片（可选多张，第一张作为封面）</label>
        <input type="file" multiple accept="image/*" @change="onSpotImagesChange" />
        <div v-if="spotImagePreviews.length > 0" class="image-previews">
          <div v-for="(url, idx) in spotImagePreviews" :key="idx" class="img-preview-item">
            <img :src="url" />
            <span v-if="idx === 0" class="cover-badge">封面</span>
          </div>
        </div>
        <p v-if="uploadingImages" class="map-hint">图片上传中...</p>
      </div>
      <button class="btn-submit" @click="submitSpot" :disabled="submitting || uploadingImages">
        {{ submitting ? '提交中...' : '提交审核' }}
      </button>
      <p v-if="submitMsg" class="submit-msg">{{ submitMsg }}</p>
    </div>

    <!-- 钓点展示 -->
    <div v-else class="spot-content">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <input v-model="keyword" placeholder="搜索钓点..." @keyup.enter="search" />
        <button @click="search">搜索</button>
      </div>

      <!-- 视图切换 -->
      <div class="view-tabs">
        <button :class="{ active: viewMode === 'map' }" @click="switchToMap">地图视图</button>
        <button :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">列表视图</button>
      </div>

      <!-- 地图视图 -->
      <div v-show="viewMode === 'map'" class="map-container">
        <div id="amap-container" class="amap"></div>
      </div>

      <!-- 列表视图 -->
      <div v-show="viewMode === 'list'" class="spot-list">
        <div v-for="spot in spotList" :key="spot.id" class="spot-card" @click="selectSpot(spot)">
          <div class="spot-info">
            <div v-if="getFirstImage(spot)" class="spot-thumb">
              <img :src="getFirstImage(spot)" :alt="spot.name" />
            </div>
            <h4>{{ spot.name }}</h4>
            <p class="spot-type">{{ spot.type === 1 ? '野钓' : '黑坑' }}</p>
            <p class="spot-fish">鱼种: {{ spot.fishSpecies || '未填写' }}</p>
            <p class="spot-desc">{{ spot.envDesc || '暂无描述' }}</p>
            <div class="spot-footer">
              <span>👁 {{ spot.viewCount || 0 }} 次浏览</span>
              <button class="btn-nav" @click.stop="openSpotNav(spot)">🧭 导航</button>
            </div>
          </div>
        </div>
        <div v-if="spotList.length === 0" class="empty">暂无审核通过的钓点</div>
      </div>

      <div v-if="totalCount > pageSize && viewMode === 'list'" class="pagination">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span>{{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import request from '../api/index.js'

const AMAP_KEY = 'YOUR_AMAP_WEB_KEY'

let map = null
let markers = []
let spotMapPicker = null
let spotPickerMarker = null

const viewMode = ref('map')
const showForm = ref(false)
const spotList = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const submitting = ref(false)
const submitMsg = ref('')
const uploadingImages = ref(false)
const spotImageFiles = ref([])
const spotImagePreviews = ref([])
const uploadedImageUrls = ref([])

const form = ref({
  name: '',
  type: 1,
  longitude: '',
  latitude: '',
  fishSpecies: '',
  waterQuality: '',
  depthDesc: '',
  feeDesc: '',
  envDesc: '',
  navigation: ''
})

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

const loadAMap = () => {
  return new Promise((resolve) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.onload = () => resolve(window.AMap)
    script.onerror = () => {
      console.warn('AMap JS API 加载失败，地图功能不可用')
      resolve(null)
    }
    document.head.appendChild(script)
  })
}

// 组件创建时立即开始预加载高德地图 SDK（不等待组件挂载）
const amapPromise = loadAMap()

const fetchSpots = async (pageOverride) => {
  const pg = pageOverride || currentPage.value
  try {
    const res = await request.get('/spot/list', {
      params: { currentPage: pg, pageSize: pageSize.value, keyword: keyword.value }
    })
    if (res.code === 200) {
      spotList.value = res.data.list || []
      totalCount.value = res.data.totalCount || 0
    }
  } catch (e) {
    console.error('获取钓点列表失败', e)
  }
}

const fetchAllSpots = async () => {
  try {
    const res = await request.get('/spot/list', {
      params: { currentPage: 1, pageSize: 9999, keyword: keyword.value }
    })
    if (res.code === 200) {
      return res.data.list || []
    }
  } catch (e) {
    console.error('获取全部钓点失败', e)
  }
  return []
}

const initMap = async () => {
  await nextTick()
  const AMapLib = await amapPromise
  if (!AMapLib) return

  // 先创建或复用地图（无数据时也能看到空地图）
  if (!map) {
    map = new AMapLib.Map('amap-container', {
      zoom: 11,
      center: [116.397428, 39.90923]
    })
  } else {
    // v-show 切换回来时刷新地图尺寸，避免显示异常
    map.resize()
  }

  // 加载全部钓点数据并打标记
  const allSpots = await fetchAllSpots()
  clearMarkers()
  markers = []

  allSpots.forEach(spot => {
    if (spot.longitude && spot.latitude) {
      const marker = new AMapLib.Marker({
        position: [spot.longitude, spot.latitude],
        title: spot.name,
        map: map
      })

      const typeName = spot.type === 1 ? '野钓' : '黑坑'
      const firstImg = spot.images ? spot.images.split(',')[0] : null
      const imgHtml = firstImg ? `<img src="${firstImg}" style="width:100%;max-height:120px;object-fit:cover;border-radius:4px;margin-bottom:6px;" />` : ''
      const infoWindow = new AMapLib.InfoWindow({
        content: `
          <div style="padding:8px;max-width:220px;">
            ${imgHtml}
            <h4 style="margin:0 0 6px;">${spot.name}</h4>
            <p style="margin:2px 0;font-size:12px;color:#666;">类型: ${typeName}</p>
            <p style="margin:2px 0;font-size:12px;color:#666;">鱼种: ${spot.fishSpecies || '未填写'}</p>
            <p style="margin:2px 0;font-size:12px;color:#666;">${spot.envDesc || ''}</p>
            <a href="https://uri.amap.com/navigation?to=${spot.longitude},${spot.latitude},${encodeURIComponent(spot.name)}&mode=car&callnative=1"
               target="_blank"
               style="display:inline-block;margin-top:6px;padding:4px 12px;background:#4caf50;color:#fff;text-decoration:none;border-radius:4px;font-size:12px;">
              🧭 导航到这里
            </a>
          </div>
        `,
        offset: new AMapLib.Pixel(0, -30)
      })

      marker.on('click', () => {
        infoWindow.open(map, marker.getPosition())
      })

      markers.push(marker)
    }
  })

  if (markers.length > 0) {
    map.setFitView(markers)
  }
}

const clearMarkers = () => {
  if (map && markers.length > 0) {
    map.remove(markers)
    markers = []
  }
}

const switchToMap = () => {
  viewMode.value = 'map'
  initMap()
}

const search = () => {
  currentPage.value = 1
  fetchSpots(1)
}

const changePage = (page) => {
  currentPage.value = page
  fetchSpots(page)
}

const selectSpot = async (spot) => {
  viewMode.value = 'map'
  await nextTick()
  // 地图未初始化时才初始化（正常情况下 onMounted 已创建）
  if (!map) await initMap()
  if (map && spot.longitude && spot.latitude) {
    map.setZoomAndCenter(16, [spot.longitude, spot.latitude])
  }
}

const onSpotImagesChange = (e) => {
  const files = Array.from(e.target.files || [])
  spotImageFiles.value = files
  spotImagePreviews.value = files.map(f => URL.createObjectURL(f))
  uploadedImageUrls.value = []
}

const uploadSpotImages = async () => {
  if (spotImageFiles.value.length === 0) return []
  uploadingImages.value = true
  try {
    const fd = new FormData()
    spotImageFiles.value.forEach(f => fd.append('files', f))
    const res = await request.post('/file/upload-images', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) return res.data || []
    return []
  } catch (e) {
    console.error('图片上传失败', e)
    return []
  } finally {
    uploadingImages.value = false
  }
}

const initSpotMapPicker = async () => {
  await nextTick()
  const AMapLib = await loadAMap()
  if (!AMapLib) return
  const container = document.getElementById('spot-map-picker')
  if (!container || spotMapPicker) return

  spotMapPicker = new AMapLib.Map('spot-map-picker', {
    zoom: 4,
    center: [116.397428, 39.90923]
  })
  spotMapPicker.on('click', (e) => {
    const { lng, lat } = e.lnglat
    form.value.longitude = lng.toFixed(6)
    form.value.latitude = lat.toFixed(6)
    if (spotPickerMarker) {
      spotPickerMarker.setPosition([lng, lat])
    } else {
      spotPickerMarker = new AMapLib.Marker({ position: [lng, lat], map: spotMapPicker })
    }
  })
}

watch(showForm, (val) => {
  if (val) {
    initSpotMapPicker()
  }
})

const getFirstImage = (spot) => {
  if (!spot.images) return null
  return spot.images.split(',')[0] || null
}

const openSpotNav = (spot) => {
  const lon = parseFloat(spot.longitude)
  const lat = parseFloat(spot.latitude)
  if (isNaN(lon) || isNaN(lat) || (lon === 0 && lat === 0)) {
    if (spot.navigation) {
      window.open(`https://uri.amap.com/navigation?to=${encodeURIComponent(spot.navigation)}&mode=car&callnative=1`, '_blank')
    } else {
      alert('该钓点暂无坐标信息')
    }
    return
  }
  window.open(`https://uri.amap.com/navigation?to=${lon},${lat},${encodeURIComponent(spot.name)}&mode=car&callnative=1`, '_blank')
}

const submitSpot = async () => {
  if (!form.value.name.trim()) {
    submitMsg.value = '请输入钓点名称'
    return
  }
  if (!form.value.longitude || !form.value.latitude) {
    submitMsg.value = '请输入经纬度'
    return
  }
  const token = localStorage.getItem('token')
  if (!token) {
    submitMsg.value = '请先登录再分享钓点'
    return
  }
  submitting.value = true
  submitMsg.value = ''
  try {
    // 先上传图片
    let imageUrls = []
    if (spotImageFiles.value.length > 0) {
      imageUrls = await uploadSpotImages()
    }

    const res = await request.post('/spot/create', {
      name: form.value.name,
      type: form.value.type,
      longitude: parseFloat(form.value.longitude),
      latitude: parseFloat(form.value.latitude),
      fishSpecies: form.value.fishSpecies,
      waterQuality: form.value.waterQuality,
      depthDesc: form.value.depthDesc,
      feeDesc: form.value.feeDesc,
      envDesc: form.value.envDesc,
      navigation: form.value.navigation,
      images: imageUrls.join(',')
    })
    if (res.code === 200) {
      submitMsg.value = '提交成功，等待审核通过后即可展示'
      form.value = {
        name: '', type: 1, longitude: '', latitude: '',
        fishSpecies: '', waterQuality: '', depthDesc: '',
        feeDesc: '', envDesc: '', navigation: ''
      }
      spotImageFiles.value = []
      spotImagePreviews.value = []
      uploadedImageUrls.value = []
    } else {
      submitMsg.value = res.msg || '提交失败'
    }
  } catch (e) {
    submitMsg.value = '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchSpots(1)   // 列表数据
  initMap()        // 地图初始化（并行执行）
})
</script>

<style scoped>
.spot-share { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; flex: 1; font-size: 20px; }
.btn-toggle {
  padding: 8px 20px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 14px;
}

/* 表单样式 */
.form-section {
  max-width: 640px; margin: 24px auto; padding: 20px;
  background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.form-section h3 { margin: 0 0 16px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 4px; font-size: 14px; color: #333; font-weight: 500; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 8px 12px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box;
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color: #1a73e8; }
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }
.btn-submit {
  width: 100%; padding: 10px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 16px; margin-top: 8px;
}
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }
.submit-msg { text-align: center; margin-top: 12px; color: #4caf50; font-size: 14px; }

/* 钓点内容 */
.spot-content { padding: 16px 24px; }
.search-bar { display: flex; max-width: 500px; margin: 0 auto 16px; }
.search-bar input {
  flex: 1; padding: 8px 12px; border: 1px solid #ddd;
  border-radius: 4px 0 0 4px; outline: none;
}
.search-bar button {
  padding: 8px 16px; background: #1a73e8; color: #fff;
  border: none; border-radius: 0 4px 4px 0; cursor: pointer;
}

.view-tabs { display: flex; gap: 8px; margin-bottom: 16px; justify-content: center; }
.view-tabs button {
  padding: 8px 24px; border: 1px solid #ddd; background: #fff;
  border-radius: 20px; cursor: pointer; font-size: 14px;
}
.view-tabs button.active { background: #1a73e8; color: #fff; border-color: #1a73e8; }

.map-container { max-width: 1000px; margin: 0 auto; }
.amap { width: 100%; height: 500px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }

.spot-list { max-width: 800px; margin: 0 auto; }
.spot-card {
  background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); cursor: pointer; transition: transform 0.2s;
}
.spot-card:hover { transform: translateY(-2px); }
.spot-thumb { margin: -16px -16px 12px; height: 140px; overflow: hidden; border-radius: 12px 12px 0 0; }
.spot-thumb img { width: 100%; height: 100%; object-fit: cover; }
.spot-info h4 { margin: 0 0 6px; font-size: 16px; }
.spot-type {
  display: inline-block; background: #e8f0fe; color: #1a73e8;
  padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-bottom: 6px;
}
.spot-fish { color: #666; font-size: 13px; margin: 4px 0; }
.spot-desc { color: #888; font-size: 13px; margin: 4px 0; }
.spot-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.btn-nav {
  padding: 6px 16px; background: #4caf50; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.btn-nav:hover { background: #388e3c; }

.pagination {
  display: flex; justify-content: center; align-items: center;
  gap: 16px; margin-top: 24px; padding: 16px;
}
.pagination button {
  padding: 8px 16px; border: 1px solid #ddd; background: #fff;
  border-radius: 4px; cursor: pointer;
}
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
.empty { text-align: center; padding: 48px; color: #999; }

.map-picker { width: 100%; height: 220px; border-radius: 8px; border: 1px solid #ddd; cursor: crosshair; }
.map-hint { font-size: 12px; color: #999; margin-top: 4px; }

.image-previews { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.img-preview-item {
  position: relative; width: 80px; height: 80px; border-radius: 6px;
  overflow: hidden; border: 2px solid #eee;
}
.img-preview-item img { width: 100%; height: 100%; object-fit: cover; }
.cover-badge {
  position: absolute; bottom: 0; left: 0; right: 0; text-align: center;
  background: rgba(26,115,232,0.8); color: #fff; font-size: 11px; padding: 2px 0;
}
</style>
