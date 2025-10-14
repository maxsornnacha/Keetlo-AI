<template>
  <main
    class="relative min-h-screen grid place-items-center overflow-hidden text-white"
  >
    <!-- Soft gradient background -->
    <div class="pointer-events-none absolute inset-0 -z-10" aria-hidden="true">
      <div
        class="absolute -top-40 -left-40 h-[46rem] w-[46rem] rounded-full blur-3xl opacity-30 bg-[conic-gradient(at_left,_theme(colors.red.500),_theme(colors.orange.400),_theme(colors.amber.400),_theme(colors.red.500))]"
      />
      <div
        class="absolute -bottom-56 -right-32 h-[42rem] w-[42rem] rounded-full blur-3xl opacity-25 bg-[radial-gradient(60%_60%_at_50%_50%,_theme(colors.slate.600),_transparent)]"
      />
    </div>

    <!-- Card -->
    <section
      class="w-full max-w-xl rounded-2xl border border-white/10 bg-white/5 backdrop-blur-md shadow-2xl px-6 py-8 sm:px-8"
      aria-labelledby="cancel-title"
    >
      <!-- Warning badge -->
      <div class="mx-auto mb-6 grid place-items-center">
        <div class="relative">
          <span
            class="absolute inset-0 rounded-full ring-2 ring-red-400/25 animate-pulse-slower"
            aria-hidden="true"
          />
          <div
            class="flex h-16 w-16 items-center justify-center rounded-full bg-red-500/15 text-red-400 ring-1 ring-red-400/30"
          >
            <UIcon name="i-lucide-x-circle" class="size-8" />
          </div>
        </div>
      </div>

      <header class="text-center space-y-2">
        <h1 id="cancel-title" class="text-3xl font-bold tracking-tight">
          Payment Canceled
        </h1>
        <p class="text-white/70">
          Your payment was canceled.
          <span class="text-white/80">No charge was made.</span>
        </p>
      </header>

      <!-- Helpful info -->
      <div class="mt-6 rounded-xl border border-white/10 bg-white/5 p-5 sm:p-6">
        <ul class="space-y-3 text-sm text-white/75">
          <li class="flex items-start gap-3">
            <UIcon name="i-lucide-info" class="mt-0.5 size-4 text-white/60" />
            <span>If this was a mistake, you can safely try again.</span>
          </li>
          <li class="flex items-start gap-3">
            <UIcon name="i-lucide-shield" class="mt-0.5 size-4 text-white/60" />
            <span>We didn’t bill your card and your order wasn’t created.</span>
          </li>
          <li class="flex items-start gap-3">
            <UIcon
              name="i-lucide-life-buoy"
              class="mt-0.5 size-4 text-white/60"
            />
            <span
              >Need help? Our team can assist you with checkout issues.</span
            >
          </li>
        </ul>
      </div>

      <!-- Actions -->
      <div class="mt-6 grid gap-3 sm:grid-cols-2">
        <!-- Primary: Back home -->
        <a href="/" class="w-full">
          <button
            class="cursor-pointer w-full h-11 rounded-lg bg-slate-800 hover:bg-slate-700 text-white font-semibold transition-colors"
          >
            Go back home
          </button>
        </a>

        <!-- Secondary: Try again (optional: point back to your pricing/checkout) -->
        <a href="/pricing" class="w-full">
          <button
            class="cursor-pointer w-full h-11 rounded-lg bg-white/10 hover:bg-white/15 text-white font-medium transition-colors"
          >
            Try again
          </button>
        </a>
      </div>

      <!-- Support -->
      <p class="mt-6 text-center text-sm text-white/60">
        Still stuck?
        <a class="text-indigo-300 hover:text-indigo-200" href="/contact-us"
          >Contact support</a
        >
      </p>
    </section>
  </main>
</template>

<script setup lang="ts">
// No external imports required
</script>

<style scoped>
/* Gentle pulse for the warning ring */
@keyframes pulse-slower {
  0% {
    transform: scale(0.95);
    opacity: 0.5;
  }
  60% {
    transform: scale(1.05);
    opacity: 0;
  }
  100% {
    transform: scale(1.05);
    opacity: 0;
  }
}
.animate-pulse-slower {
  animation: pulse-slower 2.4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
