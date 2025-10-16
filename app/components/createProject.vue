<template>
  <div class="relative w-full">
    <!-- Card container -->
    <div
      class="group relative rounded-2xl border border-white/10 bg-white/[0.3] backdrop-blur
             shadow-[inset_0_1px_0_0_rgba(255,255,255,.06)] transition"
    >
      <!-- Top gradient hairline -->
      <div class="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/20 to-transparent" />

      <!-- Floating label -->
      <label
        for="project-idea"
        class="absolute -top-3 left-4 inline-flex items-center gap-2 px-2 rounded-full
               text-[11px] font-semibold tracking-wide uppercase
               bg-[#0f1320] text-white/90 ring-1 ring-white/10"
      >
        <UIcon name="i-lucide-sparkles" class="size-3.5" />
        Project Brief
      </label>

      <!-- Textarea -->
      <textarea
        id="project-idea"
        ref="textareaRef"
        v-model="inputValue"
        :maxlength="maxChars"
        :placeholder="animatedPlaceholder"
        class="w-full min-h-[140px] max-h-[55vh] rounded-2xl bg-transparent px-5 pt-5 pb-16
               text-base text-white/90 placeholder:text-white/40 focus:outline-none
               resize-none leading-relaxed"
        :aria-busy="loadingSubmit ? 'true' : 'false'"
        @input="autoResize"
        @keydown.enter.exact.prevent="generateProject"
        @keydown.meta.enter.prevent="generateProject"
        @keydown.ctrl.enter.prevent="generateProject"
      />

      <!-- Bottom bar: helper + counter + button -->
      <div
        class="absolute bottom-0 left-0 right-0 flex flex-wrap items-center gap-3
               border-t border-white/10 px-3 py-2.5 bg-[#0e1220]/60 backdrop-blur rounded-b-2xl"
      >
        <div class="ml-auto flex items-center gap-3">
          <!-- Counter -->
          <span class="text-xs tabular-nums text-white/60">
            {{ chars }}/{{ maxChars }}
          </span>

          <!-- Submit -->
          <button
            type="button"
            class="relative inline-flex items-center gap-2 h-10 px-4 rounded-lg text-white text-sm font-medium
                   transition disabled:opacity-60 disabled:cursor-not-allowed
                   focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400/50"
            :class="canSubmit ? 'cursor-pointer bg-indigo-600 hover:bg-indigo-700' : 'bg-indigo-500'"
            :disabled="!canSubmit || loadingSubmit"
            @click="generateProject"
          >
            <UIcon
              v-if="!loadingSubmit"
              name="i-lucide-wand-sparkles"
              class="size-5 text-white"
            />
            <UIcon
              v-else
              name="i-lucide-loader-circle"
              class="size-5 text-white animate-spin"
            />
            <span>{{ loadingSubmit ? 'Generating…' : 'Generate' }}</span>

            <!-- Progress hairline while loading -->
            <span
              v-if="loadingSubmit"
              class="pointer-events-none absolute -bottom-[2px] left-0 h-[2px] w-full overflow-hidden rounded-b-lg"
            >
              <span class="block h-full w-1/2 animate-[progress_1.2s_linear_infinite] bg-white/70"/>
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Tiny tips row -->
    <p class="mt-2 text-xs text-white/50">
      Tip: Press <kbd class="rounded bg-white/10 px-1.5 py-0.5">Shift + Enter</kbd> for new line,
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">⌘/Ctrl</kbd> +
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> Or 
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> to generate.
       to generate.
    </p>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
const { $modal } = useNuxtApp();

const config = useRuntimeConfig();

const textareaRef = ref<HTMLTextAreaElement | null>(null);
const inputValue = ref<string>("");
const loadingSubmit = ref(false);

/* ------- UX helpers ------- */
const maxChars = ref(500); // soft cap for nice counter UX
const chars = ref(0);
watch(inputValue, v => (chars.value = v.length));

/* Animated placeholder (typed-in effect) */
const fullPlaceholder =
  "Describe your website idea… e.g. “A responsive landing with hero, features grid, and a contact form.”";
const animatedPlaceholder = ref("");
function runPlaceholderTyping() {
  let i = 0;
  animatedPlaceholder.value = "";
  const tick = () => {
    if (i <= fullPlaceholder.length) {
      animatedPlaceholder.value = fullPlaceholder.slice(0, i++);
      requestAnimationFrame(tick);
    }
  };
  tick();
}

/* Auto-resize textarea to content */
function autoResize() {
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = "0px";
  el.style.height = Math.min(el.scrollHeight, window.innerHeight * 0.55) + "px";
}

/* Submit rules */
const canSubmit = computed(() => !!inputValue.value && !loadingSubmit.value && inputValue.value.length <= maxChars.value);

/* ------- Submit ------- */
const generateProject = async () => {
  if (!canSubmit.value) return;
  loadingSubmit.value = true;

  try {
    const payload = { input: inputValue.value.trim() };
    const response = await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/create`,
      payload,
    );
    const projectId = response.data.projectId;
    window.location.href = `/account/projects/${projectId}`;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.log(error)
      if(error.status === 401){
        window.location.href = `/auth/login?next=${window.location.href}`
      } else {
      await $modal.alert({ 
      title: 'Error', 
      html: `<p>${toErrorMessage(error?.response?.data?.message)}</p>`, 
      variant: 'danger' 
      });
    }
    } else {
      alert(3)
       await $modal.alert({ 
      title: 'Error', 
      html: `<p>${toErrorMessage(error)}</p>`, 
      variant: 'danger' 
      });
    }
  } finally {
    loadingSubmit.value = false;
  }
};

/* ------- Mount ------- */
onMounted(() => {
  runPlaceholderTyping();
  nextTick(autoResize);
});
</script>

<style scoped>
@keyframes progress {
  0%   { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}
</style>
