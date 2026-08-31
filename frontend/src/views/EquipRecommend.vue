<template>
  <div class="equip-page">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>AI装备推荐</h2>
    </header>

    <div class="content">
      <div class="form-card">
        <div class="form-group">
          <label>目标鱼种</label>
          <select v-model="targetFish">
            <option value="鲤鱼">鲤鱼</option>
            <option value="草鱼">草鱼</option>
            <option value="鲫鱼">鲫鱼</option>
            <option value="鲈鱼">鲈鱼</option>
            <option value="青鱼">青鱼</option>
            <option value="鲢鳙">鲢鳙</option>
            <option value="翘嘴">翘嘴</option>
            <option value="黑鱼">黑鱼</option>
            <option value="罗非">罗非</option>
            <option value="鳊鱼">鳊鱼</option>
          </select>
        </div>
        <div class="form-group">
          <label>钓法</label>
          <select v-model="method">
            <option value="台钓">台钓</option>
            <option value="路亚">路亚</option>
            <option value="溪流钓">溪流钓</option>
            <option value="抛竿">抛竿</option>
            <option value="筏钓">筏钓</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>预算下限</label>
            <input v-model.number="budgetMin" type="number" placeholder="0" />
          </div>
          <div class="form-group">
            <label>预算上限</label>
            <input v-model.number="budgetMax" type="number" placeholder="10000" />
          </div>
        </div>
        <button class="btn-recommend" @click="recommend" :disabled="recommending">
          {{ recommending ? 'AI生成中...' : 'AI生成方案' }}
        </button>
      </div>

      <div v-if="recommending" class="loading-card">
        <span class="loading-icon">⏳</span>
        <p>AI正在为您定制装备方案...</p>
      </div>

      <div v-if="plan && !recommending" class="plan-card">
        <h3>AI推荐方案</h3>
        <p class="plan-tags">
          <span class="tag">{{ plan.targetFish }}</span>
          <span class="tag">{{ plan.fishingMethod }}</span>
        </p>
        <div class="ai-analysis">{{ plan.analysis }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../api/index.js'

const targetFish = ref('鲤鱼')
const method = ref('台钓')
const budgetMin = ref(0)
const budgetMax = ref(5000)
const recommending = ref(false)
const plan = ref(null)

const recommend = async () => {
  recommending.value = true
  plan.value = null
  try {
    const res = await request.get('/equip/recommend', {
      params: {
        targetFish: targetFish.value,
        budgetMin: budgetMin.value,
        budgetMax: budgetMax.value,
        method: method.value
      }
    })
    if (res.code === 200) {
      plan.value = res.data
    }
  } catch (e) {
    console.error('获取推荐失败', e)
  } finally {
    recommending.value = false
  }
}
</script>

<style scoped>
.equip-page { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.content { max-width: 600px; margin: 24px auto; padding: 0 16px; }
.form-card, .plan-card, .loading-card {
  background: #fff; border-radius: 12px; padding: 24px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; color: #555; font-size: 14px; }
.form-group input, .form-group select {
  width: 100%; padding: 10px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box;
}
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }
.btn-recommend {
  width: 100%; padding: 14px; background: #1a73e8; color: #fff;
  border: none; border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-recommend:disabled { opacity: 0.6; cursor: not-allowed; }
.loading-card { text-align: center; padding: 40px; }
.loading-icon { font-size: 36px; display: block; margin-bottom: 12px; }
.loading-card p { color: #666; font-size: 15px; }
.plan-card h3 { margin: 0 0 12px; }
.plan-tags { display: flex; gap: 8px; margin-bottom: 20px; }
.tag {
  display: inline-block; padding: 4px 12px; background: #e8f0fe;
  color: #1a73e8; border-radius: 12px; font-size: 13px;
}
.ai-analysis { white-space: pre-line; line-height: 1.8; color: #444; font-size: 15px; }
</style>
