<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <footer>
    <div class="bg-white border border-gray-200 rounded-t-4xl mx-auto max-w-[1400px] px-4 sm:px-6 lg:px-10 py-10">
      <div class="flex flex-col items-center justify-between gap-6 sm:flex-row">
        <div class="flex flex-col items-center gap-2 sm:items-start">
          <div class="flex items-center gap-0">
              <nuxt-img
        src="/icon-256.png"
        class="size-5"
        />
            <h3 class="text-xl font-bold">EETLO</h3>
          </div>
          <p class="text-sm text-gray-500">
            © 2025 Sornnacha Buranapongwattana
          </p>
        </div>
        <div
          class="flex flex-wrap items-center justify-center gap-6 text-sm"
        >
          <a class="hover:text-black transition-colors hover:text-gray-500" href="/terms-of-service"
            >Terms of Service</a
          >
          <a class="hover:text-black transition-colors hover:text-gray-500" href="/privacy-policy"
            >Privacy Policy</a
          >
          <a class="hover:text-black transition-colors hover:text-gray-500" href="/contact-us">Contact Us</a>
        </div>
      </div>
    </div>
  </footer>
</template>
