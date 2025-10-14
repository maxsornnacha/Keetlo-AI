<template>
  <Teleport v-if="selectedPage && type === 2" to="body">
    <!-- Modal Root (dark class applied here so Tailwind dark: variants work inside) -->
    <div class="dark">
      <div class="fixed inset-0 z-[9999] flex items-center justify-center">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/70" @click="close()" />

        <!-- Dialog Panel -->
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="opacity-0"
          enter-to-class="opacity-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="opacity-0"
        >
          <div
            v-if="selectedPage && type === 2"
            ref="panelRef"
            class="relative w-full max-h-full h-full bg-white dark:bg-gray-950 rounded-2xl shadow-2xl border border-gray-200 dark:border-gray-800 overflow-hidden"
            role="dialog"
            aria-modal="true"
          >
            <!-- Header -->
            <header
              class="flex items-center justify-between px-5 py-3 border-b border-gray-200 dark:border-gray-800"
            >
              <h2 class="text-lg font-semibold">
                {{ props.selectedPage?.label }}
              </h2>
              <div class="flex items-center gap-2">
                <button
                  class="cursor-pointer rounded-full px-2 pt-2 pb-1 bg-red-600 text-white hover:bg-red-700 transition"
                  aria-label="Close preview"
                  @click="close()"
                >
                  <UIcon name="i-lucide-x" class="size-5" />
                </button>
              </div>
            </header>

            <!-- Browser chrome (address bar) -->
            <div
              class="flex items-center gap-3 px-4 py-2 border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950"
            >
              <div class="flex items-center gap-1">
                <span class="inline-block w-3 h-3 rounded-full bg-red-500" />
                <span class="inline-block w-3 h-3 rounded-full bg-yellow-400" />
                <span class="inline-block w-3 h-3 rounded-full bg-green-500" />
              </div>
              <div class="flex items-center gap-2 ml-3">
                <button
                  disabled
                  class="rounded-md border border-gray-300 dark:border-gray-700 px-2 py-1 text-sm hover:bg-gray-50 dark:hover:bg-gray-900 disabled:opacity-40"
                >
                  ←
                </button>
                <button
                  disabled
                  class="rounded-md border border-gray-300 dark:border-gray-700 px-2 py-1 text-sm hover:bg-gray-50 dark:hover:bg-gray-900 disabled:opacity-40"
                >
                  →
                </button>
                <button
                  disabled
                  class="rounded-md border border-gray-300 dark:border-gray-700 px-2 py-1 text-base hover:bg-gray-50 dark:hover:bg-gray-900"
                >
                  <UIcon name="i-lucide-rotate-cw" class="size-4 mt-1" />
                </button>
              </div>
              <div class="flex-1 flex items-center gap-2 min-w-0">
                <input
                  v-model="urlInput"
                  disabled
                  type="text"
                  spellcheck="false"
                  class="flex-1 min-w-0 rounded-lg border border-gray-300 dark:border-gray-700 px-3 py-1.5 bg-gray-50 dark:bg-gray-900 focus:bg-white dark:focus:bg-gray-900"
                >
                <button
                  class="rounded-lg px-4 py-1.5 text-white hover:opacity-95 bg-indigo-600"
                >
                  Go
                </button>
              </div>
            </div>

            <!-- Controls -->
            <div
              class="flex flex-wrap items-end gap-4 px-4 py-3 border-b border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-gray-950/60"
            >
              <div class="flex items-end gap-3">
                <label class="text-xs"
                  >W
                  <input
                    v-model.number="width"
                    type="number"
                    min="200"
                    max="2400"
                    class="block border border-gray-300 dark:border-gray-700 rounded px-2 py-1 w-24 bg-white dark:bg-gray-900"
                  >
                </label>
                <label class="text-xs"
                  >H
                  <input
                    v-model.number="height"
                    type="number"
                    min="200"
                    max="3000"
                    class="block border border-gray-300 dark:border-gray-700 rounded px-2 py-1 w-24 bg-white dark:bg-gray-900"
                  >
                </label>
                <label class="text-xs"
                  >DPR
                  <select
                    v-model.number="dpr"
                    class="block border border-gray-300 dark:border-gray-700 rounded px-2 py-1 w-24 bg-white dark:bg-gray-900"
                  >
                    <option :value="1">1x</option>
                    <option :value="1.5">1.5x</option>
                    <option :value="2">2x</option>
                    <option :value="3">3x</option>
                  </select>
                </label>
              </div>

              <div class="flex items-end gap-3">
                <label class="text-xs"
                  >Scale Mode
                  <select
                    v-model="scaleMode"
                    class="block border border-gray-300 dark:border-gray-700 rounded px-2 py-1 w-40 bg-white dark:bg-gray-900"
                  >
                    <option value="dpr">DPR (1/DPR)</option>
                    <option value="fit">Fit to Window</option>
                    <option value="custom">Custom Zoom</option>
                  </select>
                </label>
                <button
                  class="cursor-pointer flex items-center gap-2 rounded-md border border-gray-300 dark:border-gray-700 px-3 py-1.5 hover:bg-gray-100 dark:hover:bg-gray-900"
                  title="Rotate"
                  @click="rotate"
                >
                  <UIcon name="i-lucide-rotate-cw" class="size-4" />
                  Rotate
                </button>
              </div>

              <div
                class="ml-auto text-sm text-gray-600 dark:text-gray-300 truncate"
                :title="infoText"
              >
                {{ infoText }}
              </div>
            </div>
            <!-- Stage -->
            <div class="overflow-auto h-full max-h-screen bg-white p-6">
              <div id="stage" ref="stageRef" class="mx-auto w-max">
                <div ref="viewportRef" class="[transform-origin:top_left]">
                  <iframe
                    ref="frameRef"
                    :srcdoc="selectedPage.htmlContent"
                    :width="width"
                    :height="height"
                    class="rounded-lg border-[10px] border-black shadow-2xl max-h-[1800px] bg-white"
                    sandbox="allow-scripts allow-same-origin"
                    referrerpolicy="no-referrer"
                    @load="lockIframe"
                  />
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from "vue";
import type { WebPage } from "~/modules/pageStream";

// Props / Emits
const props = defineProps<{
  type: number;
  selectedPage: WebPage | null;
  activeTab: string;
  selectedPath: string | null;
  webPages: WebPage[];
  initialScaleMode: string;
  initialWidth: number;
  initialHeight: number;
  initialDpr: number;
  inititalDomain: string;
  closePreview: () => void;
}>();

const width = ref(props.initialWidth);
const height = ref(props.initialHeight);
const dpr = ref(props.initialDpr);
const scaleMode = ref(props.initialScaleMode);
const selectedPage = ref(props.selectedPage);
const type = ref(props.type);
const domain = ref(props.inititalDomain);

watch(
  () =>
    [
      props.initialWidth,
      props.initialHeight,
      props.initialDpr,
      props.initialScaleMode,
      props.selectedPage,
      props.type,
      props.inititalDomain,
    ] as const,
  ([w, h, d, s, page, t, dom]) => {
    width.value = w;
    height.value = h;
    dpr.value = d;
    scaleMode.value = s;
    selectedPage.value = page;
    type.value = t;
    domain.value = dom;
  }
);

watch([selectedPage, type], async () => {
  await nextTick();
  applySize();
});

watch([dpr, width, height, scaleMode], () => {
  applySize();
});

// DOM refs
const panelRef = ref<HTMLElement | null>(null);
const frameRef = ref<HTMLIFrameElement | null>(null);
const viewportRef = ref<HTMLElement | null>(null);
const stageRef = ref<HTMLElement | null>(null);
// URL input
const urlInput = ref("");
watch(selectedPage, (value) => {
  if (!value) {
    return;
  }
  urlInput.value = domain.value + value.path;
});

// Derived
const infoText = computed(() => {
  const scale = computeScale(width.value, height.value);
  const visW = Math.round(width.value * scale);
  const visH = Math.round(height.value * scale);
  const modeLabel =
    scaleMode.value === "fit"
      ? "Fit"
      : scaleMode.value === "custom"
      ? `Zoom ${Math.round(scale * 100)}%`
      : `DPR ${dpr.value}x`;
  return `Viewport: ${width.value}×${height.value} (${modeLabel}) • Visible ~ ${visW}×${visH}`;
});

// Methods
function close() {
  props.closePreview();
}

function rotate() {
  const w = width.value;
  width.value = height.value;
  height.value = w;
}

function computeScale(vw: number, vh: number) {
  if (scaleMode.value === "dpr") {
    return 1 / (dpr.value || 1);
  }
  if (scaleMode.value === "fit") {
    const checker = panelRef.value;
    const bezelPad = 28;
    const maxW = Math.max((checker?.clientWidth || 1200) - 80, 200);
    const maxH = Math.max((checker?.clientHeight || 700) - 260, 200);
    const sX = (maxW - bezelPad) / vw;
    const sY = (maxH - bezelPad) / vh;
    return Math.max(0.1, Math.min(sX, sY));
  }
  // custom zoom
  return 100 / 100;
}

function applySize() {
  const frame = frameRef.value;
  const viewport = viewportRef.value;
  const stage = stageRef.value;
  if (!frame || !viewport || !stage) return;

  const vw = Math.max(parseInt(String(width.value)) || 390, 200);
  const vh = Math.max(parseInt(String(height.value)) || 844, 200);

  frame.width = vw.toString();
  frame.height = vh.toString();

  const scale = computeScale(vw, vh);
  viewport.style.transform = `scale(${scale})`;

  // visible (after scale) for stage sizing
  const visibleW = Math.round(vw * scale);
  stage.style.width = `${visibleW + 28}px`;
}

watch(
  () => props.selectedPage?.htmlContent,
  (u) => {
    if (frameRef.value && u) frameRef.value.src = u;
  }
);

// Init
onMounted(async () => {
  await nextTick();
  applySize();
  window.addEventListener("resize", onResize);
  window.addEventListener("keydown", onKeydown);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", onResize);
  window.removeEventListener("keydown", onKeydown);
});

function onResize() {
  if (scaleMode.value === "fit") applySize();
}
function onKeydown(e: KeyboardEvent) {
  if (e.key === "Escape") close();
}
</script>
