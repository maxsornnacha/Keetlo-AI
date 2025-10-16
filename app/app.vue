<template>
  <div v-if="!loadingAuthAccessibility">
    <div v-if="layout1">
      <Header :user="user" />
      <div class="min-h-[70vh]">
        <NuxtPage :user="user" />
      </div>
      <Footer />
    </div>

    <div v-else-if="layout2">
      <Header :user="user" />
      <div class="min-h-[70vh]">
        <NuxtPage :user="user" />
      </div>
    </div>

    <div v-else-if="layout3">
      <NuxtPage :user="user" />
    </div>

    <div v-else>
      <Header :user="user" />
      <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-16 min-h-[70vh]">
        <NuxtPage :user="user" />
      </div>
      <Footer />
    </div>

    <ModalRoot/>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '#imports'
import { getToken } from '@/utils/auth'
import type { User } from './types/User'
import ModalRoot from './components/modal/ModalRoot.vue'
import Header from '~/components/header.vue'
import Footer from '~/components/footer.vue'

const route = useRoute()

/** 1) Make the layout flags reactive (computed from route).
 *    These update automatically on navigation.
 */
const layout1 = computed(() =>
  route.path === '/' 
  || route.path.includes('/account/favorites')
  || route.path.includes('/account/payment')
)
const layout2 = computed(() =>
  route.fullPath.includes('/auth')
)
const layout3 = computed(() =>
  route.path.includes('/account/projects') ||
  route.path.includes('/account/billing/receipt') ||
  route.path === '/oauth/loading' ||
  route.path === '/payment/success' ||
  route.path === '/payment/cancel' ||
  route.path.includes('/public/projects/preview/')
)

/** 2) Source of truth: the store. Avoid copying state long-term. */
const userStore = useUserStore()
const user = ref<User>({
  email: '',
  firstname: '',
  lastname: '',
  avatarUrl: null,
  isLogin: 'loading',
  limitRequests: 100,
  leftRequests: 100,
  subscriptionPlanName: '',
  isGithubConnected: 0,
  isGoogleConnected: 0,
})

const loadingAuthAccessibility = ref(true)

const syncUser = () => {
  const u = userStore.user
  if (u) {
    user.value = {
      email: u.email,
      firstname: u.firstname,
      lastname: u.lastname,
      avatarUrl: u.avatarUrl,
      limitRequests: u.limitRequests,
      leftRequests: u.leftRequests,
      subscriptionPlanName: u.subscriptionPlanName,
      isGithubConnected: u.isGithubConnected,
      isGoogleConnected: u.isGoogleConnected,
      isLogin: 'yes',
    }
  } else {
    user.value.isLogin = 'no'
  }
}

/** 3) Initialize user once; do redirects here (client) using navigateTo. */
onMounted(async () => {
  await userStore.initializeUser()
  syncUser()

  // Redirects – same logic as yours, but SPA-friendly
  const token = getToken()
  if (token && (route.path === '/auth/login' || route.path === '/auth/register')) {
    await navigateTo('/', { replace: true })
  } else if (!token && route.path.startsWith('/account')) {
    await navigateTo(`/auth/login?next=${encodeURIComponent(route.fullPath)}`, { replace: true })
  }

  loadingAuthAccessibility.value = false
})

/** 4) Keep user in sync if store changes later. */
watch(() => userStore.user, syncUser, { deep: true })
</script>
