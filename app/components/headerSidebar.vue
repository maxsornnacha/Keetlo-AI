<!-- components/headerSidebar.vue -->
<template>
  <aside
    class="overflow-y-auto backdrop-blur-sm flex flex-col w-72 border-r border-[#30363d] bg-[#0D1117]/70 h-screen"
    role="dialog"
    aria-modal="true"
  >
    <div class="p-3">
      <nav class="space-y-1">
        <NuxtLink
          v-for="item in menu"
          :key="item.label"
          :to="item.link"
          class="group flex items-center gap-3 rounded-lg px-3 py-2 outline-none transition"
          :class="isActive(item.link)
            ? 'bg-white/10 border border-white/10 text-white'
            : 'text-gray-300 hover:bg-white/5 border border-transparent hover:border-white/10'"
          @click="handleNavigate"
        >
          <UIcon :name="item.icon" class="h-5 w-5 shrink-0 opacity-90 group-hover:opacity-100" />
          <span class="font-medium text-sm truncate">{{ item.label }}</span>
          <UIcon
            name="i-lucide-chevron-right"
            class="ml-auto h-4 w-4 opacity-0 group-hover:opacity-100 transition"
          />
        </NuxtLink>
      </nav>
    </div>
  </aside>
</template>

<script setup lang="ts">
const emit = defineEmits<{ (e: 'navigate'): void }>()
const route = useRoute()

const menu = [
  { link: '/',         label: 'Home',     icon: 'i-lucide-home' },
  { link: '/features', label: 'Features', icon: 'i-lucide-shapes' },
  { link: '/pricing',  label: 'Pricing',  icon: 'i-lucide-badge-dollar-sign' },
]

const isActive = (link: string) => route.path === link

const handleNavigate = () => {
  // close drawer immediately on click
  emit('navigate')
}
</script>
