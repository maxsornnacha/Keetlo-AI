<!-- eslint-disable vue/no-v-html -->
<!-- components/ModalRoot.vue -->
<template>
  <Teleport to="body">
    <transition
      enter-active-class="duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="state.open"
        class="fixed inset-0 z-[3000] flex items-center justify-center p-4 sm:p-6"
        :style="safeAreaPadding"
        @keydown.esc.prevent="close(false)"
      >
        <!-- Backdrop (layered) -->
        <div class="absolute inset-0">
          <div class="absolute inset-0 bg-black/70" @click="close(false)" />
        </div>

        <!-- Dialog -->
        <transition
          enter-active-class="duration-200 ease-out"
          enter-from-class="opacity-0 translate-y-2 scale-[.985]"
          enter-to-class="opacity-100 translate-y-0 scale-100"
          leave-active-class="duration-150 ease-in"
          leave-from-class="opacity-100 translate-y-0 scale-100"
          leave-to-class="opacity-0 translate-y-1 scale-[.99]"
        >
          <div
            v-show="state.open"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="titleId"
            class="relative w-full max-w-md m-4"
          >
            <!-- gradient halo border -->
            <div class="pointer-events-none absolute -inset-[1px]" />
            <div
              class="relative rounded-lg border border-gray-200 bg-gray-50 text-black shadow-2xl"
            >
              <!-- focus trap sentinels -->
              <button ref="startSentinel" class="sr-only" @focus="focusFirst" />

              <!-- Header -->
              <div class="flex items-center gap-3 p-2">
                <div
                  class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br"
                  :class="iconBg"
                  aria-hidden="true"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-5 w-5"
                    :class="iconColor"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <circle cx="12" cy="12" r="9" />
                    <path d="M12 8v5" stroke-linecap="round" />
                    <circle cx="12" cy="16" r="1" fill="currentColor" />
                  </svg>
                </div>

                <div class="min-w-0">
                  <h2 :id="titleId" class="text-base font-semibold tracking-tight">
                    {{ state.title }}
                  </h2>
                </div>

                <!-- Close button -->
                <button
                  type="button"
                  class="ml-auto inline-flex p-2 cursor-pointer items-center justify-center rounded-lg text-black hover:bg-gray-200 transition"
                  aria-label="Close dialog"
                  @click="close(false)"
                >
                  <UIcon name="i-lucide-x" class="size-5" />
                </button>
              </div>

              <!-- Body -->
              <div class="max-h-[58vh] overflow-y-auto py-4 px-2 text-gray-700 text-sm">
                <div v-html="state.html" />
              </div>

              <!-- Actions -->
              <div class="p-2 flex flex-col sm:flex-row justify-end gap-2">
               <button
                  v-if="state.confirmText"
                  ref="confirmBtn"
                  type="button"
                  :class="confirmClass"
                  @click="close(true)"
                >
                  {{ state.confirmText ?? "OK" }}
                </button>

                <button
                  v-if="state.kind === 'confirm'"
                  ref="cancelBtn"
                  type="button"
                  class="cursor-pointer inline-flex w-full sm:w-auto min-w-[120px] items-center justify-center rounded-md p-2 hover:bg-gray-200 text-gray-700 border border-gray-200 text-sm font-medium transition"
                  @click="close(false)"
                >
                  {{ state.cancelText ?? "Cancel" }}
                </button>
              </div>

              <button ref="endSentinel" class="sr-only" @focus="focusLast" />
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

type ModalVariant = "default" | "danger" | "success";
type ModalKind = "confirm" | "alert";
interface ModalState {
  open: boolean;
  kind: ModalKind;
  title: string;
  html?: string;
  confirmText?: string;
  cancelText?: string;
  variant: ModalVariant;
}

const nuxtApp = useNuxtApp(); // ✅ INSIDE <script setup>
const state = computed<ModalState>(() => nuxtApp.$modal.state);

const titleId = `modal-title-${Math.random().toString(36).slice(2, 8)}`;
const safeAreaPadding = {
  padding:
    "env(safe-area-inset-top) env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left)",
};

const confirmBtn = ref<HTMLButtonElement | null>(null);
const cancelBtn = ref<HTMLButtonElement | null>(null);
const startSentinel = ref<HTMLButtonElement | null>(null);
const endSentinel = ref<HTMLButtonElement | null>(null);

const iconBg = computed(() => {
  switch (state.value.variant) {
    case "danger":
      return "from-rose-500/15 via-rose-400/10 to-rose-300/5";
    case "success":
      return "from-emerald-500/15 via-emerald-400/10 to-emerald-300/5";
    default:
      return "from-indigo-500/15 via-indigo-400/10 to-sky-400/10";
  }
});

const iconColor = computed(() =>
  state.value.variant === "danger"
    ? "text-rose-300"
    : state.value.variant === "success"
    ? "text-emerald-300"
    : "text-indigo-300"
);

const confirmClass = computed(() => {
  const base =
    "cursor-pointer inline-flex w-full sm:w-auto min-w-[120px] items-center justify-center rounded-lg p-2 text-sm"

  switch (state.value.variant) {
    case "danger":
      return [
        base,
        "bg-rose-600 hover:bg-rose-500 text-white focus-visible:ring-rose-400 shadow-rose-600/20"
      ].join(" ");
    case "success":
      return [
        base,
        "bg-emerald-600 hover:bg-emerald-500 text-white focus-visible:ring-emerald-400 shadow-emerald-600/20"
      ].join(" ");
    default:
      return [
        base,
        "bg-indigo-600 hover:bg-indigo-500 text-white focus-visible:ring-indigo-400 shadow-indigo-600/20"
      ].join(" ");
  }
});

function close(result: boolean) {
  nuxtApp.$modal.close(result);
}
function focusFirst() {
  if (state.value.kind === "confirm")
    (cancelBtn.value ?? confirmBtn.value)?.focus();
  else confirmBtn.value?.focus();
}
function focusLast() {
  confirmBtn.value?.focus();
}

watch(
  () => state.value.open,
  (open) => {
    document.documentElement.style.overflow = open ? "hidden" : "";
    if (open) setTimeout(focusFirst, 0);
  }
);
</script>
