<template>
  <div class="register-page">
    <div class="backdrop" aria-hidden="true"></div>
    <div class="register-card">
      <router-link to="/" class="brand">云渔</router-link>
      <h2>创建账号</h2>
      <p class="sub">加入钓友圈，收藏钓场与渔获</p>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>账号</label>
          <input v-model="account" type="text" placeholder="请输入账号" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <div class="form-group">
          <label>昵称</label>
          <input v-model="nickname" type="text" placeholder="请输入昵称" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="phone" type="text" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>选择身份</label>
          <div class="role-selector">
            <button type="button" class="role-option" :class="{ active: role === 0 }" @click="role = 0">
              <span class="role-name">普通用户</span>
              <span class="role-desc">浏览钓场、预订场次</span>
            </button>
            <button type="button" class="role-option" :class="{ active: role === 1 }" @click="role = 1">
              <span class="role-name">塘主</span>
              <span class="role-desc">管理钓场、发布场次</span>
            </button>
          </div>
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="yy-btn btn-full" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="login-link">已有账号？<router-link to="/login">立即登录</router-link></p>
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
const nickname = ref('')
const phone = ref('')
const role = ref(0)
const errorMsg = ref('')
const loading = ref(false)

const handleRegister = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await request.post('/user/register', {
      account: account.value,
      password: password.value,
      nickname: nickname.value,
      phone: phone.value,
      role: role.value
    })
    if (res.code === 200) {
      alert('注册成功，请登录')
      router.push('/login')
    } else {
      errorMsg.value = res.msg || '注册失败'
    }
  } catch (e) {
    if (e.response?.data?.msg) {
      errorMsg.value = e.response.data.msg
    } else if (e.response) {
      errorMsg.value = '服务器错误(' + e.response.status + ')'
    } else {
      errorMsg.value = '网络错误，请检查后端是否启动'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  position: relative;
}

.backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(700px 320px at 80% 10%, rgba(18, 122, 138, 0.3), transparent 60%),
    linear-gradient(145deg, #0b2a32 0%, #0c4a56 50%, #127a8a 100%);
}

.register-card {
  position: relative;
  z-index: 1;
  width: min(440px, 100%);
  background: rgba(255, 255, 255, 0.97);
  padding: 36px 32px;
  border-radius: 24px;
  box-shadow: var(--yy-shadow-lg);
  animation: yy-fade-up 0.45s ease;
}

.brand {
  font-family: var(--yy-display);
  font-size: 34px;
  color: var(--yy-deep);
  display: inline-block;
  margin-bottom: 8px;
}

.register-card h2 {
  font-size: 24px;
  color: var(--yy-deep);
  margin-bottom: 4px;
}

.sub {
  color: var(--yy-muted);
  font-size: 13px;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--yy-muted);
  font-size: 13px;
}

.form-group input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--yy-line);
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: var(--yy-foam);
}

.form-group input:focus {
  border-color: var(--yy-primary);
  box-shadow: 0 0 0 3px rgba(18, 122, 138, 0.15);
  background: #fff;
}

.role-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.role-option {
  padding: 12px 10px;
  border: 1.5px solid var(--yy-line);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: border-color 0.2s, background 0.2s;
}

.role-option.active {
  border-color: var(--yy-primary);
  background: rgba(18, 122, 138, 0.08);
}

.role-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--yy-ink);
}

.role-desc {
  font-size: 11px;
  color: var(--yy-muted);
}

.btn-full {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
}

.error {
  color: var(--yy-danger);
  font-size: 13px;
  margin-bottom: 12px;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--yy-muted);
}

.login-link a {
  color: var(--yy-primary);
  font-weight: 600;
}
</style>
