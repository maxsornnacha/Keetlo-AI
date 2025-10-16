<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="sticky top-0 z-50 border-b border-b-[#21262d] bg-[#0D1117]/80 backdrop-blur-sm">
  <header
    class="mx-auto max-w-[1400px] grid grid-cols-2 md:grid-cols-3 whitespace-nowrap px-4 lg:px-8 py-4"
  >
    <!-- Left: Logo + mobile menu -->
    <div class="flex items-center gap-2">
      <button
        class="lg:hidden hover:bg-slate-50/20 pt-1 px-1 rounded-md cursor-pointer"
        aria-label="Open menu"
        @click="onOpenMobileSidebar"
      >
        <UIcon name="i-lucide-menu" class="size-6" />
      </button>

      <NuxtLink to="/" class="flex items-center gap-0 focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded-md">
        <nuxt-img src="/logo.png" class="h-9 w-9" />
        <h1 class="text-xl font-bold tracking-tight">EETLO</h1>
      </NuxtLink>
    </div>

    <!-- Center: Desktop nav -->
    <nav class="hidden md:flex justify-center items-center gap-6 text-sm font-medium">
      <NuxtLink
        to="/"
        class="hover:text-white transition-colors"
        :class="route.path === '/' ? 'text-white' : 'text-gray-300'"
      >Home</NuxtLink>

      <NuxtLink
        to="/features"
        class="hover:text-white transition-colors"
        :class="route.path === '/features' ? 'text-white' : 'text-gray-300'"
      >Features</NuxtLink>

      <NuxtLink
        to="/pricing"
        class="hover:text-white transition-colors"
        :class="route.path === '/pricing' ? 'text-white' : 'text-gray-300'"
      >Pricing</NuxtLink>
    </nav>

    <!-- Right: Actions -->
    <div class="flex justify-end gap-4">
      <NuxtLink
        v-if="props.user.isLogin === 'yes'"
        to="/account/projects"
        class="flex items-center gap-2 border border-slate-50/20 px-2 rounded-md hover:bg-slate-50/20 duration-300 transition"
      >
        <UIcon name="i-lucide-folder-open" class="w-6 h-6 shrink-0 mt-1" />
        <span>Projects</span>
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
          class="h-screen bg-[#0D1117] opacity-90 border border-slate-50/20 fixed inset-y-0 left-0 z-50 transform transition-transform duration-300 w-72"
          :class="mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
          @keydown.esc="onCloseMobileSidebar"
        >
          <div class="bg-[#0D1117] border-b border-[#30363d] flex justify-between items-center p-3">
            <h2 class="text-base font-semibold">Menu</h2>
            <button
              class="pt-1 px-1 hover:bg-slate-50/20 border border-slate-50/20 rounded-lg cursor-pointer"
              aria-label="Close menu"
              @click="onCloseMobileSidebar"
            >
              <UIcon name="i-lucide-x" class="size-6" />
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
