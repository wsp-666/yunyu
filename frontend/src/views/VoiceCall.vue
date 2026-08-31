<template>
  <div class="voice-call-page">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>🎣 AI语音指导</h2>
      <span class="member-badge">会员专享</span>
    </header>

    <!-- 非会员提示 -->
    <div v-if="!isMember" class="non-member">
      <div class="lock-icon">🔒</div>
      <h3>语音通话为会员专享功能</h3>
      <p>开通会员后可享受AI实时语音钓鱼指导</p>
      <button class="btn-upgrade" @click="$router.push('/points')">去开通会员</button>
    </div>

    <!-- 会员通话界面 -->
    <div v-else class="call-container">
      <!-- 未通话状态 -->
      <div v-if="!callActive" class="idle-screen">
        <div class="ai-avatar">🎣</div>
        <h3>AI钓鱼大师</h3>
        <p>在线 · 随时为您提供钓鱼指导</p>
        <button class="btn-start" @click="startCall">开始通话</button>
        <div class="tips">
          <p>💡 提示：</p>
          <ul>
            <li>点击"开始通话"后直接说话即可</li>
            <li>AI会在您说完后自动回复</li>
            <li>支持连续对话，像打电话一样</li>
            <li>建议使用耳机以获得更好的体验</li>
          </ul>
        </div>
      </div>

      <!-- 通话中 -->
      <div v-else class="active-call">
        <div class="call-header">
          <div class="call-avatar" :class="{ speaking: callStatus === 'speaking', listening: callStatus === 'listening' }">
            🎣
          </div>
          <div class="call-info">
            <h3>AI钓鱼大师</h3>
            <p class="status-text">{{ statusText }}</p>
          </div>
          <div class="call-timer">{{ callTimer }}</div>
        </div>

        <!-- 对话记录 -->
        <div class="transcript" ref="transcriptEl">
          <div v-for="(item, idx) in transcript" :key="idx" :class="item.role">
            <div class="t-avatar">{{ item.role === 'user' ? '👤' : '🎣' }}</div>
            <div class="t-bubble">
              <div class="t-label">{{ item.role === 'user' ? '我' : 'AI大师' }}</div>
              <div class="t-text">{{ item.text }}</div>
            </div>
          </div>
        </div>

        <!-- 状态指示器 -->
        <div class="call-status-bar">
          <div class="status-indicator">
            <div v-if="callStatus === 'listening'" class="pulse-ring"></div>
            <div v-if="callStatus === 'thinking'" class="dot-pulse"></div>
            <div v-if="callStatus === 'speaking'" class="wave-bars">
              <span></span><span></span><span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- 控制按钮 -->
        <div class="call-controls">
          <button class="btn-end-call" @click="endCall">结束通话</button>
          <button v-if="callStatus === 'speaking'" class="btn-skip" @click="skipSpeech">跳过播报</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import request from '../api/index.js'

const isMember = computed(() => {
  const info = localStorage.getItem('userInfo')
  if (!info) return false
  const user = JSON.parse(info)
  if (user.memberLevel <= 0) return false
  if (user.memberExpire) {
    const expire = new Date(user.memberExpire)
    if (expire < new Date()) return false
  }
  return true
})

const callActive = ref(false)
const callStatus = ref('idle') // idle | listening | thinking | speaking
const transcript = ref([])
const callTimer = ref('00:00')
const transcriptEl = ref(null)

let recognition = null
let timerInterval = null
let callSeconds = 0
let shouldRestartListen = true

const statusText = computed(() => {
  switch (callStatus.value) {
    case 'listening': return '正在聆听...'
    case 'thinking': return '思考中...'
    case 'speaking': return '正在回复...'
    default: return '就绪'
  }
})

onMounted(() => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (SpeechRecognition) {
    recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.interimResults = false
    recognition.continuous = false
    recognition.onresult = (e) => {
      const text = e.results[0][0].transcript
      if (text.trim()) {
        transcript.value.push({ role: 'user', text })
        scrollTranscript()
        processMessage(text)
      } else {
        startListening()
      }
    }
    recognition.onerror = (e) => {
      console.error('Speech recognition error:', e.error)
      if (e.error === 'no-speech' || e.error === 'aborted') {
        if (shouldRestartListen && callActive.value) {
          setTimeout(() => startListening(), 300)
        }
      } else {
        callStatus.value = 'idle'
      }
    }
    recognition.onend = () => {
      if (callStatus.value === 'listening') {
        callStatus.value = 'idle'
      }
    }
  }
})

onUnmounted(() => {
  cleanup()
})

const startCall = async () => {
  if (!recognition) {
    alert('您的浏览器不支持语音识别，请使用Chrome浏览器')
    return
  }
  try {
    await navigator.mediaDevices.getUserMedia({ audio: true })
  } catch (e) {
    alert('无法访问麦克风，请检查浏览器权限设置')
    return
  }
  callActive.value = true
  transcript.value = []
  callSeconds = 0
  shouldRestartListen = true
  callTimer.value = '00:00'
  timerInterval = setInterval(() => {
    callSeconds++
    const m = String(Math.floor(callSeconds / 60)).padStart(2, '0')
    const s = String(callSeconds % 60).padStart(2, '0')
    callTimer.value = m + ':' + s
  }, 1000)
  startListening()
}

const startListening = () => {
  if (!callActive.value || !shouldRestartListen) return
  callStatus.value = 'listening'
  try {
    recognition.start()
  } catch (e) {
    setTimeout(() => {
      try { recognition.start() } catch (e2) {}
    }, 200)
  }
}

const processMessage = async (msg) => {
  if (!callActive.value) return
  callStatus.value = 'thinking'

  try {
    const res = await request.post('/ai/chat', { message: msg })
    if (res.code === 200) {
      const reply = res.data.reply
      transcript.value.push({ role: 'master', text: reply })
      scrollTranscript()
      await speakText(reply)
    } else {
      const errMsg = res.msg || '抱歉，请稍后再试'
      transcript.value.push({ role: 'master', text: errMsg })
      scrollTranscript()
      await speakText(errMsg)
    }
  } catch (e) {
    transcript.value.push({ role: 'master', text: '抱歉，我暂时无法回复，请再说一遍' })
    scrollTranscript()
    await speakText('抱歉，我暂时无法回复，请再说一遍')
  }

  if (shouldRestartListen && callActive.value) {
    setTimeout(() => startListening(), 500)
  }
}

const speakText = (text) => {
  return new Promise((resolve) => {
    if (!window.speechSynthesis) {
      resolve()
      return
    }
    window.speechSynthesis.cancel()
    callStatus.value = 'speaking'
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    utterance.rate = 1.1
    utterance.pitch = 1.0
    utterance.onend = () => {
      callStatus.value = 'idle'
      resolve()
    }
    utterance.onerror = () => {
      callStatus.value = 'idle'
      resolve()
    }
    window.speechSynthesis.speak(utterance)
  })
}

const skipSpeech = () => {
  window.speechSynthesis.cancel()
  callStatus.value = 'idle'
  if (shouldRestartListen && callActive.value) {
    setTimeout(() => startListening(), 300)
  }
}

const endCall = () => {
  shouldRestartListen = false
  callActive.value = false
  callStatus.value = 'idle'
  if (recognition) {
    try { recognition.stop() } catch (e) {}
  }
  window.speechSynthesis.cancel()
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const cleanup = () => {
  shouldRestartListen = false
  if (recognition) {
    try { recognition.abort() } catch (e) {}
  }
  window.speechSynthesis.cancel()
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const scrollTranscript = () => {
  nextTick(() => {
    if (transcriptEl.value) {
      transcriptEl.value.scrollTop = transcriptEl.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.voice-call-page {
  display: flex; flex-direction: column; height: 100vh;
  background: linear-gradient(135deg, #0f1923 0%, #1a2a3a 50%, #0d1b2a 100%);
  color: #fff;
}
.header {
  display: flex; align-items: center; padding: 12px 20px;
  background: rgba(0,0,0,0.3); flex-shrink: 0;
}
.back { color: #fff; text-decoration: none; margin-right: 12px; font-size: 18px; }
.header h2 { margin: 0; flex: 1; font-size: 18px; }
.member-badge {
  font-size: 12px; background: linear-gradient(135deg, #ff9800, #f44336);
  padding: 3px 10px; border-radius: 10px;
}

.non-member {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; padding: 40px;
}
.lock-icon { font-size: 64px; margin-bottom: 16px; }
.non-member h3 { margin: 0 0 8px; color: #ff9800; }
.non-member p { color: #aaa; margin: 0 0 24px; }
.btn-upgrade {
  padding: 12px 32px; background: linear-gradient(135deg, #ff9800, #f44336);
  color: #fff; border: none; border-radius: 24px; font-size: 16px; cursor: pointer;
}

.call-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

.idle-screen {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; padding: 40px;
}
.ai-avatar {
  width: 100px; height: 100px; border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8, #4fc3f7);
  display: flex; align-items: center; justify-content: center;
  font-size: 50px; margin-bottom: 20px;
  box-shadow: 0 0 30px rgba(26,115,232,0.3);
}
.idle-screen h3 { margin: 0 0 8px; font-size: 24px; }
.idle-screen > p { color: #aaa; margin: 0 0 32px; }
.btn-start {
  padding: 16px 48px; background: linear-gradient(135deg, #1a73e8, #4fc3f7);
  color: #fff; border: none; border-radius: 30px; font-size: 18px;
  cursor: pointer; letter-spacing: 2px;
  box-shadow: 0 4px 20px rgba(26,115,232,0.4);
  transition: transform 0.2s;
}
.btn-start:hover { transform: scale(1.05); }
.tips {
  margin-top: 40px; background: rgba(255,255,255,0.05);
  border-radius: 12px; padding: 20px 24px; max-width: 400px;
}
.tips p { margin: 0 0 8px; color: #ccc; font-size: 14px; }
.tips ul { margin: 0; padding-left: 20px; color: #aaa; font-size: 13px; line-height: 2; }

.active-call {
  flex: 1; display: flex; flex-direction: column; overflow: hidden;
}
.call-header {
  display: flex; align-items: center; padding: 20px;
  background: rgba(0,0,0,0.2); gap: 16px;
}
.call-avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8, #4fc3f7);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; transition: all 0.3s;
}
.call-avatar.listening {
  box-shadow: 0 0 20px rgba(26,115,232,0.6);
  animation: avatarPulse 1.5s infinite;
}
.call-avatar.speaking {
  box-shadow: 0 0 20px rgba(76,175,80,0.6);
  animation: avatarPulse 0.8s infinite;
}
@keyframes avatarPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}
.call-info { flex: 1; }
.call-info h3 { margin: 0; font-size: 18px; }
.status-text { margin: 4px 0 0; font-size: 13px; color: #aaa; }
.call-timer { font-size: 14px; color: #aaa; font-variant-numeric: tabular-nums; }

.transcript {
  flex: 1; overflow-y: auto; padding: 16px;
  display: flex; flex-direction: column; gap: 12px;
}
.transcript > div { display: flex; gap: 10px; align-items: flex-start; }
.transcript .master { flex-direction: row; }
.transcript .user { flex-direction: row-reverse; }
.t-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: rgba(255,255,255,0.1);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}
.t-bubble {
  max-width: 80%; padding: 10px 14px; border-radius: 12px;
}
.user .t-bubble {
  background: rgba(26,115,232,0.3); border-bottom-right-radius: 4px;
}
.master .t-bubble {
  background: rgba(255,255,255,0.08); border-bottom-left-radius: 4px;
}
.t-label { font-size: 11px; opacity: 0.6; margin-bottom: 2px; }
.t-text { font-size: 14px; line-height: 1.6; }

.call-status-bar {
  display: flex; justify-content: center; padding: 12px;
}
.status-indicator { position: relative; height: 40px; display: flex; align-items: center; }

.pulse-ring {
  width: 16px; height: 16px; border-radius: 50%; background: #ff4444;
  animation: pulseRing 1.5s infinite;
}
@keyframes pulseRing {
  0% { box-shadow: 0 0 0 0 rgba(255,68,68,0.6); }
  70% { box-shadow: 0 0 0 20px rgba(255,68,68,0); }
  100% { box-shadow: 0 0 0 0 rgba(255,68,68,0); }
}

.dot-pulse {
  width: 12px; height: 12px; border-radius: 50%; background: #ff9800;
  animation: dotPulse 1s infinite;
}
@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.wave-bars { display: flex; gap: 3px; align-items: flex-end; height: 20px; }
.wave-bars span {
  width: 3px; background: #4caf50; border-radius: 2px;
  animation: wave 0.6s infinite alternate;
}
.wave-bars span:nth-child(1) { height: 8px; }
.wave-bars span:nth-child(2) { height: 14px; animation-delay: 0.1s; }
.wave-bars span:nth-child(3) { height: 20px; animation-delay: 0.2s; }
.wave-bars span:nth-child(4) { height: 14px; animation-delay: 0.3s; }
.wave-bars span:nth-child(5) { height: 8px; animation-delay: 0.4s; }
@keyframes wave {
  0% { transform: scaleY(0.4); }
  100% { transform: scaleY(1); }
}

.call-controls {
  display: flex; justify-content: center; gap: 20px; padding: 20px;
  background: rgba(0,0,0,0.2);
}
.btn-end-call {
  padding: 14px 40px; background: #ff4444; color: #fff;
  border: none; border-radius: 30px; font-size: 16px; cursor: pointer;
  letter-spacing: 1px;
}
.btn-end-call:hover { background: #e53935; }
.btn-skip {
  padding: 14px 24px; background: rgba(255,255,255,0.15); color: #fff;
  border: 1px solid rgba(255,255,255,0.2); border-radius: 30px;
  font-size: 14px; cursor: pointer;
}
</style>
