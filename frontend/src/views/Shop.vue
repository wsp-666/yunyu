<template>
  <div class="shop-page">
    <PageBar title="渔具商城">
      <span class="my-points">积分 {{ myPoints }}</span>
      <router-link to="/points" class="points-link">积分中心</router-link>
    </PageBar>

    <div class="category-tabs">
      <button v-for="cat in categories" :key="cat.value"
              :class="{ active: currentCategory === cat.value }"
              @click="currentCategory = cat.value; fetchProducts()">
        {{ cat.label }}
      </button>
    </div>

    <main class="main">
      <div v-if="loading" class="loading">加载中...</div>

      <div v-else class="product-grid">
        <div v-for="product in productList" :key="product.id" class="product-card"
             @click="openDetail(product)">
          <div class="product-image">
            <img :src="getFirstImage(product.images) || '/placeholder.jpg'" :alt="product.name" />
            <span v-if="product.salesCount > 50" class="badge-hot">热销</span>
          </div>
          <div class="product-info">
            <h4>{{ product.name }}</h4>
            <p class="product-brand" v-if="product.brand">{{ product.brand }}</p>
            <div class="product-bottom">
              <div class="price-section">
                <span class="price">¥{{ product.price }}</span>
                <span class="points-price">{{ product.pointsPrice }}积分</span>
              </div>
              <span class="sales">已售{{ product.salesCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && productList.length === 0" class="empty">暂无商品</div>

      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page <= 1" @click="page--; fetchProducts()">上一页</button>
        <span>{{ page }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click="page++; fetchProducts()">下一页</button>
      </div>
    </main>

    <div v-if="detailProduct" class="modal-overlay" @click.self="detailProduct = null">
      <div class="modal-card">
        <button class="modal-close" @click="detailProduct = null">✕</button>
        <div class="modal-image">
          <img :src="getFirstImage(detailProduct.images) || '/placeholder.jpg'" :alt="detailProduct.name" />
        </div>
        <div class="modal-body">
          <h3>{{ detailProduct.name }}</h3>
          <p class="modal-brand" v-if="detailProduct.brand">品牌: {{ detailProduct.brand }}</p>
          <p class="modal-desc" v-if="detailProduct.description">{{ detailProduct.description }}</p>
          <div class="modal-prices">
            <span class="modal-price">¥{{ detailProduct.price }}</span>
            <span class="modal-points">{{ detailProduct.pointsPrice }}积分可兑换</span>
          </div>
          <p class="modal-stock">库存: {{ detailProduct.stock }} | 已售: {{ detailProduct.salesCount }}</p>
          <div class="modal-actions">
            <button class="btn-points" :disabled="myPoints < detailProduct.pointsPrice || detailProduct.stock <= 0"
                    @click="exchangeProduct(detailProduct)">
              {{ myPoints < detailProduct.pointsPrice ? '积分不足' : '积分兑换' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/index.js'
import PageBar from '../components/PageBar.vue'

const productList = ref([])
const myPoints = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const totalCount = ref(0)
const currentCategory = ref(null)
const detailProduct = ref(null)

const categories = [
  { label: '全部分类', value: null },
  { label: '鱼竿', value: 1 },
  { label: '渔轮', value: 2 },
  { label: '鱼线', value: 3 },
  { label: '浮漂', value: 4 },
  { label: '鱼饵', value: 5 },
  { label: '配件', value: 6 }
]

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

const getFirstImage = (images) => {
  if (!images) return null
  return images.split(',')[0]
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (currentCategory.value) params.category = currentCategory.value
    const res = await request.get('/product/list', { params })
    if (res.code === 200) {
      productList.value = res.data.list || []
      totalCount.value = res.data.totalCount || 0
    }
  } catch (e) {
    console.error('获取商品列表失败', e)
  } finally {
    loading.value = false
  }
}

const fetchMyPoints = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const res = await request.get('/points/detail', { params: { page: 1, size: 1 } })
    if (res.code === 200) {
      myPoints.value = res.data.totalPoints || 0
    }
  } catch (e) {
    // not logged in or error, ignore
  }
}

const openDetail = (product) => {
  detailProduct.value = product
}

const exchangeProduct = async (product) => {
  if (!confirm(`确认使用 ${product.pointsPrice} 积分兑换「${product.name}」？`)) return
  try {
    const res = await request.post('/points/exchange', null, { params: { productId: product.id } })
    if (res.code === 200) {
      alert('兑换成功！')
      myPoints.value -= product.pointsPrice
      product.stock -= 1
      product.salesCount += 1
      detailProduct.value = { ...product }
    } else {
      alert(res.msg || '兑换失败')
    }
  } catch (e) {
    alert('兑换失败')
  }
}

onMounted(() => {
  fetchProducts()
  fetchMyPoints()
})
</script>

<style scoped>
.shop-page { min-height: 100vh; }
.my-points { font-size: 13px; color: var(--yy-accent); font-weight: 700; }
.points-link { color: var(--yy-primary); font-size: 13px; font-weight: 600; }

.category-tabs {
  display: flex; gap: 8px; padding: 14px 20px;
  border-bottom: 1px solid var(--yy-line); flex-wrap: wrap;
  background: rgba(255,255,255,0.65);
}
.category-tabs button {
  padding: 8px 16px; border: 1px solid var(--yy-line); background: var(--yy-surface);
  border-radius: 999px; cursor: pointer; font-size: 13px; color: var(--yy-muted);
  transition: all 0.2s;
}
.category-tabs button:hover { border-color: var(--yy-primary); color: var(--yy-primary); }
.category-tabs button.active {
  background: var(--yy-primary); color: #fff; border-color: var(--yy-primary);
}

.main { max-width: 1200px; margin: 24px auto; padding: 0 16px; }
.loading, .empty { text-align: center; padding: 60px; color: var(--yy-muted); }

.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px;
}
.product-card {
  background: var(--yy-surface); border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius); overflow: hidden;
  box-shadow: var(--yy-shadow); cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover { transform: translateY(-4px); box-shadow: var(--yy-shadow-lg); }
.product-image { position: relative; height: 180px; overflow: hidden; background: #f0f0f0; }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.badge-hot {
  position: absolute; top: 8px; right: 8px; background: #ff4444;
  color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 11px;
}
.product-info { padding: 14px; }
.product-info h4 { margin: 0 0 4px; font-size: 15px; color: #333; }
.product-brand { color: #999; font-size: 12px; margin: 0 0 8px; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; }
.price-section { display: flex; align-items: baseline; gap: 8px; }
.price { color: #ff4444; font-size: 18px; font-weight: bold; }
.points-price { color: #ff9800; font-size: 12px; }
.sales { color: #999; font-size: 12px; }

.pagination {
  display: flex; justify-content: center; align-items: center;
  gap: 16px; margin-top: 32px; padding: 16px;
}
.pagination button {
  padding: 8px 16px; border: 1px solid #ddd; background: #fff;
  border-radius: 4px; cursor: pointer;
}
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center; z-index: 200;
}
.modal-card {
  background: #fff; border-radius: 12px; max-width: 500px; width: 90%;
  max-height: 80vh; overflow-y: auto; position: relative;
}
.modal-close {
  position: absolute; top: 12px; right: 12px; background: rgba(0,0,0,0.4);
  color: #fff; border: none; width: 28px; height: 28px; border-radius: 50%;
  font-size: 14px; cursor: pointer; z-index: 1;
}
.modal-image { height: 260px; overflow: hidden; background: #f0f0f0; }
.modal-image img { width: 100%; height: 100%; object-fit: cover; }
.modal-body { padding: 24px; }
.modal-body h3 { margin: 0 0 8px; font-size: 20px; color: #333; }
.modal-brand { color: #666; font-size: 14px; margin: 0 0 8px; }
.modal-desc { color: #666; font-size: 14px; line-height: 1.6; margin: 0 0 16px; }
.modal-prices { display: flex; align-items: baseline; gap: 12px; margin-bottom: 8px; }
.modal-price { color: #ff4444; font-size: 24px; font-weight: bold; }
.modal-points { color: #ff9800; font-size: 14px; }
.modal-stock { color: #999; font-size: 13px; margin: 0 0 20px; }
.modal-actions { display: flex; gap: 12px; }
.btn-points {
  flex: 1; padding: 12px; background: #ff9800; color: #fff;
  border: none; border-radius: 8px; font-size: 16px; cursor: pointer;
}
.btn-points:disabled { background: #ccc; cursor: not-allowed; }
</style>
