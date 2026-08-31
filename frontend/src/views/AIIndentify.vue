<template>
  <div class="ai-identify">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>AI识鱼</h2>
    </header>

    <div class="content">
      <div class="upload-area" @click="triggerUpload">
        <div v-if="!previewUrl" class="upload-placeholder">
          <div class="icon">📷</div>
          <p>点击拍照或选择照片</p>
        </div>
        <img v-else :src="previewUrl" class="preview" />
        <input ref="fileInput" type="file" accept="image/*" @change="handleFileChange" hidden />
      </div>

      <button class="btn-identify" @click="identify" :disabled="!selectedFile || identifying">
        {{ identifying ? '识别中...' : '开始识别' }}
      </button>

      <div v-if="result" class="result-card">
        <h3>识别结果</h3>
        <div class="result-item">
          <span class="label">鱼种</span>
          <span class="value">{{ result.fishName }}</span>
        </div>
        <div class="result-item">
          <span class="label">置信度</span>
          <span class="value">{{ (result.confidence * 100).toFixed(1) }}%</span>
        </div>
        <div class="result-item">
          <span class="label">简介</span>
          <span class="value">{{ result.description }}</span>
        </div>
        <div class="result-item">
          <span class="label">推荐钓法</span>
          <span class="value">{{ result.fishingTips }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../api/index.js'

const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const identifying = ref(false)
const result = ref(null)

const triggerUpload = () => fileInput.value.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    selectedFile.value = file
    previewUrl.value = URL.createObjectURL(file)
    result.value = null
  }
}

const identify = async () => {
  if (!selectedFile.value) return
  identifying.value = true
  try {
    const formData = new FormData()
    formData.append('image', selectedFile.value)
    const res = await request.post('/ai/identify', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      result.value = res.data
    }
  } catch (e) {
    console.error('识别失败', e)
    alert('识别失败，请重试')
  } finally {
    identifying.value = false
  }
}
</script>

<style scoped>
.ai-identify { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; }
.content { max-width: 500px; margin: 24px auto; padding: 0 16px; }
.upload-area {
  border: 2px dashed #ddd; border-radius: 12px; padding: 40px;
  text-align: center; cursor: pointer; background: #fff;
  transition: border-color 0.2s;
}
.upload-area:hover { border-color: #1a73e8; }
.upload-placeholder .icon { font-size: 48px; }
.upload-placeholder p { color: #999; margin-top: 12px; }
.preview { max-width: 100%; max-height: 300px; border-radius: 8px; }
.btn-identify {
  width: 100%; padding: 14px; margin-top: 16px; background: #1a73e8;
  color: #fff; border: none; border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-identify:disabled { opacity: 0.6; cursor: not-allowed; }
.result-card {
  background: #fff; border-radius: 12px; padding: 24px;
  margin-top: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.result-card h3 { margin: 0 0 16px; }
.result-item { margin-bottom: 12px; }
.result-item .label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.result-item .value { font-size: 15px; color: #333; }
</style>
