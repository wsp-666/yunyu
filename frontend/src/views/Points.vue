<template>
  <div class="points-page">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>积分中心</h2>
    </header>

    <div class="points-card">
      <div class="points-balance">
        <span class="label">当前积分</span>
        <span class="value">{{ totalPoints }}</span>
      </div>
      <button class="btn-signin" @click="dailySignIn" :disabled="signedIn">
        {{ signedIn ? '已签到' : '每日签到 +5' }}
      </button>
    </div>

    <div class="section">
      <h3>会员中心</h3>
      <div v-if="memberInfo.memberLevel > 0" class="member-status">
        <div class="member-badge">👑 会员已激活</div>
        <p>到期时间：{{ formatTime(memberInfo.memberExpire) }}</p>
      </div>
      <div class="member-plans">
        <div v-for="plan in memberPlans" :key="plan.months" class="plan-card" :class="{ popular: plan.popular }">
          <div v-if="plan.popular" class="plan-tag">推荐</div>
          <h4>{{ plan.label }}</h4>
          <p class="plan-desc">{{ plan.desc }}</p>
          <div class="plan-price"><span class="rmb">¥</span>{{ plan.price }}</div>
          <ul class="plan-benefits">
            <li v-for="b in plan.benefits" :key="b">✓ {{ b }}</li>
          </ul>
          <button class="btn-upgrade" @click="upgradeMember(plan.months)">
            立即开通
          </button>
        </div>
      </div>
    </div>

    <div class="section">
      <h3>积分明细</h3>
      <div v-for="log in pointsLogList" :key="log.id" class="log-item">
        <div class="log-info">
          <span class="log-type">{{ getLogType(log.type) }}</span>
          <span class="log-time">{{ formatTime(log.createTime) }}</span>
        </div>
        <span :class="log.points > 0 ? 'positive' : 'negative'">
          {{ log.points > 0 ? '+' : '' }}{{ log.points }}
        </span>
      </div>
      <div v-if="pointsLogList.length === 0" class="empty">暂无积分记录</div>
    </div>

    <div class="section">
      <h3>积分兑换商品</h3>
      <div v-if="exchangeProducts.length === 0" class="empty">暂无兑换商品</div>
      <div class="product-list">
        <div v-for="product in exchangeProducts" :key="product.id" class="product-card">
          <img v-if="product.images" :src="product.images" class="product-img" />
          <div v-else class="product-img-placeholder">🎁</div>
          <div class="product-body">
            <h4>{{ product.name }}</h4>
            <p class="product-brand" v-if="product.brand">{{ product.brand }}</p>
            <p class="product-desc" v-if="product.description">{{ (product.description || '').substring(0, 40) }}</p>
            <div class="product-footer">
              <span class="product-price">{{ product.pointsPrice }}积分</span>
              <span class="product-stock">库存: {{ product.stock }}</span>
            </div>
            <button class="btn-exchange" @click="exchange(product.id)"
                    :disabled="product.stock <= 0 || totalPoints < product.pointsPrice">
              {{ product.stock <= 0 ? '已兑完' : (totalPoints < product.pointsPrice ? '积分不足' : '立即兑换') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index.js'

const totalPoints = ref(0)
const pointsLogList = ref([])
const exchangeProducts = ref([])
const signedIn = ref(false)
const memberInfo = ref({ memberLevel: 0, memberExpire: null, points: 0 })

const memberPlans = [
  { months: 1, label: '月度会员', desc: '适合短期体验', price: 30, popular: false, benefits: ['AI语音指导', '会员专属标识', '签到双倍积分'] },
  { months: 6, label: '半年会员', desc: '性价比之选', price: 108, popular: true, benefits: ['AI语音指导', '会员专属标识', '签到双倍积分', '优先客服'] },
  { months: 12, label: '年度会员', desc: '钓鱼达人必备', price: 188, popular: false, benefits: ['AI语音指导', '会员专属标识', '签到双倍积分', '优先客服', '专属钓技课程'] }
]

const getLogType = (type) => {
  const map = { 1: '分享钓点', 2: '帮助他人', 3: '每日签到', 4: '积分兑换', 5: '系统奖励' }
  return map[type] || '其他'
}

const fetchData = async () => {
  try {
    const res = await request.get('/points/detail', { params: { page: 1, size: 50 } })
    if (res.code === 200) {
      totalPoints.value = res.data.totalPoints
      pointsLogList.value = res.data.pointsLogList?.list || []
    }
    const productRes = await request.get('/product/list', { params: { page: 1, size: 50 } })
    if (productRes.code === 200) {
      exchangeProducts.value = (productRes.data.list || []).filter(p => p.pointsPrice > 0)
    }
  } catch (e) {
    console.error('获取数据失败', e)
  }
}

const dailySignIn = async () => {
  try {
    const res = await request.post('/points/signin')
    if (res.code === 200) {
      totalPoints.value = res.data.totalPoints
      signedIn.value = true
      fetchData()
    } else {
      alert(res.msg)
    }
  } catch (e) {
    console.error('签到失败', e)
  }
}

const exchange = async (productId) => {
  if (!confirm('确认兑换该商品？')) return
  try {
    const res = await request.post('/points/exchange', null, { params: { productId } })
    if (res.code === 200) {
      alert('兑换成功')
      fetchData()
    } else {
      alert(res.msg || '兑换失败')
    }
  } catch (e) {
    alert(e.response?.data?.msg || '兑换失败')
  }
}

const fetchMemberInfo = async () => {
  try {
    const res = await request.get('/points/member-info')
    if (res.code === 200) {
      memberInfo.value = res.data
    }
  } catch (e) {
    console.error('获取会员信息失败', e)
  }
}

const upgradeMember = async (months) => {
  const plan = memberPlans.find(p => p.months === months)
  if (!confirm(`确认支付 ¥${plan.price} 开通${plan.label}？\n\n支付方式：微信 / 支付宝\n（演示环境：点击确认即完成支付）`)) return
  try {
    const res = await request.post('/points/upgrade-member', null, { params: { months } })
    if (res.code === 200) {
      alert(`支付成功！${plan.label}已激活`)
      fetchData()
      fetchMemberInfo()
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.memberLevel = res.data.memberLevel
      userInfo.memberExpire = res.data.memberExpire
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      alert(res.msg || '支付失败')
    }
  } catch (e) {
    alert(e.response?.data?.msg || '支付失败')
  }
}

const formatTime = (time) => time ? time.substring(0, 16).replace('T', ' ') : ''

onMounted(() => {
  fetchData()
  fetchMemberInfo()
})
</script>

<style scoped>
.points-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 40px; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.points-card {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  color: #fff; padding: 32px; text-align: center;
}
.points-balance { margin-bottom: 16px; }
.points-balance .label { display: block; font-size: 14px; opacity: 0.8; }
.points-balance .value { font-size: 48px; font-weight: bold; }
.btn-signin {
  padding: 10px 32px; background: rgba(255,255,255,0.2);
  color: #fff; border: 2px solid rgba(255,255,255,0.5);
  border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-signin:disabled { opacity: 0.5; cursor: not-allowed; }
.section { max-width: 600px; margin: 24px auto; padding: 0 16px; }
.section h3 { margin: 0 0 16px; font-size: 18px; color: #333; }
.empty { text-align: center; padding: 24px; color: #999; }
.log-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #fff; border-radius: 8px;
  margin-bottom: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.log-info { display: flex; flex-direction: column; }
.log-type { font-size: 14px; color: #333; }
.log-time { font-size: 12px; color: #999; }
.positive { color: #4caf50; font-weight: bold; }
.negative { color: #ff4444; font-weight: bold; }

.product-list { display: flex; flex-direction: column; gap: 12px; }
.product-card {
  display: flex; gap: 14px; background: #fff; border-radius: 12px;
  padding: 14px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.product-img { width: 100px; height: 75px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.product-img-placeholder {
  width: 100px; height: 75px; background: #f0f0f0;
  display: flex; align-items: center; justify-content: center;
  border-radius: 8px; font-size: 28px; flex-shrink: 0;
}
.product-body { flex: 1; min-width: 0; }
.product-body h4 { margin: 0 0 4px; font-size: 15px; }
.product-brand { margin: 0; color: #999; font-size: 12px; }
.product-desc { margin: 4px 0; color: #666; font-size: 12px; }
.product-footer { display: flex; gap: 12px; align-items: center; margin: 6px 0; }
.product-price { color: #ff4444; font-weight: bold; font-size: 18px; }
.product-stock { color: #999; font-size: 12px; }
.btn-exchange {
  padding: 6px 20px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.btn-exchange:disabled { background: #ccc; cursor: not-allowed; }

/* 会员中心样式 */
.member-status {
  background: linear-gradient(135deg, #ff9800, #f44336);
  color: #fff; padding: 16px 20px; border-radius: 10px; margin-bottom: 16px;
}
.member-status .member-badge { font-size: 16px; font-weight: bold; margin-bottom: 4px; }
.member-status p { margin: 0; font-size: 13px; opacity: 0.9; }
.member-plans { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
.plan-card {
  background: #fff; border-radius: 12px; padding: 20px 16px;
  text-align: center; position: relative;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.plan-card.popular { border: 2px solid #ff9800; }
.plan-tag {
  position: absolute; top: -10px; left: 50%; transform: translateX(-50%);
  background: #ff9800; color: #fff; padding: 2px 12px;
  border-radius: 10px; font-size: 12px; font-weight: bold;
}
.plan-card h4 { margin: 0 0 4px; font-size: 16px; color: #333; }
.plan-desc { margin: 0 0 12px; font-size: 12px; color: #999; }
.plan-price { font-size: 28px; font-weight: bold; color: #ff4444; }
.plan-price .rmb { font-size: 18px; }
.plan-benefits { list-style: none; padding: 0; margin: 12px 0; text-align: left; }
.plan-benefits li { font-size: 12px; color: #666; line-height: 2; }
.btn-upgrade {
  width: 100%; padding: 10px 0; background: #ff9800;
  color: #fff; border: none; border-radius: 8px;
  font-size: 14px; cursor: pointer; font-weight: bold;
}
.btn-upgrade:hover { background: #f57c00; }
</style>
