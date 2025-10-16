<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-16 min-h-[70vh]">
      <slot />
    </div>
    <Footer />
    <ModalRoot/>
  </div>
</template>
<script setup lang="ts">
import Header from '~/components/header.vue'
import Footer from '~/components/footer.vue'
import ModalRoot from '~/components/modal/ModalRoot.vue'
</script>