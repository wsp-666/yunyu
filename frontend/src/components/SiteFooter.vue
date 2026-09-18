<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="name">云渔</span>
        <p>找钓场 · 看放鱼 · 问大师</p>
      </div>
      <div class="footer-cols">
        <div>
          <h4>发现</h4>
          <router-link to="/">钓场大厅</router-link>
          <router-link to="/spot-share">钓点分享</router-link>
          <router-link to="/community">渔获社区</router-link>
        </div>
        <div>
          <h4>工具</h4>
          <router-link to="/weather">出钓天气</router-link>
          <router-link to="/ai-chat">AI 大师</router-link>
          <router-link to="/equip">装备推荐</router-link>
        </div>
        <div>
          <h4>服务</h4>
          <router-link to="/shop">渔具商城</router-link>
          <router-link to="/points">积分中心</router-link>
          <router-link to="/login">登录 / 注册</router-link>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <span>© {{ year }} 云渔 YunYu</span>
      <span class="sep">·</span>
      <span>Vue 3 + Spring Boot · Redis 缓存</span>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.site-footer {
  margin-top: 64px;
  background: linear-gradient(180deg, rgba(11, 42, 50, 0.96), #071c22);
  color: rgba(255, 255, 255, 0.88);
}

.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 48px 20px 28px;
  display: grid;
  grid-template-columns: 1.1fr 2fr;
  gap: 40px;
}

.footer-brand .name {
  font-family: var(--yy-display);
  font-size: 36px;
  display: block;
  margin-bottom: 8px;
  color: #fff;
}

.footer-brand p {
  color: rgba(255, 255, 255, 0.65);
  font-size: 14px;
}

.footer-cols {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.footer-cols h4 {
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
  margin-bottom: 14px;
  font-weight: 600;
}

.footer-cols a {
  display: block;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  margin-bottom: 10px;
  transition: color 0.2s;
}

.footer-cols a:hover {
  color: #fbbf24;
}

.footer-bottom {
  max-width: 1240px;
  margin: 0 auto;
  padding: 16px 20px 28px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}

.sep { opacity: 0.5; }

@media (max-width: 760px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: 28px;
    padding-top: 36px;
  }
  .footer-cols {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
