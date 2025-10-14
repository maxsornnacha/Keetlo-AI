<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <header
    class="sticky top-0 z-50 grid grid-cols-2 md:grid-cols-3 whitespace-nowrap border-b border-solid border-b-[#21262d] bg-[#0D1117]/80 px-4 lg:px-8 py-4 backdrop-blur-sm"
  >
    <a href="/" class="flex items-center gap-2">
      <nuxt-img src="/logo.png" class="h-10 w-10" />
      <h1 class="text-xl font-bold tracking-tight">KEETLO</h1>
    </a>
    <nav
      class="hidden md:flex justify-center items-center gap-6 text-sm font-medium"
    >
      <a 
      class="hover:text-white transition-colors" 
      :class="route.path === '/' ? 'text-white': 'text-gray-300'"
      href="/"
      >
        Home
      </a>
      <a
      class="hover:text-white transition-colors" 
      :class="route.path === '/features' ? 'text-white': 'text-gray-300'"
        href="/features"
        >Features</a
      >
      <a
           class="hover:text-white transition-colors" 
      :class="route.path === '/pricing' ? 'text-white': 'text-gray-300'"
        href="/pricing"
        >Pricing</a
      >
    </nav>
    <div class="flex justify-end gap-4">
    <a v-if="props.user.isLogin === 'yes'" href="/account/projects" class="flex items-center gap-2 border border-slate-50/20 px-2 rounded-md hover:bg-slate-50/20 duration-300 transition">
          <UIcon name="i-lucide-folder-open" class="w-6 h-6 shrink-0 mt-1" />
          <span>Projects</span>
   </a> 
    <UserProfile :user="props.user"/>
    </div>
  </header>
</template>
<script setup lang="ts">
import type { User } from "../types/User";
const props = defineProps<{ user: User }>();
const route = useRoute();
</script>