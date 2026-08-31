<template>
  <div class="owner-page">
    <header class="header">
      <router-link to="/" class="back">← 首页</router-link>
      <h2>塘主管理后台</h2>
    </header>

    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key">
        {{ tab.label }}
      </button>
    </div>

    <!-- 我的钓场 -->
    <div v-if="activeTab === 'venues'" class="tab-content">
      <div class="section-header">
        <h3>我的钓场</h3>
        <button class="btn-primary" @click="startAdd">+ 添加钓场</button>
      </div>

      <div v-if="myVenues.length === 0" class="empty">暂无钓场，请先添加</div>

      <div v-for="venue in myVenues" :key="venue.id" class="venue-item">
        <div class="venue-info">
          <h4>{{ venue.name }}</h4>
          <p>{{ venue.address }}</p>
          <p class="venue-meta">钓位: {{ venue.totalSeats }} | 关注: {{ venue.followerCount }} | 状态: {{ venue.status === 1 ? '营业中' : '歇业' }}</p>
        </div>
        <div class="venue-actions">
          <button class="btn-edit" @click="startEdit(venue)">编辑</button>
          <button class="btn-delete" @click="confirmDelete(venue)">删除</button>
        </div>
      </div>

      <div v-if="showVenueForm" class="modal-overlay" @click.self="cancelForm">
        <div class="form-card">
          <h3>{{ editingVenueId ? '编辑钓场' : '添加钓场' }}</h3>
          <div class="form-group">
            <label>钓场名称</label>
            <input v-model="venueForm.name" placeholder="请输入钓场名称" />
          </div>
          <div class="form-group">
            <label>详细地址</label>
            <input v-model="venueForm.address" placeholder="请输入详细地址" />
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="venueForm.phone" placeholder="请输入联系电话" />
          </div>
          <div class="form-group">
            <label>地图选点（点击地图设置经纬度）</label>
            <div id="venue-map-picker" class="map-picker" @click.self="noop"></div>
            <p class="map-hint">点击地图上的位置自动填入经纬度</p>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>经度</label>
              <input v-model="venueForm.longitude" type="number" step="0.000001" placeholder="经度" />
            </div>
            <div class="form-group">
              <label>纬度</label>
              <input v-model="venueForm.latitude" type="number" step="0.000001" placeholder="纬度" />
            </div>
          </div>
          <div class="form-group">
            <label>封面图片</label>
            <input type="file" accept="image/*" @change="onVenueCoverChange" />
            <div v-if="venueCoverPreview" class="image-previews">
              <div class="img-preview-item">
                <img :src="venueCoverPreview" />
                <span class="cover-badge">封面</span>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>更多图片（用于详细介绍）</label>
            <input type="file" multiple accept="image/*" @change="onVenueImagesChange" />
            <div v-if="venueImagePreviews.length > 0" class="image-previews">
              <div v-for="(url, idx) in venueImagePreviews" :key="idx" class="img-preview-item">
                <img :src="url" />
              </div>
            </div>
            <p v-if="uploadingVenueImages" class="map-hint">图片上传中...</p>
          </div>
          <div class="form-group">
            <label>总钓位数</label>
            <input v-model="venueForm.totalSeats" type="number" placeholder="总钓位数" />
          </div>
          <div class="form-group">
            <label>钓场规则</label>
            <textarea v-model="venueForm.ruleDesc" placeholder="钓场规则描述"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn-cancel" @click="cancelForm">取消</button>
            <button class="btn-primary" @click="saveVenue" :disabled="!venueForm.name || uploadingVenueImages">
              {{ editingVenueId ? '保存修改' : '提交' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布鱼讯 -->
    <div v-if="activeTab === 'fishInfo'" class="tab-content">
      <h3>发布放鱼视频</h3>
      <div class="form-card-inline">
        <div class="form-group">
          <label>选择钓场</label>
          <select v-model="fishInfo.venueId">
            <option value="">请选择钓场</option>
            <option v-for="v in myVenues" :key="v.id" :value="v.id">{{ v.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>视频文件</label>
          <input type="file" @change="onVideoFileChange" accept="video/*" />
        </div>
        <div class="form-group">
          <label>放鱼数量（尾）</label>
          <input v-model="fishInfo.fishCount" type="number" placeholder="放鱼数量" />
        </div>
        <div class="form-group">
          <label>鱼种</label>
          <input v-model="fishInfo.fishSpecies" placeholder="如：鲤鱼、草鱼" />
        </div>
        <div class="form-group">
          <label>个体大小</label>
          <input v-model="fishInfo.fishSizeDesc" placeholder="如：3-5斤" />
        </div>
        <div class="form-group">
          <label>正钓时间</label>
          <input v-model="fishInfo.fishingTime" type="datetime-local" />
        </div>
        <div class="form-group">
          <label>票价（元）</label>
          <input v-model="fishInfo.ticketPrice" type="number" placeholder="票价" />
        </div>
        <button class="btn-primary" @click="publishFishInfo" :disabled="!fishInfo.venueId || !videoFile">
          发布鱼讯
        </button>
        <p v-if="fishMsg" class="feedback">{{ fishMsg }}</p>
      </div>

      <h3 style="margin-top: 32px;">发布上鱼视频</h3>
      <div class="form-card-inline">
        <div class="form-group">
          <label>选择钓场</label>
          <select v-model="catchVideo.venueId">
            <option value="">请选择钓场</option>
            <option v-for="v in myVenues" :key="v.id" :value="v.id">{{ v.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>视频文件</label>
          <input type="file" @change="onCatchFileChange" accept="video/*" />
        </div>
        <button class="btn-primary" @click="publishCatchVideo" :disabled="!catchVideo.venueId || !catchFile">
          发布上鱼视频
        </button>
        <p v-if="catchMsg" class="feedback">{{ catchMsg }}</p>
      </div>
    </div>

    <!-- 场次管理 -->
    <div v-if="activeTab === 'sessions'" class="tab-content">
      <div class="section-header">
        <h3>场次管理</h3>
        <div>
          <select v-model="sessionVenueId" @change="fetchSessions">
            <option value="">选择钓场</option>
            <option v-for="v in myVenues" :key="v.id" :value="v.id">{{ v.name }}</option>
          </select>
          <button class="btn-primary" style="margin-left:12px" @click="showSessionForm = true" :disabled="!sessionVenueId">
            + 新增场次
          </button>
        </div>
      </div>

      <div v-if="sessionList.length === 0" class="empty">请先选择钓场</div>

      <div v-for="s in sessionList" :key="s.id" class="session-item">
        <div class="session-info">
          <h4>{{ s.name }}</h4>
          <p>时间: {{ s.startTime?.substring(0,16) }} ~ {{ s.endTime?.substring(0,16) }}</p>
          <p>票价: ¥{{ s.ticketPrice }} | 钓位: {{ s.bookedSeats || 0 }}/{{ s.totalSeats }} | 状态: {{ ['未开始','可报名','报名中','进行中','已结束'][s.status] || s.status }}</p>
        </div>
      </div>

      <div v-if="showSessionForm" class="modal-overlay" @click.self="showSessionForm = false">
        <div class="form-card">
          <h3>新增场次</h3>
          <div class="form-group">
            <label>场次名称</label>
            <input v-model="sessionForm.name" placeholder="如：周六正钓" />
          </div>
          <div class="form-group">
            <label>票价（元）</label>
            <input v-model="sessionForm.ticketPrice" type="number" placeholder="票价" />
          </div>
          <div class="form-group">
            <label>总钓位数</label>
            <input v-model="sessionForm.totalSeats" type="number" placeholder="总钓位数" />
          </div>
          <div class="form-group">
            <label>开始时间</label>
            <input v-model="sessionForm.startTime" type="datetime-local" />
          </div>
          <div class="form-group">
            <label>结束时间</label>
            <input v-model="sessionForm.endTime" type="datetime-local" />
          </div>
          <div class="form-group">
            <label>抽位截止时间</label>
            <input v-model="sessionForm.drawTime" type="datetime-local" />
          </div>
          <div class="form-actions">
            <button class="btn-cancel" @click="showSessionForm = false">取消</button>
            <button class="btn-primary" @click="createSession">提交</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 营收数据 -->
    <div v-if="activeTab === 'revenue'" class="tab-content">
      <h3>营收统计</h3>
      <div class="form-card-inline" style="display:flex;gap:12px;align-items:end;flex-wrap:wrap;">
        <div class="form-group">
          <label>选择钓场</label>
          <select v-model="revenueVenueId" @change="fetchRevenue">
            <option value="">请选择</option>
            <option v-for="v in myVenues" :key="v.id" :value="v.id">{{ v.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>开始日期</label>
          <input v-model="revenueStart" type="date" />
        </div>
        <div class="form-group">
          <label>结束日期</label>
          <input v-model="revenueEnd" type="date" />
        </div>
        <button class="btn-primary" @click="fetchRevenue" :disabled="!revenueVenueId">查询</button>
      </div>

      <div v-if="revenueData.length > 0" style="margin-top:24px">
        <h4>营收趋势</h4>
        <table class="data-table">
          <thead>
            <tr><th>日期</th><th>营收(元)</th><th>订单数</th></tr>
          </thead>
          <tbody>
            <tr v-for="d in revenueData" :key="d.date">
              <td>{{ d.date }}</td>
              <td>¥{{ d.amount }}</td>
              <td>{{ d.orderCount }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="sessionStats.length > 0" style="margin-top:24px">
        <h4>场次上座率</h4>
        <table class="data-table">
          <thead>
            <tr><th>场次</th><th>营收</th><th>已订/总钓位</th><th>上座率</th></tr>
          </thead>
          <tbody>
            <tr v-for="s in sessionStats" :key="s.sessionName">
              <td>{{ s.sessionName }}</td>
              <td>¥{{ s.revenue }}</td>
              <td>{{ s.bookedSeats }}/{{ s.totalSeats }}</td>
              <td>{{ s.occupancyRate?.toFixed(1) }}%</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import request from '../api/index.js'

const AMAP_KEY = 'YOUR_AMAP_WEB_KEY'

let venueMapPicker = null
let venuePickerMarker = null

const activeTab = ref('venues')
const tabs = [
  { key: 'venues', label: '我的钓场' },
  { key: 'fishInfo', label: '发布鱼讯' },
  { key: 'sessions', label: '场次管理' },
  { key: 'revenue', label: '营收数据' }
]

const myVenues = ref([])

const showVenueForm = ref(false)
const editingVenueId = ref(null)
const venueForm = ref({ name: '', address: '', phone: '', longitude: '', latitude: '', totalSeats: 40, ruleDesc: '' })
const venueCoverFile = ref(null)
const venueCoverPreview = ref('')
const venueImageFiles = ref([])
const venueImagePreviews = ref([])
const uploadingVenueImages = ref(false)

const fishInfo = ref({ venueId: '', fishCount: 0, fishSpecies: '', fishSizeDesc: '', fishingTime: '', ticketPrice: '' })
const videoFile = ref(null)
const fishMsg = ref('')

const catchVideo = ref({ venueId: '' })
const catchFile = ref(null)
const catchMsg = ref('')

const sessionVenueId = ref('')
const sessionList = ref([])
const showSessionForm = ref(false)
const sessionForm = ref({ name: '', ticketPrice: '', totalSeats: '', startTime: '', endTime: '', drawTime: '' })

const revenueVenueId = ref('')
const revenueStart = ref('2024-01-01')
const revenueEnd = ref('2024-12-31')
const revenueData = ref([])
const sessionStats = ref([])

const fetchMyVenues = async () => {
  try {
    const res = await request.get('/venue/my')
    if (res.code === 200) myVenues.value = res.data || []
  } catch (e) { console.error('获取我的钓场失败', e) }
}

const noop = () => {}

const loadAMapSDK = () => {
  return new Promise((resolve) => {
    if (window.AMap) { resolve(window.AMap); return }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.onload = () => resolve(window.AMap)
    script.onerror = () => { console.warn('AMap 加载失败'); resolve(null) }
    document.head.appendChild(script)
  })
}

const initVenueMapPicker = async () => {
  await nextTick()
  const AMapLib = await loadAMapSDK()
  if (!AMapLib) return
  const container = document.getElementById('venue-map-picker')
  if (!container) return

  if (!venueMapPicker) {
    venueMapPicker = new AMapLib.Map('venue-map-picker', {
      zoom: 4,
      center: [116.397428, 39.90923]
    })
    venueMapPicker.on('click', (e) => {
      const { lng, lat } = e.lnglat
      venueForm.value.longitude = lng.toFixed(6)
      venueForm.value.latitude = lat.toFixed(6)
      if (venuePickerMarker) {
        venuePickerMarker.setPosition([lng, lat])
      } else {
        venuePickerMarker = new AMapLib.Marker({ position: [lng, lat], map: venueMapPicker })
      }
    })
  }
}

watch(showVenueForm, (val) => {
  if (val) {
    initVenueMapPicker()
    if (venueForm.value.longitude && venueForm.value.latitude && venueMapPicker) {
      const pos = [parseFloat(venueForm.value.longitude), parseFloat(venueForm.value.latitude)]
      venueMapPicker.setZoomAndCenter(16, pos)
      if (venuePickerMarker) {
        venuePickerMarker.setPosition(pos)
      }
    }
  }
})

const onVenueCoverChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    venueCoverFile.value = file
    venueCoverPreview.value = URL.createObjectURL(file)
  }
}

const onVenueImagesChange = (e) => {
  const files = Array.from(e.target.files || [])
  venueImageFiles.value = files
  venueImagePreviews.value = files.map(f => URL.createObjectURL(f))
}

const uploadVenueImages = async () => {
  const allFiles = []
  if (venueCoverFile.value) allFiles.push(venueCoverFile.value)
  venueImageFiles.value.forEach(f => allFiles.push(f))
  if (allFiles.length === 0) return { cover: '', images: '' }

  uploadingVenueImages.value = true
  try {
    const fd = new FormData()
    allFiles.forEach(f => fd.append('files', f))
    const res = await request.post('/file/upload-images', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      const urls = res.data || []
      const cover = venueCoverFile.value ? (urls[0] || '') : ''
      const startIdx = venueCoverFile.value ? 1 : 0
      const images = urls.slice(startIdx).join(',')
      return { cover, images }
    }
    return { cover: '', images: '' }
  } catch (e) {
    console.error('图片上传失败', e)
    return { cover: '', images: '' }
  } finally {
    uploadingVenueImages.value = false
  }
}

const resetVenueForm = () => {
  venueForm.value = { name: '', address: '', phone: '', longitude: '', latitude: '', totalSeats: 40, ruleDesc: '' }
  venueCoverFile.value = null
  venueCoverPreview.value = ''
  venueImageFiles.value = []
  venueImagePreviews.value = []
  editingVenueId.value = null
}

const cancelForm = () => {
  showVenueForm.value = false
  resetVenueForm()
}

const startAdd = () => {
  resetVenueForm()
  showVenueForm.value = true
}

const startEdit = (venue) => {
  editingVenueId.value = venue.id
  venueForm.value = {
    name: venue.name || '',
    address: venue.address || '',
    phone: venue.phone || '',
    longitude: venue.longitude || '',
    latitude: venue.latitude || '',
    totalSeats: venue.totalSeats || 40,
    ruleDesc: venue.ruleDesc || ''
  }
  venueCoverPreview.value = venue.coverImage || ''
  venueCoverFile.value = null
  venueImageFiles.value = []
  venueImagePreviews.value = []
  showVenueForm.value = true
}

const saveVenue = async () => {
  try {
    const { cover, images } = await uploadVenueImages()

    const payload = {
      ...venueForm.value,
      coverImage: cover || venueCoverPreview.value || undefined,
      images: images || undefined
    }

    if (editingVenueId.value) {
      payload.id = editingVenueId.value
      const res = await request.put('/venue/update', payload)
      if (res.code === 200) {
        cancelForm()
        fetchMyVenues()
      } else alert(res.msg || '修改失败')
    } else {
      const res = await request.post('/venue/create', payload)
      if (res.code === 200) {
        cancelForm()
        fetchMyVenues()
      } else alert(res.msg || '添加失败')
    }
  } catch (e) { alert('操作失败') }
}

const confirmDelete = async (venue) => {
  if (!confirm(`确定要删除钓场"${venue.name}"吗？此操作不可恢复。`)) return
  try {
    const res = await request.delete(`/venue/delete/${venue.id}`)
    if (res.code === 200) {
      fetchMyVenues()
    } else alert(res.msg || '删除失败')
  } catch (e) { alert('删除失败') }
}

const onVideoFileChange = (e) => { videoFile.value = e.target.files[0] }
const onCatchFileChange = (e) => { catchFile.value = e.target.files[0] }

const publishFishInfo = async () => {
  fishMsg.value = ''
  const fd = new FormData()
  fd.append('video', videoFile.value)
  fd.append('venueId', fishInfo.value.venueId)
  fd.append('type', 1)
  fd.append('fishCount', fishInfo.value.fishCount)
  fd.append('fishSpecies', fishInfo.value.fishSpecies)
  fd.append('fishingTime', fishInfo.value.fishingTime)
  fd.append('ticketPrice', fishInfo.value.ticketPrice)
  try {
    const res = await request.post('/video/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 200) {
      fishMsg.value = '鱼讯发布成功！'
      fishInfo.value = { venueId: '', fishCount: 0, fishSpecies: '', fishSizeDesc: '', fishingTime: '', ticketPrice: '' }
      videoFile.value = null
    } else { fishMsg.value = res.msg || '发布失败' }
  } catch (e) { fishMsg.value = '发布失败' }
}

const publishCatchVideo = async () => {
  catchMsg.value = ''
  const fd = new FormData()
  fd.append('video', catchFile.value)
  fd.append('venueId', catchVideo.value.venueId)
  fd.append('type', 2)
  try {
    const res = await request.post('/video/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 200) {
      catchMsg.value = '上鱼视频发布成功！'
      catchVideo.value = { venueId: '' }
      catchFile.value = null
    } else { catchMsg.value = res.msg || '发布失败' }
  } catch (e) { catchMsg.value = '发布失败' }
}

const fetchSessions = async () => {
  if (!sessionVenueId.value) return
  try {
    const res = await request.get(`/session/list/${sessionVenueId.value}`)
    if (res.code === 200) sessionList.value = res.data || []
  } catch (e) { console.error(e) }
}

const createSession = async () => {
  try {
    const res = await request.post('/session/create', {
      ...sessionForm.value,
      venueId: sessionVenueId.value
    })
    if (res.code === 200) {
      showSessionForm.value = false
      sessionForm.value = { name: '', ticketPrice: '', totalSeats: '', startTime: '', endTime: '', drawTime: '' }
      fetchSessions()
    } else alert(res.msg || '创建失败')
  } catch (e) { alert('创建失败') }
}

const fetchRevenue = async () => {
  try {
    const res = await request.get('/stat/revenue', {
      params: { venueId: revenueVenueId.value, startDate: revenueStart.value, endDate: revenueEnd.value }
    })
    if (res.code === 200) {
      revenueData.value = res.data.revenueData || []
      sessionStats.value = res.data.sessionStats || []
    }
  } catch (e) { console.error(e) }
}

onMounted(fetchMyVenues)
</script>

<style scoped>
.owner-page { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }

.tabs {
  display: flex; gap: 0; background: #fff; border-bottom: 2px solid #eee;
}
.tabs button {
  padding: 14px 28px; border: none; background: transparent;
  cursor: pointer; font-size: 14px; color: #666;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.tabs button:hover { color: #1a73e8; }
.tabs button.active { color: #1a73e8; border-bottom-color: #1a73e8; font-weight: bold; }

.tab-content { max-width: 1000px; margin: 24px auto; padding: 0 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.empty { text-align: center; padding: 60px; color: #999; }

.venue-item, .session-item {
  background: #fff; padding: 16px; border-radius: 8px;
  margin-bottom: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  display: flex; justify-content: space-between; align-items: center;
}
.venue-actions { display: flex; gap: 8px; flex-shrink: 0; }
.btn-edit {
  padding: 6px 14px; border: 1px solid #1a73e8; background: #fff;
  color: #1a73e8; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.btn-delete {
  padding: 6px 14px; border: 1px solid #ff4444; background: #fff;
  color: #ff4444; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.btn-edit:hover { background: #e8f0fe; }
.btn-delete:hover { background: #fff0f0; }
.venue-info h4, .session-info h4 { margin: 0 0 4px; }
.venue-info p, .session-info p { margin: 0 0 4px; color: #666; font-size: 13px; }
.venue-meta { font-size: 12px; color: #999; }

.btn-primary {
  padding: 10px 24px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 14px;
}
.btn-primary:disabled { background: #ccc; cursor: not-allowed; }
.btn-cancel {
  padding: 10px 24px; background: #eee; color: #333;
  border: none; border-radius: 6px; cursor: pointer;
}

.form-card, .form-card-inline {
  background: #fff; padding: 24px; border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 16px;
}
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; color: #555; font-size: 14px; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 8px 12px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box;
}
.form-group textarea { min-height: 80px; resize: vertical; }
.form-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 16px; }
.feedback { color: #4caf50; margin-top: 12px; }

.data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; }
.data-table th, .data-table td { padding: 10px 16px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f5f5f5; font-weight: bold; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center; z-index: 200;
}
.modal-overlay .form-card { max-width: 600px; width: 90%; max-height: 85vh; overflow-y: auto; }
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }
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
