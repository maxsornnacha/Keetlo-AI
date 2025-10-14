<template>
  <div
    class="h-full w-full bg-black/70 fixed inset-0 z-[9999] top-0 flex items-center justify-center"
  >
    <div class="grid gap-3">
      <div class="flex items-center justify-center gap-1.5">
        <div class="w-4 h-4 rounded-full bg-indigo-600 animate-bounce-delay-1" />
        <div class="w-4 h-4 rounded-full bg-indigo-600 animate-bounce-delay-2" />
        <div class="w-4 h-4 rounded-full bg-indigo-600 animate-bounce-delay-3" />
        <div class="w-4 h-4 rounded-full bg-indigo-600 animate-bounce-delay-4" />
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes bounceDelay {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.animate-bounce-delay-1 {
  animation: bounceDelay 1.4s infinite ease-in-out;
  animation-delay: 0s;
}

.animate-bounce-delay-2 {
  animation: bounceDelay 1.4s infinite ease-in-out;
  animation-delay: 0.2s;
}

.animate-bounce-delay-3 {
  animation: bounceDelay 1.4s infinite ease-in-out;
  animation-delay: 0.4s;
}

.animate-bounce-delay-4 {
  animation: bounceDelay 1.4s infinite ease-in-out;
  animation-delay: 0.6s;
}
</style>
