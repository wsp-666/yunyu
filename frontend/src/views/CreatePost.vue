<template>
  <div class="create-post">
    <header class="header">
      <router-link to="/community" class="back">← 返回</router-link>
      <h2>发布帖子</h2>
      <button class="btn-submit" @click="submitPost" :disabled="submitting">
        {{ submitting ? '发布中...' : '发布' }}
      </button>
    </header>

    <div class="form">
      <div class="form-group">
        <label>类型</label>
        <select v-model="form.type">
          <option :value="1">晒渔获</option>
          <option :value="2">聊装备</option>
          <option :value="3">问钓法</option>
          <option :value="4">约比赛</option>
          <option :value="5">求助</option>
        </select>
      </div>
      <div class="form-group">
        <label>标题</label>
        <input v-model="form.title" placeholder="请输入标题" />
      </div>
      <div class="form-group">
        <label>正文</label>
        <textarea v-model="form.content" placeholder="请输入内容..." rows="6"></textarea>
      </div>
      <div class="form-group">
        <label>话题标签</label>
        <input v-model="form.topicTag" placeholder="例如: #野钓 #路亚" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index.js'

const router = useRouter()
const submitting = ref(false)
const form = ref({
  type: 1,
  title: '',
  content: '',
  topicTag: ''
})

const submitPost = async () => {
  if (!form.value.content) {
    alert('请输入内容')
    return
  }
  submitting.value = true
  try {
    const res = await request.post('/post/create', form.value)
    if (res.code === 200) {
      alert('发布成功')
      router.push('/community')
    } else {
      alert(res.msg || '发布失败')
    }
  } catch (e) {
    alert('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-post { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; align-items: center; padding: 12px 24px;
  background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.back { color: #1a73e8; text-decoration: none; margin-right: 16px; }
.header h2 { margin: 0; flex: 1; }
.btn-submit {
  padding: 8px 24px; background: #1a73e8; color: #fff;
  border: none; border-radius: 6px; cursor: pointer;
}
.btn-submit:disabled { opacity: 0.6; }
.form { max-width: 600px; margin: 24px auto; padding: 0 16px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 6px; color: #555; font-size: 14px; }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 10px 12px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box;
}
.form-group textarea { resize: vertical; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  border-color: #1a73e8;
}
</style>
