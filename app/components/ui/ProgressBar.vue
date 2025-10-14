<template>
  <div class="w-full max-w-md">
    <!-- Bar -->
    <div class="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
      <div
        class="h-full bg-indigo-600 transition-all duration-300"
        :style="{ width: percent + '%' }"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

interface Props {
  used: number
  total: number
}

const props = defineProps<Props>()

const percent = computed(() => {
  if (props.total === 0) return 0
  return Math.min(100, (props.used / props.total) * 100)
})
</script>


