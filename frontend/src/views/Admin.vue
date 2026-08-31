<template>
  <div class="admin-page">
    <header class="header">
      <h2>云渔管理后台</h2>
      <span v-if="adminInfo" class="admin-name">{{ adminInfo.name }}（{{ adminInfo.role === 1 ? '超级管理员' : '管理员' }}）</span>
      <button class="btn-logout" @click="logout">退出</button>
    </header>

    <div v-if="!isLoggedIn" class="login-section">
      <div class="login-card">
        <h3>请先登录</h3>
        <p class="empty">正在跳转到登录页...</p>
        <button class="btn-login" @click="goLogin">去登录</button>
      </div>
    </div>

    <div v-else class="admin-content">
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }"
                @click="activeTab = tab.key; onTabChange()">
          {{ tab.label }}
        </button>
      </div>

      <!-- 数据概览 -->
      <div v-if="activeTab === 'dashboard'" class="tab-content">
        <div class="stat-grid">
          <div class="stat-card"><span class="stat-num">{{ dash.userCount }}</span><span class="stat-label">用户总数</span></div>
          <div class="stat-card"><span class="stat-num">{{ dash.venueCount }}</span><span class="stat-label">营业钓场</span></div>
          <div class="stat-card"><span class="stat-num">{{ dash.orderCount }}</span><span class="stat-label">订单总数</span></div>
          <div class="stat-card"><span class="stat-num">{{ dash.postCount }}</span><span class="stat-label">帖子总数</span></div>
          <div class="stat-card"><span class="stat-num">{{ dash.productCount }}</span><span class="stat-label">上架商品</span></div>
          <div class="stat-card"><span class="stat-num">{{ dash.spotCount }}</span><span class="stat-label">已审钓点</span></div>
          <div class="stat-card"><span class="stat-num" style="color:#ff9800">{{ dash.pendingSpotCount }}</span><span class="stat-label">待审钓点</span></div>
          <div class="stat-card"><span class="stat-num" style="color:#ff9800">{{ dash.pendingPostCount }}</span><span class="stat-label">待审核帖子</span></div>
        </div>

        <!-- 四个ECharts图表 -->
        <div class="chart-grid">
          <div class="chart-box">
            <h4 class="chart-title">年龄分布</h4>
            <div ref="chartAge" class="chart-item"></div>
          </div>
          <div class="chart-box">
            <h4 class="chart-title">主要鱼种分布</h4>
            <div ref="chartFish" class="chart-item"></div>
          </div>
          <div class="chart-box">
            <h4 class="chart-title">地域分布</h4>
            <div ref="chartRegion" class="chart-item"></div>
          </div>
          <div class="chart-box">
            <h4 class="chart-title">钓法分布</h4>
            <div ref="chartMethod" class="chart-item"></div>
          </div>
        </div>
      </div>

      <!-- 用户管理 -->
      <div v-if="activeTab === 'users'" class="tab-content">
        <div class="search-row">
          <input v-model="userKeyword" placeholder="搜索用户(账号/昵称)" @keyup.enter="fetchUsers" />
          <button class="btn-sm" @click="fetchUsers">搜索</button>
        </div>
        <table class="data-table">
          <thead><tr><th>ID</th><th>账号</th><th>昵称</th><th>手机号</th><th>角色</th><th>积分</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="u in userList" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.account }}</td>
              <td>{{ u.nickname }}</td>
              <td>{{ u.phone }}</td>
              <td>{{ u.role === 1 ? '塘主' : '用户' }}</td>
              <td>{{ u.points }}</td>
              <td><span :class="u.lockState === '1' ? 'tag-ok' : 'tag-bad'">{{ u.lockState === '1' ? '正常' : '锁定' }}</span></td>
              <td>
                <button v-if="u.lockState === '1'" class="btn-sm btn-danger" @click="lockUser(u.id, '0')">锁定</button>
                <button v-else class="btn-sm btn-ok" @click="lockUser(u.id, '1')">解锁</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pager">
          <button :disabled="userPage <= 1" @click="userPage--; fetchUsers()">上一页</button>
          <span>{{ userPage }} / {{ Math.ceil(userTotal / 10) || 1 }}</span>
          <button :disabled="userPage >= Math.ceil(userTotal / 10)" @click="userPage++; fetchUsers()">下一页</button>
        </div>
      </div>

      <!-- 内容审核 -->
      <div v-if="activeTab === 'audit'" class="tab-content">
        <div class="sub-tabs">
          <button :class="{ active: auditType === 'spot' }" @click="auditType = 'spot'; fetchPending()">待审钓点</button>
          <button :class="{ active: auditType === 'post' }" @click="auditType = 'post'; fetchPending()">待审帖子</button>
        </div>
        <div v-for="item in pendingList" :key="item.id" class="pending-card">
          <div class="pending-info">
            <h4>{{ item.name || item.title || '无标题' }}</h4>
            <p>{{ (item.content || item.envDesc || '').substring(0, 200) }}</p>
          </div>
          <div class="pending-actions">
            <button class="btn-sm btn-ok" @click="audit(item.id, 'approve')">通过</button>
            <button class="btn-sm btn-danger" @click="showReject(item.id)">驳回</button>
          </div>
        </div>
        <div v-if="pendingList.length === 0" class="empty">暂无待审核内容</div>

        <div v-if="showRejectModal" class="modal-overlay" @click.self="showRejectModal = false">
          <div class="modal-card">
            <h3>驳回原因</h3>
            <textarea v-model="rejectReason" placeholder="请输入驳回原因..." rows="4"></textarea>
            <div class="modal-btns">
              <button class="btn-cancel" @click="showRejectModal = false">取消</button>
              <button class="btn-sm btn-danger" @click="confirmReject">确认驳回</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 钓点管理 -->
      <div v-if="activeTab === 'spots'" class="tab-content">
        <div class="sub-tabs">
          <button :class="{ active: spotFilter === undefined }" @click="spotFilter = undefined; fetchSpots()">全部</button>
          <button :class="{ active: spotFilter === 0 }" @click="spotFilter = 0; fetchSpots()">待审</button>
          <button :class="{ active: spotFilter === 1 }" @click="spotFilter = 1; fetchSpots()">已通过</button>
          <button :class="{ active: spotFilter === 2 }" @click="spotFilter = 2; fetchSpots()">已驳回</button>
        </div>
        <table class="data-table">
          <thead><tr><th>ID</th><th>名称</th><th>提交者ID</th><th>鱼类</th><th>查看</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="s in spotList" :key="s.id">
              <td>{{ s.id }}</td>
              <td>{{ s.name }}</td>
              <td>{{ s.submitUserId }}</td>
              <td>{{ s.fishSpecies }}</td>
              <td>{{ s.viewCount }}</td>
              <td><span :class="statusClass(s.status)">{{ statusLabel(s.status) }}</span></td>
              <td>
                <button v-if="s.status !== 1" class="btn-sm btn-ok" @click="setSpotStatus(s.id, 1)">通过</button>
                <button v-if="s.status === 1" class="btn-sm btn-warn" @click="setSpotStatus(s.id, 2)">驳回</button>
                <button class="btn-sm btn-danger" @click="deleteSpot(s.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 帖子管理 -->
      <div v-if="activeTab === 'posts'" class="tab-content">
        <div class="sub-tabs">
          <button :class="{ active: postFilter === undefined }" @click="postFilter = undefined; fetchPosts()">全部</button>
          <button :class="{ active: postFilter === 0 }" @click="postFilter = 0; fetchPosts()">待审</button>
          <button :class="{ active: postFilter === 1 }" @click="postFilter = 1; fetchPosts()">已通过</button>
          <button :class="{ active: postFilter === 2 }" @click="postFilter = 2; fetchPosts()">已驳回</button>
        </div>
        <table class="data-table">
          <thead><tr><th>ID</th><th>标题</th><th>作者ID</th><th>点赞</th><th>评论</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="p in postList" :key="p.id">
              <td>{{ p.id }}</td>
              <td>{{ (p.title || '').substring(0, 20) }}</td>
              <td>{{ p.userId }}</td>
              <td>{{ p.likeCount }}</td>
              <td>{{ p.commentCount }}</td>
              <td><span :class="statusClass(p.status)">{{ statusLabel(p.status) }}</span></td>
              <td>
                <button v-if="p.status !== 1" class="btn-sm btn-ok" @click="setPostStatus(p.id, 1)">通过</button>
                <button v-if="p.status === 1" class="btn-sm btn-warn" @click="setPostStatus(p.id, 2)">驳回</button>
                <button class="btn-sm btn-danger" @click="deletePost(p.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 钓场管理 -->
      <div v-if="activeTab === 'venues'" class="tab-content">
        <table class="data-table">
          <thead><tr><th>ID</th><th>名称</th><th>塘主ID</th><th>地址</th><th>钓位</th><th>关注</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="v in venueList" :key="v.id">
              <td>{{ v.id }}</td>
              <td>{{ v.name }}</td>
              <td>{{ v.ownerId }}</td>
              <td>{{ (v.address || '').substring(0, 15) }}</td>
              <td>{{ v.totalSeats }}</td>
              <td>{{ v.followerCount }}</td>
              <td><span :class="statusClass(v.status)">{{ ['待审','营业','歇业'][v.status] || v.status }}</span></td>
              <td>
                <button v-if="v.status !== 1" class="btn-sm btn-ok" @click="setVenueStatus(v.id, 1)">启用</button>
                <button v-if="v.status === 1" class="btn-sm btn-danger" @click="setVenueStatus(v.id, 2)">歇业</button>
                <button class="btn-sm btn-danger" @click="deleteVenue(v.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 商品管理 -->
      <div v-if="activeTab === 'products'" class="tab-content">
        <div class="section-header">
          <h3>商品列表</h3>
          <button class="btn-sm" @click="showProductForm = true; editingProduct = null; resetProductForm()">+ 添加商品</button>
        </div>
        <table class="data-table">
          <thead><tr><th>图</th><th>ID</th><th>名称</th><th>品类</th><th>品牌</th><th>售价</th><th>库存</th><th>销量</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="pr in productList" :key="pr.id">
              <td><img v-if="pr.images" :src="pr.images" class="product-thumb" /></td>
              <td>{{ pr.id }}</td>
              <td>{{ pr.name }}</td>
              <td>{{ ['','竿','轮','线','漂','饵','配件'][pr.category] || pr.category }}</td>
              <td>{{ pr.brand }}</td>
              <td>¥{{ pr.price }}</td>
              <td>{{ pr.stock }}</td>
              <td>{{ pr.salesCount }}</td>
              <td><span :class="pr.status === 1 ? 'tag-ok' : 'tag-bad'">{{ pr.status === 1 ? '上架' : '下架' }}</span></td>
              <td>
                <button class="btn-sm" @click="openEditProduct(pr)">编辑</button>
                <button v-if="pr.status === 1" class="btn-sm btn-danger" @click="toggleProductStatus(pr.id, 0)">下架</button>
                <button v-else class="btn-sm btn-ok" @click="toggleProductStatus(pr.id, 1)">上架</button>
                <button class="btn-sm btn-danger" @click="deleteProduct(pr.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="showProductForm" class="modal-overlay" @click.self="showProductForm = false">
          <div class="modal-card modal-wide">
            <h3>{{ editingProduct ? '编辑商品' : '添加商品' }}</h3>
            <div class="form-group"><label>图片</label>
              <input type="file" accept="image/*" @change="onProductImageChange" />
              <img v-if="productImagePreview" :src="productImagePreview" class="img-preview" />
            </div>
            <div class="form-group"><label>名称</label><input v-model="productForm.name" /></div>
            <div class="form-group">
              <label>品类</label>
              <select v-model="productForm.category">
                <option :value="1">鱼竿</option><option :value="2">渔轮</option><option :value="3">鱼线</option>
                <option :value="4">浮漂</option><option :value="5">鱼饵</option><option :value="6">配件</option>
              </select>
            </div>
            <div class="form-group"><label>品牌</label><input v-model="productForm.brand" /></div>
            <div class="form-group"><label>售价</label><input v-model="productForm.price" type="number" step="0.01" /></div>
            <div class="form-group"><label>库存</label><input v-model="productForm.stock" type="number" /></div>
            <div class="form-group"><label>描述</label><textarea v-model="productForm.description"></textarea></div>
            <div class="modal-btns">
              <button class="btn-cancel" @click="showProductForm = false">取消</button>
              <button class="btn-sm" @click="saveProduct" :disabled="productSaving">{{ productSaving ? '上传中...' : (editingProduct ? '保存' : '添加') }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 积分兑换 -->
      <div v-if="activeTab === 'exchange'" class="tab-content">
        <div class="section-header">
          <h3>积分兑换设置</h3>
        </div>
        <table class="data-table">
          <thead><tr><th>图</th><th>ID</th><th>名称</th><th>品类</th><th>售价</th><th>库存</th><th>兑换积分</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="pr in productList" :key="pr.id">
              <td><img v-if="pr.images" :src="pr.images" class="product-thumb" /></td>
              <td>{{ pr.id }}</td>
              <td>{{ pr.name }}</td>
              <td>{{ ['','竿','轮','线','漂','饵','配件'][pr.category] || pr.category }}</td>
              <td>¥{{ pr.price }}</td>
              <td>{{ pr.stock }}</td>
              <td>
                <span v-if="pr.pointsPrice > 0" class="tag-ok">{{ pr.pointsPrice }}积分</span>
                <span v-else class="tag-bad">未设置</span>
              </td>
              <td>
                <button class="btn-sm" @click="openExchangeSetting(pr)">设置</button>
                <button v-if="pr.pointsPrice > 0" class="btn-sm btn-danger" @click="clearExchange(pr.id)">取消兑换</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="showExchangeModal" class="modal-overlay" @click.self="showExchangeModal = false">
          <div class="modal-card">
            <h3>设置积分兑换</h3>
            <p style="color:#666;font-size:14px">{{ exchangeTarget?.name }}</p>
            <div class="form-group">
              <label>兑换所需积分</label>
              <input v-model="exchangePoints" type="number" placeholder="输入积分数，0表示取消兑换" />
            </div>
            <div class="modal-btns">
              <button class="btn-cancel" @click="showExchangeModal = false">取消</button>
              <button class="btn-sm" @click="saveExchange">确认</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 管理员管理（仅超级管理员可见） -->
      <div v-if="activeTab === 'admins'" class="tab-content">
        <div class="section-header">
          <h3>管理员列表</h3>
          <button class="btn-sm" @click="showAdminForm = true">+ 添加管理员</button>
        </div>
        <table class="data-table">
          <thead><tr><th>ID</th><th>账号</th><th>姓名</th><th>角色</th><th>创建时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="a in adminList" :key="a.id">
              <td>{{ a.id }}</td>
              <td>{{ a.account }}</td>
              <td>{{ a.name }}</td>
              <td><span :class="a.role === 1 ? 'tag-ok' : ''">{{ a.role === 1 ? '超级管理员' : '小管理员' }}</span></td>
              <td>{{ a.createTime }}</td>
              <td>
                <button v-if="a.id !== adminInfo.id" class="btn-sm btn-danger" @click="deleteAdmin(a.id)">删除</button>
                <span v-else class="tag-bad">当前账号</span>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="showAdminForm" class="modal-overlay" @click.self="showAdminForm = false">
          <div class="modal-card">
            <h3>添加管理员</h3>
            <div class="form-group"><label>账号</label><input v-model="newAdmin.account" /></div>
            <div class="form-group"><label>密码</label><input v-model="newAdmin.password" type="password" /></div>
            <div class="form-group"><label>姓名</label><input v-model="newAdmin.name" /></div>
            <div class="form-group">
              <label>角色</label>
              <select v-model="newAdmin.role">
                <option :value="0">小管理员</option>
                <option :value="1">超级管理员</option>
              </select>
            </div>
            <p v-if="adminFormErr" class="error">{{ adminFormErr }}</p>
            <div class="modal-btns">
              <button class="btn-cancel" @click="showAdminForm = false">取消</button>
              <button class="btn-sm" @click="createAdmin">确认添加</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index.js'
import * as echarts from 'echarts'

const router = useRouter()
const isLoggedIn = ref(false)
const adminInfo = ref(null)
const activeTab = ref('dashboard')

const isSuperAdmin = computed(() => adminInfo.value && adminInfo.value.role === 1)

const baseTabs = [
  { key: 'dashboard', label: '数据概览' },
  { key: 'users', label: '用户管理' },
  { key: 'audit', label: '内容审核' },
  { key: 'spots', label: '钓点管理' },
  { key: 'posts', label: '帖子管理' },
  { key: 'venues', label: '钓场管理' },
  { key: 'products', label: '商品管理' },
  { key: 'exchange', label: '积分兑换' }
]
const adminTab = { key: 'admins', label: '管理员管理' }
const tabs = computed(() => isSuperAdmin.value ? [...baseTabs, adminTab] : baseTabs)

const dash = ref({ userCount: 0, venueCount: 0, orderCount: 0, postCount: 0, productCount: 0, spotCount: 0, pendingSpotCount: 0, pendingPostCount: 0 })

// 图表 DOM refs
const chartAge = ref(null)
const chartFish = ref(null)
const chartRegion = ref(null)
const chartMethod = ref(null)
let chartInstances = []

const userList = ref([]); const userPage = ref(1); const userTotal = ref(0); const userKeyword = ref('')

const auditType = ref('spot'); const pendingList = ref([])
const showRejectModal = ref(false); const rejectReason = ref(''); const rejectTargetId = ref(null)

const spotFilter = ref(undefined); const spotList = ref([])
const postFilter = ref(undefined); const postList = ref([])

const venueList = ref([])

const productList = ref([])
const showProductForm = ref(false); const editingProduct = ref(null); const productSaving = ref(false)
const productForm = ref({ name: '', category: 1, brand: '', price: 0, stock: 0, description: '', images: '' })
const productImagePreview = ref('')
const productImageFile = ref(null)

const adminList = ref([])
const showAdminForm = ref(false); const adminFormErr = ref('')
const newAdmin = ref({ account: '', password: '', name: '', role: 0 })

const statusClass = (s) => {
  if (s === 0) return 'tag-warn'
  if (s === 1) return 'tag-ok'
  if (s === 2) return 'tag-bad'
  return ''
}
const statusLabel = (s) => {
  if (s === 0) return '待审'
  if (s === 1) return '通过'
  if (s === 2) return '驳回'
  return s
}

const goLogin = () => {
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('adminToken')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

const onTabChange = () => {
  // 切出 dashboard 时销毁图表
  if (activeTab.value !== 'dashboard') {
    disposeCharts()
  }
  const map = {
    dashboard: () => { fetchDashboard(); fetchCharts() },
    users: fetchUsers, audit: fetchPending,
    spots: fetchSpots, posts: fetchPosts, venues: fetchVenues,
    products: fetchProducts, exchange: fetchProducts, admins: fetchAdmins
  }
  map[activeTab.value]?.()
}

const fetchDashboard = async () => {
  try {
    const res = await request.get('/admin/dashboard')
    if (res.code === 200) dash.value = res.data
  } catch (e) { console.error(e) }
}

// ===== ECharts 图表 =====

const fetchCharts = async () => {
  try {
    const res = await request.get('/admin/dashboard/charts')
    if (res.code === 200) {
      await nextTick()
      initCharts(res.data)
    }
  } catch (e) { console.error('获取图表数据失败', e) }
}

const disposeCharts = () => {
  chartInstances.forEach(c => { try { c.dispose() } catch (e) { /* ignore */ } })
  chartInstances = []
}

const makeBarOption = (data, title) => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '8%', bottom: '3%', top: '8%', containLabel: true },
  xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: data.length > 6 ? 30 : 0, fontSize: 11 } },
  yAxis: { type: 'value' },
  series: [{
    name: title, type: 'bar',
    data: data.map(d => d.value),
    itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#1a73e8' }, { offset: 1, color: '#89c4f4' }
    ]), borderRadius: [4, 4, 0, 0] }
  }]
})

const makePieOption = (data, title) => ({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { fontSize: 11 } },
  series: [{
    name: title, type: 'pie',
    radius: ['40%', '70%'],
    center: ['40%', '50%'],
    data: data.map(d => ({ name: d.name, value: d.value })),
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
    label: { show: false }
  }]
})

const initCharts = (data) => {
  disposeCharts()

  // 1. 年龄分布 — 柱状图
  if (chartAge.value && data.ageDistribution?.length) {
    const c = echarts.init(chartAge.value)
    c.setOption(makeBarOption(data.ageDistribution, '年龄分布'))
    chartInstances.push(c)
  }

  // 2. 主要鱼种分布 — 柱状图（横向更直观）
  if (chartFish.value && data.fishSpeciesDistribution?.length) {
    const c = echarts.init(chartFish.value)
    const d = data.fishSpeciesDistribution
    c.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: d.map(i => i.name), axisLabel: { fontSize: 11 }, inverse: true },
      series: [{
        name: '鱼种', type: 'bar',
        data: d.map(i => i.value),
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#4caf50' }, { offset: 1, color: '#a5d6a7' }
        ]), borderRadius: [0, 4, 4, 0] }
      }]
    })
    chartInstances.push(c)
  }

  // 3. 地域分布 — 柱状图
  if (chartRegion.value && data.regionDistribution?.length) {
    const c = echarts.init(chartRegion.value)
    c.setOption(makeBarOption(data.regionDistribution, '地域分布'))
    chartInstances.push(c)
  }

  // 4. 钓法分布 — 饼图
  if (chartMethod.value && data.methodDistribution?.length) {
    const c = echarts.init(chartMethod.value)
    c.setOption(makePieOption(data.methodDistribution, '钓法分布'))
    chartInstances.push(c)
  }

  // 响应式 resize
  window.addEventListener('resize', () => {
    chartInstances.forEach(c => { try { c.resize() } catch (e) { /* ignore */ } })
  }, { once: true })
}

const fetchUsers = async () => {
  try {
    const res = await request.get('/admin/users', { params: { page: userPage.value, size: 10, keyword: userKeyword.value } })
    if (res.code === 200) { userList.value = res.data.list || []; userTotal.value = res.data.totalCount || 0 }
  } catch (e) { console.error(e) }
}

const lockUser = async (id, state) => {
  await request.put(`/admin/user/lock/${id}`, null, { params: { state } })
  fetchUsers()
}

const fetchPending = async () => {
  try {
    const res = await request.get('/audit/pending', { params: { type: auditType.value, page: 1, size: 20 } })
    if (res.code === 200) pendingList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const audit = async (targetId, action) => {
  await request.post('/audit/do', { targetType: auditType.value, targetId, action, reason: action === 'reject' ? rejectReason.value : '' })
  fetchPending()
}

const showReject = (id) => { rejectTargetId.value = id; rejectReason.value = ''; showRejectModal.value = true }
const confirmReject = () => {
  if (!rejectReason.value) { alert('请输入驳回原因'); return }
  audit(rejectTargetId.value, 'reject')
  showRejectModal.value = false
}

// Spots
const fetchSpots = async () => {
  try {
    const params = { page: 1, size: 50 }
    if (spotFilter.value !== undefined) params.status = spotFilter.value
    const res = await request.get('/admin/spots', { params })
    if (res.code === 200) spotList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const setSpotStatus = async (id, status) => {
  await request.put(`/admin/spot/status/${id}`, null, { params: { status } })
  fetchSpots()
}

const deleteSpot = async (id) => {
  if (!confirm('确认删除该钓点？')) return
  await request.delete(`/admin/spot/${id}`)
  fetchSpots()
}

// Posts
const fetchPosts = async () => {
  try {
    const params = { page: 1, size: 50 }
    if (postFilter.value !== undefined) params.status = postFilter.value
    const res = await request.get('/admin/posts', { params })
    if (res.code === 200) postList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const setPostStatus = async (id, status) => {
  await request.put(`/admin/post/status/${id}`, null, { params: { status } })
  fetchPosts()
}

const deletePost = async (id) => {
  if (!confirm('确认删除该帖子？')) return
  await request.delete(`/admin/post/${id}`)
  fetchPosts()
}

// Venues
const fetchVenues = async () => {
  try {
    const res = await request.get('/admin/venues', { params: { page: 1, size: 50 } })
    if (res.code === 200) venueList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const setVenueStatus = async (id, status) => {
  await request.put(`/admin/venue/status/${id}`, null, { params: { status } })
  fetchVenues()
}

const deleteVenue = async (id) => {
  if (!confirm('确认删除该钓场？')) return
  await request.delete(`/admin/venue/${id}`)
  fetchVenues()
}

// Products
const fetchProducts = async () => {
  try {
    const res = await request.get('/admin/products', { params: { page: 1, size: 50 } })
    if (res.code === 200) productList.value = res.data.list || []
  } catch (e) { console.error(e) }
}

const resetProductForm = () => {
  productForm.value = { name: '', category: 1, brand: '', price: 0, stock: 0, description: '', images: '' }
  productImagePreview.value = ''
  productImageFile.value = null
}

const openEditProduct = (p) => {
  editingProduct.value = p
  productForm.value = { name: p.name, category: p.category, brand: p.brand, price: p.price, stock: p.stock, description: p.description, images: p.images || '' }
  productImagePreview.value = p.images || ''
  productImageFile.value = null
  showProductForm.value = true
}

const onProductImageChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  productImageFile.value = file
  productImagePreview.value = URL.createObjectURL(file)
}

const saveProduct = async () => {
  try {
    productSaving.value = true
    let imageUrl = productForm.value.images || ''

    if (productImageFile.value) {
      const fd = new FormData()
      fd.append('files', productImageFile.value)
      const uploadRes = await request.post('/file/upload-images', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.length > 0) {
        imageUrl = uploadRes.data[0]
      }
    }

    const payload = { ...productForm.value, images: imageUrl }

    if (editingProduct.value) {
      await request.put(`/admin/product/${editingProduct.value.id}`, payload)
    } else {
      await request.post('/admin/product', payload)
    }
    showProductForm.value = false
    editingProduct.value = null
    resetProductForm()
    fetchProducts()
  } catch (e) { alert('操作失败') }
  finally { productSaving.value = false }
}

const toggleProductStatus = async (id, status) => {
  await request.put(`/admin/product/status/${id}`, null, { params: { status } })
  fetchProducts()
}

const deleteProduct = async (id) => {
  if (!confirm('确认删除该商品？')) return
  await request.delete(`/admin/product/${id}`)
  fetchProducts()
}

// Exchange
const showExchangeModal = ref(false)
const exchangeTarget = ref(null)
const exchangePoints = ref(0)

const openExchangeSetting = (p) => {
  exchangeTarget.value = p
  exchangePoints.value = p.pointsPrice || 0
  showExchangeModal.value = true
}

const saveExchange = async () => {
  try {
    const pts = parseInt(exchangePoints.value) || 0
    await request.put(`/admin/product/${exchangeTarget.value.id}`, { pointsPrice: pts })
    showExchangeModal.value = false
    exchangeTarget.value = null
    fetchProducts()
  } catch (e) { alert('设置失败') }
}

const clearExchange = async (id) => {
  if (!confirm('确认取消该商品的积分兑换？')) return
  await request.put(`/admin/product/${id}`, { pointsPrice: 0 })
  fetchProducts()
}

// Admins
const fetchAdmins = async () => {
  try {
    const res = await request.get('/admin/admins')
    if (res.code === 200) adminList.value = res.data || []
  } catch (e) { console.error(e) }
}

const createAdmin = async () => {
  adminFormErr.value = ''
  if (!newAdmin.value.account || !newAdmin.value.password || !newAdmin.value.name) {
    adminFormErr.value = '请填写完整信息'
    return
  }
  try {
    const res = await request.post('/admin/admin/create', null, { params: newAdmin.value })
    if (res.code === 200) {
      showAdminForm.value = false
      newAdmin.value = { account: '', password: '', name: '', role: 0 }
      fetchAdmins()
    } else {
      adminFormErr.value = res.msg || '添加失败'
    }
  } catch (e) {
    adminFormErr.value = '添加失败'
  }
}

const deleteAdmin = async (id) => {
  if (!confirm('确认删除该管理员？')) return
  try {
    await request.delete(`/admin/admin/delete/${id}`)
    fetchAdmins()
  } catch (e) { alert('删除失败') }
}

onMounted(() => {
  const token = localStorage.getItem('adminToken')
  if (!token) {
    router.push('/login')
    return
  }
  const infoStr = localStorage.getItem('userInfo')
  if (infoStr) {
    try {
      const info = JSON.parse(infoStr)
      if (info.userType === 'admin') {
        adminInfo.value = { name: info.nickname, role: info.adminRole, id: info.userId }
      }
    } catch (e) { /* ignore */ }
  }
  isLoggedIn.value = true
  fetchDashboard()
  fetchCharts()
})
</script>

<style scoped>
.admin-page { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; gap: 16px; padding: 12px 24px;
  background: #1a73e8; color: #fff;
}
.header h2 { margin: 0; flex: 1; }
.admin-name { font-size: 13px; opacity: 0.9; }
.btn-logout { padding: 6px 16px; background: rgba(255,255,255,0.2); color: #fff; border: none; border-radius: 4px; cursor: pointer; }

.login-section { display: flex; justify-content: center; padding: 48px; }
.login-card { background: #fff; padding: 32px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
.login-card h3 { margin: 0 0 24px; text-align: center; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 6px; color: #555; font-size: 14px; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-size: 14px;
}
.form-group textarea { min-height: 60px; resize: vertical; }
.error { color: #ff4444; font-size: 13px; margin-bottom: 12px; }
.btn-login { width: 100%; padding: 12px; background: #1a73e8; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; }

.admin-content { padding: 0; }
.tabs {
  display: flex; gap: 0; background: #fff; border-bottom: 2px solid #eee; padding: 0 24px;
  overflow-x: auto; flex-wrap: wrap;
}
.tabs button {
  padding: 14px 20px; border: none; background: transparent; cursor: pointer;
  font-size: 14px; color: #666; border-bottom: 2px solid transparent; margin-bottom: -2px; white-space: nowrap;
}
.tabs button:hover { color: #1a73e8; }
.tabs button.active { color: #1a73e8; border-bottom-color: #1a73e8; font-weight: bold; }

.tab-content { max-width: 1100px; margin: 24px auto; padding: 0 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { margin: 0; }
.empty { text-align: center; padding: 48px; color: #999; }

.stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.stat-card {
  background: #fff; padding: 24px; border-radius: 12px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.stat-num { display: block; font-size: 36px; font-weight: bold; color: #1a73e8; }
.stat-label { display: block; font-size: 13px; color: #999; margin-top: 4px; }

.data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; font-size: 13px; }
.data-table th, .data-table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f5f5f5; font-weight: bold; white-space: nowrap; }

.tag-ok { color: #4caf50; font-weight: bold; }
.tag-bad { color: #ff4444; font-weight: bold; }
.tag-warn { color: #ff9800; font-weight: bold; }

.btn-sm {
  padding: 5px 14px; background: #1a73e8; color: #fff; border: none;
  border-radius: 4px; cursor: pointer; font-size: 12px; margin: 0 2px;
}
.btn-ok { background: #4caf50; }
.btn-danger { background: #ff4444; }
.btn-warn { background: #ff9800; }
.btn-cancel { padding: 6px 16px; background: #eee; color: #333; border: none; border-radius: 4px; cursor: pointer; }

.sub-tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.sub-tabs button {
  padding: 8px 20px; border: 1px solid #ddd; background: #fff;
  border-radius: 6px; cursor: pointer; font-size: 13px;
}
.sub-tabs button.active { background: #1a73e8; color: #fff; border-color: #1a73e8; }
.pending-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 16px; border-radius: 8px;
  margin-bottom: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.pending-info { flex: 1; }
.pending-info h4 { margin: 0 0 6px; font-size: 15px; }
.pending-info p { margin: 0; color: #666; font-size: 13px; }
.pending-actions { display: flex; gap: 8px; margin-left: 16px; }

.search-row { display: flex; gap: 8px; margin-bottom: 16px; }
.search-row input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; flex: 1; max-width: 300px; }

.pager { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 20px; }
.pager button { padding: 6px 14px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.pager button:disabled { opacity: 0.5; cursor: not-allowed; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-card {
  background: #fff; padding: 24px; border-radius: 12px; width: 90%; max-width: 420px; max-height: 90vh; overflow-y: auto;
}
.modal-wide { max-width: 520px; }
.modal-card h3 { margin: 0 0 16px; }
.modal-card textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; resize: vertical; box-sizing: border-box; }
.modal-btns { display: flex; gap: 12px; justify-content: flex-end; margin-top: 16px; }

.product-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
.img-preview { max-width: 200px; max-height: 150px; margin-top: 8px; border-radius: 6px; border: 1px solid #eee; }

/* 图表 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 24px;
}
.chart-box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.chart-title {
  margin: 0 0 12px;
  font-size: 15px;
  color: #333;
  font-weight: 600;
}
.chart-item {
  width: 100%;
  height: 320px;
}

@media (max-width: 768px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
