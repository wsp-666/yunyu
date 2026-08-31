<template>
  <header class="yy-header" :class="{ open: menuOpen }">
    <div class="yy-header-inner">
      <router-link to="/" class="brand" @click="menuOpen = false">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 40 40" width="28" height="28">
            <circle cx="20" cy="20" r="18" fill="currentColor" opacity="0.12" />
            <path d="M8 22c4-8 12-12 20-8-3 6-8 10-14 11-2 .3-4-.7-6-3z" fill="currentColor" />
            <circle cx="27" cy="14" r="2.2" fill="var(--yy-accent)" />
          </svg>
        </span>
        <span class="brand-text">云渔</span>
      </router-link>

      <div class="search" v-if="showSearch">
        <input
          :value="keyword"
          type="search"
          placeholder="搜索钓场、水域..."
          @input="$emit('update:keyword', $event.target.value)"
          @keyup.enter="$emit('search')"
        />
        <button type="button" @click="$emit('search')">搜索</button>
      </div>

      <button class="menu-toggle" type="button" aria-label="菜单" @click="menuOpen = !menuOpen">
        <span></span><span></span><span></span>
      </button>

      <nav class="nav" @click="menuOpen = false">
        <div class="nav-group">
          <router-link to="/">钓场</router-link>
          <router-link to="/spot-share">钓点</router-link>
          <router-link to="/community">社区</router-link>
          <router-link to="/shop">商城</router-link>
        </div>
        <div class="nav-group muted">
          <router-link to="/ai/identify">识鱼</router-link>
          <router-link to="/ai-chat">AI大师</router-link>
          <router-link to="/voice-call">语音</router-link>
          <router-link to="/weather">天气</router-link>
          <router-link to="/equip">装备</router-link>
          <router-link to="/points">积分</router-link>
        </div>
        <div class="nav-user">
          <router-link v-if="!isLogin" to="/login" class="login-link">登录</router-link>
          <div v-else class="user-info" @click.stop="showMenu = !showMenu">
            <span class="user-tag" :class="userInfo?.role === 1 ? 'owner' : 'normal'">
              {{ userInfo?.role === 1 ? '塘主' : '用户' }}
            </span>
            <span class="nickname">{{ userInfo?.nickname || '用户' }}</span>
            <div v-if="showMenu" class="dropdown">
              <router-link to="/order">我的订单</router-link>
              <router-link v-if="userInfo?.role === 1" to="/owner">塘主后台</router-link>
              <button type="button" class="logout" @click="logout">退出登录</button>
            </div>
          </div>
        </div>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

defineProps({
  keyword: { type: String, default: '' },
  showSearch: { type: Boolean, default: true }
})

defineEmits(['update:keyword', 'search'])

const menuOpen = ref(false)
const showMenu = ref(false)

const isLogin = computed(() => !!localStorage.getItem('token'))
const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  window.location.href = '/'
}

const onDocClick = () => { showMenu.value = false }
onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.yy-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(14px);
  background: rgba(244, 250, 251, 0.86);
  border-bottom: 1px solid var(--yy-line);
}

.yy-header-inner {
  max-width: 1240px;
  margin: 0 auto;
  min-height: var(--yy-header-h);
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  color: var(--yy-deep);
}

.brand-mark {
  color: var(--yy-primary);
  display: grid;
  place-items: center;
}

.brand-text {
  font-family: var(--yy-display);
  font-size: 28px;
  letter-spacing: 0.04em;
  line-height: 1;
}

.search {
  flex: 1;
  max-width: 360px;
  display: flex;
  background: var(--yy-surface);
  border: 1px solid var(--yy-line);
  border-radius: 999px;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(11, 42, 50, 0.04);
}

.search input {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 16px;
  background: transparent;
  font-size: 14px;
}

.search button {
  border: none;
  background: var(--yy-primary);
  color: #fff;
  padding: 0 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.search button:hover {
  background: var(--yy-primary-hover);
}

.menu-toggle {
  display: none;
  width: 42px;
  height: 42px;
  border: 1px solid var(--yy-line);
  border-radius: 12px;
  background: var(--yy-surface);
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  margin-left: auto;
  cursor: pointer;
}

.menu-toggle span {
  width: 18px;
  height: 2px;
  background: var(--yy-deep);
  border-radius: 2px;
}

.nav {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 18px;
}

.nav-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-group a {
  padding: 8px 10px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--yy-ink);
  transition: background 0.2s, color 0.2s;
}

.nav-group a:hover,
.nav-group a.router-link-active {
  background: rgba(18, 122, 138, 0.1);
  color: var(--yy-primary);
}

.nav-group.muted a {
  color: var(--yy-muted);
  font-size: 12px;
}

.login-link {
  padding: 8px 16px;
  border-radius: 999px;
  background: var(--yy-primary);
  color: #fff !important;
  font-size: 13px;
  font-weight: 600;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(18, 122, 138, 0.08);
}

.nickname {
  font-size: 13px;
  color: var(--yy-deep);
  max-width: 88px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  color: #fff;
  font-weight: 600;
}

.user-tag.normal { background: var(--yy-primary); }
.user-tag.owner { background: var(--yy-accent); }

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 140px;
  background: var(--yy-surface);
  border: 1px solid var(--yy-line);
  border-radius: 12px;
  box-shadow: var(--yy-shadow);
  padding: 6px;
  z-index: 20;
  animation: yy-fade-up 0.18s ease;
}

.dropdown a,
.dropdown .logout {
  display: block;
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 13px;
  color: var(--yy-ink);
  cursor: pointer;
}

.dropdown a:hover,
.dropdown .logout:hover {
  background: var(--yy-mist);
}

.logout {
  color: var(--yy-danger) !important;
}

@media (max-width: 980px) {
  .search { max-width: 220px; }
  .nav-group.muted { display: none; }
}

@media (max-width: 760px) {
  .menu-toggle { display: flex; }
  .search {
    order: 3;
    flex: 1 1 100%;
    max-width: none;
  }
  .yy-header-inner { flex-wrap: wrap; }
  .nav {
    display: none;
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 8px 0 12px;
  }
  .yy-header.open .nav { display: flex; }
  .nav-group,
  .nav-group.muted {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }
  .nav-group a {
    text-align: center;
    background: rgba(255, 255, 255, 0.7);
  }
  .nav-user { display: flex; justify-content: center; }
}
</style>
