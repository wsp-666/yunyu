<template>
  <div class="weather-page">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>钓鱼天气</h2>
    </header>

    <div class="content">
      <div class="input-area">
        <input v-model="city" type="text" placeholder="输入城市名称，如：北京" @keyup.enter="fetchWeather" />
        <button @click="fetchWeather" :disabled="!city.trim() || loading">查询</button>
      </div>

      <div v-if="loading" class="loading">
        <span class="loading-icon">⏳</span>
        <p>正在获取{{ city }}的天气数据...</p>
      </div>

      <div v-if="weather && !loading" class="weather-result">
        <!-- 实际天气数据 -->
        <div class="weather-card">
          <h3 class="city-title">📍 {{ weather.city }}</h3>
          <div class="weather-main">
            <span class="temp">{{ weather.temperature }}</span>
            <span class="desc">{{ weather.weatherDesc }}</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="icon">🌡</span>
              <span class="label">体感温度</span>
              <span class="value">{{ weather.feelsLike }}</span>
            </div>
            <div class="detail-item">
              <span class="icon">💧</span>
              <span class="label">湿度</span>
              <span class="value">{{ weather.humidity }}</span>
            </div>
            <div class="detail-item">
              <span class="icon">🌬</span>
              <span class="label">风力</span>
              <span class="value">{{ weather.wind }}</span>
            </div>
            <div class="detail-item">
              <span class="icon">📊</span>
              <span class="label">气压</span>
              <span class="value">{{ weather.pressure }}</span>
            </div>
          </div>
        </div>

        <!-- AI钓鱼建议 -->
        <div class="advice-card">
          <h3>🎣 AI钓鱼建议</h3>
          <div class="ai-analysis">{{ weather.analysis }}</div>
        </div>
      </div>

      <div v-if="!weather && !loading" class="empty-tip">
        <p>输入城市名称，获取实时天气和钓鱼建议</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../api/index.js'

const city = ref('')
const weather = ref(null)
const loading = ref(false)

const fetchWeather = async () => {
  loading.value = true
  weather.value = null
  try {
    const res = await request.get('/weather/fishing', {
      params: { city: city.value.trim() }
    })
    if (res.code === 200) {
      weather.value = res.data
    }
  } catch (e) {
    console.error('获取天气失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.weather-page { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.content { max-width: 600px; margin: 24px auto; padding: 0 16px; }
.input-area { display: flex; gap: 8px; margin-bottom: 24px; }
.input-area input {
  flex: 1; padding: 12px; border: 1px solid #ddd;
  border-radius: 8px; font-size: 15px; outline: none;
}
.input-area input:focus { border-color: #1a73e8; }
.input-area button {
  padding: 12px 28px; background: #1a73e8; color: #fff;
  border: none; border-radius: 8px; cursor: pointer; font-size: 15px;
}
.input-area button:disabled { opacity: 0.5; cursor: not-allowed; }

.loading { text-align: center; padding: 60px 20px; }
.loading-icon { font-size: 40px; display: block; margin-bottom: 12px; }
.loading p { color: #666; font-size: 15px; }

.weather-card, .advice-card {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 16px;
}
.city-title { margin: 0 0 16px; font-size: 20px; color: #333; }
.weather-main { display: flex; align-items: baseline; gap: 12px; margin-bottom: 20px; }
.temp { font-size: 48px; font-weight: bold; color: #1a73e8; }
.desc { font-size: 18px; color: #666; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.detail-item {
  background: #f6f8fa; padding: 14px; border-radius: 10px;
  display: flex; align-items: center; gap: 8px;
}
.detail-item .icon { font-size: 20px; }
.detail-item .label { font-size: 12px; color: #999; }
.detail-item .value { font-size: 14px; color: #333; font-weight: bold; margin-left: auto; }

.advice-card h3 { margin: 0 0 16px; font-size: 18px; color: #333; }
.ai-analysis { white-space: pre-line; line-height: 1.8; color: #444; font-size: 15px; }

.empty-tip { text-align: center; padding: 60px 20px; color: #999; }
.empty-tip p { font-size: 15px; }
</style>
