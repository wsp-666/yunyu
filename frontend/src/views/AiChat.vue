<template>
  <div class="chat-page">
    <header class="header">
      <router-link to="/" class="back">← 返回</router-link>
      <h2>AI 钓鱼大师</h2>
      <span class="online-badge">在线</span>
    </header>

    <div class="chat-area" ref="chatArea">
      <div v-if="messages.length === 0" class="welcome">
        <div class="welcome-mark">云渔</div>
        <h3>你好，我是 AI 钓鱼大师</h3>
        <p>可以帮你解答选竿、打窝、路亚、冬钓等问题：</p>
        <div class="topics">
          <span v-for="t in topics" :key="t" class="topic-tag" @click="send(t)">{{ t }}</span>
        </div>
      </div>

      <div v-for="(msg, idx) in messages" :key="idx" :class="msg.role">
        <div class="avatar">{{ msg.role === 'user' ? '我' : '师' }}</div>
        <div class="bubble">
          <div class="text">{{ msg.text }}</div>
          <div class="time">{{ msg.time }}</div>
          <button v-if="msg.role === 'master'" class="btn-speak" @click="speak(msg.text)" title="播报">播报</button>
        </div>
      </div>

      <div v-if="typing" class="typing-indicator">
        <div class="avatar">师</div>
        <div class="bubble typing"><span></span><span></span><span></span></div>
      </div>
    </div>

    <div class="input-bar">
      <button class="btn-voice" :class="{ recording }" @touchstart.prevent="startVoice" @touchend.prevent="stopVoice"
              @mousedown.prevent="startVoice" @mouseup.prevent="stopVoice" title="按住说话">语音</button>
      <input v-model="inputText" type="text" placeholder="输入钓鱼问题..." @keyup.enter="send()" ref="textInput" />
      <button class="btn-send" @click="send()" :disabled="!inputText.trim() && !recording">发送</button>
    </div>

    <div v-if="recording" class="recording-overlay">
      <div class="recording-pulse"></div>
      <p>正在聆听...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '../api/index.js'

const messages = ref([])
const inputText = ref('')
const typing = ref(false)
const recording = ref(false)
const chatArea = ref(null)
const textInput = ref(null)

const topics = [
  '新手用什么鱼竿好？',
  '野钓鲤鱼怎么打窝？',
  '路亚翘嘴用什么饵？',
  '冬天怎么钓鲫鱼？',
  '气压低适合钓鱼吗？',
  '如何判断钓位好坏？'
]

let recognition = null

onMounted(() => {
  // 初始化语音识别
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (SpeechRecognition) {
    recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.interimResults = false
    recognition.continuous = false
    recognition.onresult = (e) => {
      const text = e.results[0][0].transcript
      inputText.value = text
      recording.value = false
      nextTick(() => send())
    }
    recognition.onerror = () => { recording.value = false }
    recognition.onend = () => { recording.value = false }
  }
})

const startVoice = () => {
  if (!recognition) {
    alert('您的浏览器不支持语音识别，请使用Chrome浏览器')
    return
  }
  recording.value = true
  recognition.start()
}

const stopVoice = () => {
  if (recognition && recording.value) {
    recognition.stop()
    recording.value = false
  }
}

const send = (text) => {
  const msg = (text || inputText.value).trim()
  if (!msg || typing.value) return
  inputText.value = ''

  messages.value.push({
    role: 'user',
    text: msg,
    time: formatTime()
  })
  scrollBottom()

  typing.value = true
  request.post('/ai/chat', { message: msg })
    .then(res => {
      typing.value = false
      if (res.code === 200) {
        messages.value.push({
          role: 'master',
          text: res.data.reply,
          time: formatTime()
        })
      } else {
        messages.value.push({
          role: 'master',
          text: res.msg || '抱歉，我暂时无法回答，请稍后重试。',
          time: formatTime()
        })
      }
      scrollBottom()
    })
    .catch(() => {
      typing.value = false
      messages.value.push({
        role: 'master',
        text: '抱歉，我暂时无法回答，请稍后重试。',
        time: formatTime()
      })
      scrollBottom()
    })
}

const speak = (text) => {
  if (!window.speechSynthesis) {
    alert('您的浏览器不支持语音播报')
    return
  }
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 1.0
  utterance.pitch = 1.0
  window.speechSynthesis.speak(utterance)
}

const scrollBottom = () => {
  nextTick(() => {
    if (chatArea.value) {
      chatArea.value.scrollTop = chatArea.value.scrollHeight
    }
  })
}

const formatTime = () => {
  const d = new Date()
  return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: 100vh; background: transparent; }
.header {
  display: flex; align-items: center; padding: 12px 20px;
  background: var(--yy-deep); color: #fff; flex-shrink: 0;
}
.back { color: #fff; text-decoration: none; margin-right: 12px; font-size: 14px; opacity: 0.9; }
.header h2 { margin: 0; flex: 1; font-size: 17px; font-weight: 600; }
.online-badge {
  font-size: 12px; background: rgba(74, 222, 128, 0.25); color: #bbf7d0;
  padding: 3px 10px; border-radius: 999px; border: 1px solid rgba(74, 222, 128, 0.35);
}

.chat-area {
  flex: 1; overflow-y: auto; padding: 16px;
  display: flex; flex-direction: column; gap: 16px;
}

.welcome { text-align: center; padding: 36px 16px; animation: yy-fade-up 0.4s ease; }
.welcome-mark {
  font-family: var(--yy-display); font-size: 42px; color: var(--yy-deep);
  margin-bottom: 10px;
}
.welcome h3 { margin: 0 0 8px; color: var(--yy-ink); }
.welcome p { color: var(--yy-muted); font-size: 14px; margin-bottom: 16px; }
.topics { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.topic-tag {
  padding: 8px 16px; background: var(--yy-surface); border: 1px solid var(--yy-line);
  border-radius: 999px; font-size: 13px; color: var(--yy-primary); cursor: pointer;
}
.topic-tag:hover { background: rgba(18, 122, 138, 0.08); border-color: var(--yy-primary); }

.user, .master { display: flex; gap: 10px; align-items: flex-start; }
.master { flex-direction: row; }
.user { flex-direction: row-reverse; }
.avatar {
  width: 40px; height: 40px; border-radius: 50%; background: rgba(18, 122, 138, 0.12);
  color: var(--yy-primary); font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.user .avatar { background: var(--yy-primary); color: #fff; }
.bubble {
  max-width: 75%; padding: 12px 16px; border-radius: 16px; position: relative;
}
.user .bubble { background: var(--yy-primary); color: #fff; border-bottom-right-radius: 4px; }
.master .bubble {
  background: var(--yy-surface); color: var(--yy-ink);
  border: 1px solid var(--yy-line); border-bottom-left-radius: 4px;
  box-shadow: var(--yy-shadow);
}
.bubble .text { white-space: pre-line; line-height: 1.6; font-size: 14px; }
.bubble .time { font-size: 11px; opacity: 0.6; margin-top: 4px; text-align: right; }
.btn-speak {
  position: absolute; right: -48px; bottom: 4px; background: none;
  border: none; cursor: pointer; font-size: 12px; padding: 4px; color: var(--yy-primary);
}

.typing-indicator { display: flex; gap: 10px; align-items: center; }
.typing-indicator .avatar { align-self: flex-start; }
.bubble.typing { display: flex; gap: 4px; align-items: center; padding: 16px 20px; }
.bubble.typing span {
  width: 8px; height: 8px; border-radius: 50%; background: #999;
  animation: typing 1.4s infinite;
}
.bubble.typing span:nth-child(2) { animation-delay: 0.2s; }
.bubble.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

.input-bar {
  display: flex; gap: 8px; padding: 12px 16px; background: rgba(255,255,255,0.92);
  border-top: 1px solid var(--yy-line); align-items: center; flex-shrink: 0;
}
.input-bar input {
  flex: 1; padding: 12px 14px; border: 1px solid var(--yy-line);
  border-radius: 999px; font-size: 14px; outline: none; background: var(--yy-foam);
}
.input-bar input:focus { border-color: var(--yy-primary); background: #fff; }
.btn-voice {
  height: 44px; padding: 0 14px; border-radius: 999px; border: 1px solid var(--yy-line);
  background: var(--yy-surface); font-size: 13px; cursor: pointer; color: var(--yy-deep);
}
.btn-voice.recording { background: var(--yy-danger); color: #fff; border-color: var(--yy-danger); animation: pulse 1s infinite; }
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(220,38,38,0.4); }
  50% { box-shadow: 0 0 0 12px rgba(220,38,38,0); }
}
.btn-send {
  padding: 12px 20px; background: var(--yy-primary); color: #fff;
  border: none; border-radius: 999px; cursor: pointer; font-size: 14px; font-weight: 600;
}
.btn-send:disabled { opacity: 0.5; cursor: not-allowed; }

.recording-overlay {
  position: fixed; bottom: 100px; left: 50%; transform: translateX(-50%);
  text-align: center; z-index: 100;
}
.recording-pulse {
  width: 60px; height: 60px; border-radius: 50%; background: rgba(255,68,68,0.8);
  margin: 0 auto 8px; animation: pulse 1s infinite;
}
.recording-overlay p { color: #fff; font-size: 14px; text-shadow: 0 1px 2px rgba(0,0,0,0.3); }
</style>
