<!-- ~/pages/404.vue -->
<template>
  <div class="min-h-screen flex flex-col lg:flex-row lg:justify-center items-center lg:gap-24 p-8 text-center">
    <nuxt-img
    src="/images/not-found/not-found.png"
    alt="Keetlo Not Found"
    class="w-[200px] h-[200px] lg:w-[450px] lg:h-[450px]"
    />
    <div class="p-16 border border-gray-200 bg-white rounded-lg max-w-lg w-full">
    <h1 class="text-4xl lg:text-6xl font-bold mb-2">404</h1>
    <p class="text-gray-400 mb-6">This page doesn’t exist.</p>
     <NuxtLink to="/" class="px-4 py-2 rounded-md border border-gray-200 hover:bg-gray-200 bg-white">Go home</NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
useSeoMeta({ title: '404 – Not Found', robots: 'noindex, nofollow' })
</script>
