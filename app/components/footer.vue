<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <footer class="border-t border-[#21262d] bg">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-10 py-10">
      <div class="flex flex-col items-center justify-between gap-6 sm:flex-row">
        <div class="flex flex-col items-center gap-2 sm:items-start">
          <div class="flex items-center gap-2">
              <nuxt-img
        src="/logo.png"
        class="h-10 w-10"
        />
            <h3 class="text-lg font-bold">Keetlo</h3>
          </div>
          <p class="text-sm text-gray-400">
            © 2025 Sornnacha Buranapongwattana
          </p>
        </div>
        <div
          class="flex flex-wrap items-center justify-center gap-6 text-sm text-gray-400"
        >
          <a class="hover:text-white transition-colors" href="/terms-of-service"
            >Terms of Service</a
          >
          <a class="hover:text-white transition-colors" href="/privacy-policy"
            >Privacy Policy</a
          >
          <a class="hover:text-white transition-colors" href="/contact-us">Contact Us</a>
        </div>
      </div>
    </div>
  </footer>
</template>
