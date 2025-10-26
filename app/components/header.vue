<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="sticky top-0 z-50 backdrop-blur-sm">
  <header
    class="mx-auto max-w-[1400px] grid grid-cols-2 whitespace-nowrap px-4 lg:px-8 py-2"
  >
    <!-- Left: Logo + mobile menu -->
     <div class="flex gap-8 items-center">
    <div class="flex items-center gap-2">
      <button
        class="lg:hidden hover:bg-gray-200 pt-1 px-1 rounded-md cursor-pointer"
        aria-label="Open menu"
        @click="onOpenMobileSidebar"
      >
        <UIcon name="i-lucide-menu" class="size-6" />
      </button>

      <NuxtLink to="/" class="flex items-center gap-0">
        <nuxt-img src="/icon-256.png" class="h-5 w-5" />
        <h1 class="text-xl font-bold tracking-tight">EETLO</h1>
      </NuxtLink>
    </div>
        <nav class="hidden md:flex justify-center items-center gap-6 text-sm font-medium">
      <NuxtLink
        to="/"
        class="hover:text-gray-400 transition-colors"
        :class="route.path === '/' ? 'text-gray-500' : 'text-black'"
      >Home</NuxtLink>

      <NuxtLink
        to="/features"
        class="hover:text-gray-400 transition-colors"
        :class="route.path === '/features' ? 'text-gray-500' : 'text-black'"
      >Features</NuxtLink>

      <NuxtLink
        to="/pricing"
        class="hover:text-gray-400 transition-colors"
        :class="route.path === '/pricing' ? 'text-gray-500' : 'text-black'"
      >Pricing</NuxtLink>

      <NuxtLink
        to="/public/projects"
        class="hover:text-gray-400 transition-colors"
        :class="route.path.includes('/public/projects') ? 'text-gray-500' : 'text-black'"
      >Public Projects</NuxtLink>
    </nav>
    </div>

    <!-- Right: Actions -->
    <div class="flex items-center justify-end gap-2">
      <NuxtLink
        v-if="props.user.isLogin === 'yes'"
        v-tooltip="'My Projects'"
         to="/account/projects"
        class="flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
      >
        <UIcon name="i-lucide-folder-open" class="size-6" />
      </NuxtLink>

      <UserProfile :user="props.user" />

      <!-- Drawer -->
      <Teleport to="body">
        <div
          v-if="mobileSidebarOpen"
          class="fixed inset-0 bg-black/70 z-40"
          @click="onCloseMobileSidebar"
        />
        <aside
          class="h-screen fixed inset-y-0 left-0 z-50 transform transition-transform duration-300 min-w-72 bg-gray-50"
          :class="mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
          @keydown.esc="onCloseMobileSidebar"
        >
          <div class="border-b border-gray-200 flex justify-between items-center px-3 py-2">
            <h2 class="text-base font-semibold">Menu</h2>
            <button
              class="pt-1 px-1 hover:bg-gray-200 rounded-lg cursor-pointer"
              aria-label="Close menu"
              @click="onCloseMobileSidebar"
            >
              <UIcon name="i-lucide-x" class="size-5" />
            </button>
          </div>

          <HeaderSidebar @navigate="onCloseMobileSidebar" />
        </aside>
      </Teleport>
    </div>
  </header>
  </div>
</template>

<script setup lang="ts">
import type { User } from '../types/User'
import HeaderSidebar from './headerSidebar.vue'

const props = defineProps<{ user: User }>()
const route = useRoute()
const mobileSidebarOpen = ref(false)

const onOpenMobileSidebar = () => { mobileSidebarOpen.value = true }
const onCloseMobileSidebar = () => { mobileSidebarOpen.value = false }

// Auto-close when route changes (redirects included)
watch(() => route.fullPath, () => {
  mobileSidebarOpen.value = false
})

// ESC to close (SSR-safe)
function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') mobileSidebarOpen.value = false
}

if (import.meta.client) {
  onMounted(() => {
    window.addEventListener('keydown', onKey)
  })
  onBeforeUnmount(() => {
    window.removeEventListener('keydown', onKey)
  })
}
</script>
