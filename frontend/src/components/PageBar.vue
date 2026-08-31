<template>
  <header class="page-bar">
    <router-link :to="backTo" class="back">← {{ backLabel }}</router-link>
    <h1>{{ title }}</h1>
    <div class="actions">
      <slot />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  backTo: { type: String, default: '/' },
  backLabel: { type: String, default: '首页' }
})
</script>

<style scoped>
.page-bar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 56px;
  padding: 10px 20px;
  backdrop-filter: blur(12px);
  background: rgba(244, 250, 251, 0.9);
  border-bottom: 1px solid var(--yy-line);
}

.back {
  color: var(--yy-primary);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.page-bar h1 {
  flex: 1;
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--yy-deep);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
