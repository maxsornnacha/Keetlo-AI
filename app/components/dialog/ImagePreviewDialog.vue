<template>
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
  <div
    v-if="isOpen && props.file"
    class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center"
    @click.self="close"
  >
    <div class="max-w-[90%] max-h-[90%] overflow-auto">
    <nuxt-img :src="onDisplayImage(props.file.base64 ? props.file.base64 : props.file.fileUrl as string)"/>
    </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import type { File } from '~/types/Projects';

const props = defineProps<{
  file: File | null
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'onClose'): void
}>()

const config = useRuntimeConfig();
const close = () => emit('onClose')

const onDisplayImage = (fileUrl : string) => {
    const dataOk = isDataUrlBase64(fileUrl);
    if(dataOk){
        return fileUrl;
    } else {
        return `${config.public.NUXT_PUBLIC_API_BASE}${fileUrl}`
    }

}
</script>