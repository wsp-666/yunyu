<template>
  <div class="login-page">
    <div class="backdrop" aria-hidden="true"></div>
    <div class="login-shell">
      <aside class="brand-panel">
        <router-link to="/" class="brand">云渔</router-link>
        <p class="tagline">找钓场 · 看放鱼 · 问大师</p>
        <ul>
          <li>实时钓场与放鱼动态</li>
          <li>社区渔获与装备交流</li>
          <li>AI 识鱼与语音指导</li>
        </ul>
      </aside>
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="sub">登录后同步订单、积分与关注</p>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>账号</label>
            <input v-model="account" type="text" placeholder="请输入账号" required autocomplete="username" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="password" type="password" placeholder="请输入密码" required autocomplete="current-password" />
          </div>
          <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
          <button type="submit" class="yy-btn btn-full" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        <p class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index.js'

const router = useRouter()
const account = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

const handleLogin = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await request.post('/user/login', {
      account: account.value,
      password: password.value
    })
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      if (res.data.userType === 'admin') {
        localStorage.setItem('adminToken', res.data.token)
        router.push('/admin')
      } else if (res.data.role === 1) {
        router.push('/owner')
      } else {
        router.push('/')
      }
    } else {
      errorMsg.value = res.msg || '登录失败'
    }
  } catch (e) {
    errorMsg.value = e.response?.data?.msg || '网络错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(700px 320px at 15% 20%, rgba(18, 122, 138, 0.35), transparent 60%),
    radial-gradient(600px 280px at 85% 80%, rgba(217, 119, 6, 0.18), transparent 55%),
    linear-gradient(145deg, #0b2a32 0%, #0c4a56 48%, #127a8a 100%);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(920px, 100%);
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--yy-shadow-lg);
  animation: yy-fade-up 0.45s ease;
}

.brand-panel {
  padding: 40px 36px;
  background:
    linear-gradient(160deg, rgba(12, 74, 86, 0.96), rgba(18, 122, 138, 0.9)),
    radial-gradient(circle at 80% 20%, rgba(251, 191, 36, 0.35), transparent 40%);
  color: #fff;
}

.brand {
  font-family: var(--yy-display);
  font-size: 42px;
  color: #fff;
  display: inline-block;
  margin-bottom: 8px;
}

.tagline {
  opacity: 0.88;
  margin-bottom: 28px;
  font-size: 14px;
}

.brand-panel ul {
  list-style: none;
  display: grid;
  gap: 12px;
}

.brand-panel li {
  padding-left: 16px;
  position: relative;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.92;
}

.brand-panel li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.55em;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #fbbf24;
}

.login-card {
  padding: 40px 36px;
}

.login-card h2 {
  font-size: 26px;
  color: var(--yy-deep);
  margin-bottom: 6px;
}

.sub {
  color: var(--yy-muted);
  font-size: 13px;
  margin-bottom: 28px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--yy-muted);
  font-size: 13px;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--yy-line);
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: var(--yy-foam);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus {
  border-color: var(--yy-primary);
  box-shadow: 0 0 0 3px rgba(18, 122, 138, 0.15);
  background: #fff;
}

.btn-full {
  width: 100%;
  margin-top: 4px;
  padding: 12px;
  border-radius: 12px;
}

.error {
  color: var(--yy-danger);
  font-size: 13px;
  margin-bottom: 12px;
}

.register-link {
  text-align: center;
  margin-top: 18px;
  font-size: 14px;
  color: var(--yy-muted);
}

.register-link a {
  color: var(--yy-primary);
  font-weight: 600;
}

@media (max-width: 720px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    padding: 28px 24px 20px;
  }
  .brand-panel ul {
    display: none;
  }
  .login-card {
    padding: 28px 24px 32px;
  }
}
</style>
