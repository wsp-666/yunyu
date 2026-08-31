<template>
  <div class="order-page">
    <header class="header">
      <router-link :to="`/venue/${venueId}`" class="back">← 返回</router-link>
      <h2>购票</h2>
    </header>

    <div class="content" v-if="!loading">
      <!-- 已有订单 - 显示抽号 -->
      <div v-if="existingOrder" class="order-card">
        <h3>订单详情</h3>
        <div class="order-info">
          <p>订单号: {{ existingOrder.orderNo }}</p>
          <p>场次ID: {{ existingOrder.sessionId }}</p>
          <p>金额: ¥{{ existingOrder.amount }}</p>
          <p>支付状态: <span :class="existingOrder.payStatus === 1 ? 'tag-ok' : 'tag-bad'">{{ existingOrder.payStatus === 1 ? '已支付' : '未支付' }}</span></p>
          <p v-if="existingOrder.seatNo">钓位号: <strong>{{ existingOrder.seatNo }}号</strong></p>
        </div>

        <div v-if="existingOrder.payStatus === 0" class="actions">
          <button class="btn-pay" @click="doPay(existingOrder.id)" :disabled="paying">
            {{ paying ? '处理中...' : '立即支付' }}
          </button>
        </div>

        <div v-else-if="!existingOrder.seatNo" class="actions">
          <button class="btn-draw" :disabled="drawDisabled" @click="doDraw(existingOrder.id)">
            {{ drawBtnText }}
          </button>
          <p v-if="drawTip" class="draw-tip">{{ drawTip }}</p>
        </div>

        <div v-else class="actions">
          <p class="success">已抽号: {{ existingOrder.seatNo }}号</p>
          <router-link to="/" class="btn-home">返回首页</router-link>
        </div>
      </div>

      <!-- 无订单 - 购票 -->
      <div v-else class="order-card">
        <h3>确认订单</h3>
        <div class="order-info">
          <p>场次ID: {{ sessionId }}</p>
          <p>钓场ID: {{ venueId }}</p>
        </div>
        <button class="btn-pay" @click="createAndPay" :disabled="paying">
          {{ paying ? '处理中...' : '立即支付' }}
        </button>
      </div>

      <!-- 支付结果 -->
      <div v-if="payResult" class="result-card">
        <p class="success">支付成功！</p>
        <p>订单号: {{ payResult.orderNo }}</p>
        <p v-if="payResult.seatNo">钓位: {{ payResult.seatNo }}号</p>
        <button v-if="!payResult.seatNo" class="btn-draw" :disabled="drawDisabled" @click="doDraw(payResult.id)">
          {{ drawBtnText }}
        </button>
        <p v-if="drawTip && !payResult.seatNo" class="draw-tip">{{ drawTip }}</p>
        <router-link v-if="payResult.seatNo" to="/" class="btn-home">返回首页</router-link>
      </div>
    </div>

    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '../api/index.js'

const route = useRoute()
const sessionId = ref(0)
const venueId = ref(0)
const paying = ref(false)
const loading = ref(true)
const existingOrder = ref(null)
const payResult = ref(null)
const sessionInfo = ref(null)

const drawDisabled = computed(() => {
  if (!sessionInfo.value) return true
  const now = new Date()
  const drawTime = new Date(sessionInfo.value.drawTime)
  const endTime = new Date(sessionInfo.value.endTime)
  if (now < drawTime) return true
  if (now > endTime) return true
  return false
})

const drawBtnText = computed(() => {
  if (!sessionInfo.value) return '抽号'
  const now = new Date()
  const drawTime = new Date(sessionInfo.value.drawTime)
  const endTime = new Date(sessionInfo.value.endTime)
  if (now < drawTime) return `等待抽号 (${formatShort(drawTime)})`
  if (now > endTime) return '不在抽号时间内'
  return '抽号'
})

const drawTip = computed(() => {
  if (!sessionInfo.value) return ''
  const now = new Date()
  const drawTime = new Date(sessionInfo.value.drawTime)
  const endTime = new Date(sessionInfo.value.endTime)
  if (now < drawTime) return `抽号时间: ${formatShort(drawTime)}`
  if (now > endTime) return '已过抽号截止时间'
  return ''
})

onMounted(async () => {
  sessionId.value = parseInt(route.query.sessionId) || 0
  venueId.value = parseInt(route.query.venueId) || 0
  if (!sessionId.value || !venueId.value) return

  try {
    const res = await request.get('/order/my-session', { params: { sessionId: sessionId.value } })
    if (res.code === 200 && res.data) {
      existingOrder.value = res.data
      // 获取场次信息(抽号时间等)
      try {
        const sRes = await request.get(`/session/${sessionId.value}`)
        if (sRes.code === 200) sessionInfo.value = sRes.data
      } catch (e) { /* ignore */ }
    }
  } catch (e) { /* ignore */ }
  loading.value = false
})

const createAndPay = async () => {
  paying.value = true
  try {
    const createRes = await request.post('/order/create', { sessionId: sessionId.value })
    if (createRes.code === 200) {
      const orderId = createRes.data.orderId
      const payRes = await request.post('/order/pay', null, { params: { orderId } })
      if (payRes.code === 200) {
        const orderRes = await request.get('/order/seat', { params: { orderId } })
        if (orderRes.code === 200) {
          payResult.value = orderRes.data
          // 获取场次信息
          try {
            const sRes = await request.get(`/session/${sessionId.value}`)
            if (sRes.code === 200) sessionInfo.value = sRes.data
          } catch (e) { /* ignore */ }
        }
      }
    }
  } catch (e) {
    alert(e.response?.data?.msg || '购票失败')
  } finally {
    paying.value = false
  }
}

const doPay = async (orderId) => {
  paying.value = true
  try {
    const res = await request.post('/order/pay', null, { params: { orderId } })
    if (res.code === 200) {
      existingOrder.value.payStatus = 1
      payResult.value = existingOrder.value
    }
  } catch (e) {
    alert(e.response?.data?.msg || '支付失败')
  } finally {
    paying.value = false
  }
}

const doDraw = async (orderId) => {
  try {
    const res = await request.post('/order/draw', null, { params: { orderId } })
    if (res.code === 200) {
      const seatNo = res.data.seatNo
      if (existingOrder.value) existingOrder.value.seatNo = seatNo
      if (payResult.value) payResult.value.seatNo = seatNo
    } else {
      alert(res.msg || '抽号失败')
    }
  } catch (e) {
    alert(e.response?.data?.msg || '抽号失败')
  }
}

const formatShort = (d) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.order-page { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.content { max-width: 500px; margin: 24px auto; padding: 0 16px; }
.loading { text-align: center; padding: 48px; color: #999; }
.order-card, .result-card {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 16px;
}
.order-card h3, .result-card h3 { margin: 0 0 16px; }
.order-info p { margin: 8px 0; color: #555; }
.tag-ok { color: #4caf50; font-weight: bold; }
.tag-bad { color: #ff4444; font-weight: bold; }
.actions { margin-top: 16px; text-align: center; }
.btn-pay {
  width: 100%; padding: 14px; background: #ff4444;
  color: #fff; border: none; border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-pay:disabled { opacity: 0.6; }
.btn-draw {
  width: 100%; padding: 14px; background: #1a73e8;
  color: #fff; border: none; border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-draw:disabled { background: #ccc; cursor: not-allowed; }
.draw-tip { color: #999; font-size: 13px; margin-top: 8px; }
.success { color: #4caf50; font-weight: bold; margin: 16px 0; font-size: 18px; }
.btn-home {
  display: inline-block; padding: 10px 24px; background: #1a73e8;
  color: #fff; text-decoration: none; border-radius: 6px; margin-top: 8px;
}
.result-card { text-align: center; }
.result-card p { margin: 8px 0; }
</style>
