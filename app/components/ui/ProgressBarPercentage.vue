<template>
  <div class="w-full max-w-md">
    <div class="flex justify-between mb-2">
      <span class="text-slate-300 font-medium">{{ label }}</span>
      <span class="text-slate-300 font-medium">{{ progress }}%</span>
    </div>
    <div class="w-full bg-gray-300 rounded-full h-2 overflow-hidden">
      <div
        class="bg-indigo-500 h-2 rounded-full transition-all duration-500"
        :style="{ width: progress + '%' }"
      />
    </div>
  </div>
</template>

<script setup>
defineProps({
  progress: {
    type: Number,
    default: 0
  },
  label: {
    type: String,
    default: 'Progress'
  }
})
</script>
