import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/venue/:id',
    name: 'VenueDetail',
    component: () => import('../views/VenueDetail.vue')
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/Community.vue')
  },
  {
    path: '/post/create',
    name: 'CreatePost',
    component: () => import('../views/CreatePost.vue')
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('../views/PostDetail.vue')
  },
  {
    path: '/points',
    name: 'Points',
    component: () => import('../views/Points.vue')
  },
  {
    path: '/ai/identify',
    name: 'AIIndentify',
    component: () => import('../views/AIIndentify.vue')
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: () => import('../views/AiChat.vue')
  },
  {
    path: '/spot-share',
    name: 'SpotShare',
    component: () => import('../views/SpotShare.vue')
  },
  {
    path: '/weather',
    name: 'Weather',
    component: () => import('../views/Weather.vue')
  },
  {
    path: '/equip',
    name: 'EquipRecommend',
    component: () => import('../views/EquipRecommend.vue')
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('../views/Order.vue')
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('../views/Shop.vue')
  },
  {
    path: '/owner',
    name: 'PondOwner',
    component: () => import('../views/PondOwner.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue')
  },
  {
    path: '/voice-call',
    name: 'VoiceCall',
    component: () => import('../views/VoiceCall.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
