<!-- components/PageCanvas.vue -->
<template>
  <div class="relative w-full h-screen" :style="{ backgroundColor: canvasBg }">
    <!-- Header -->
    <div
      class="absolute w-[200px] top-0 right-0 flex items-center justify-end gap-4 p-4 text-white z-30"
    >
      <div class="flex items-center gap-2">
        <!-- Zoom controls -->
        <div class="flex gap-2 border border-slate-50/30 p-1 rounded-lg bg-black/70">
          <button
            class="px-3 py-1 text-black bg-white rounded cursor-pointer hover:bg-gray-200"
            @click="zoomOut"
          >
            -
          </button>
          <span class="text-white flex gap-1 items-center">
            <UIcon name="i-lucide-search" class="size-5" />
            ({{ (scale * 500).toFixed(0) }}%)
          </span>
          <button
            class="px-3 py-1 text-black bg-white rounded cursor-pointer hover:bg-gray-200"
            @click="zoomIn"
          >
            +
          </button>
        </div>

        <!-- Canvas BG color -->
        <div class="relative">
          <button
            ref="TriggerPaletteButtonRef"
            v-tooltip="'Canvas background color'"
            aria-haspopup="true"
            :aria-expanded="showPalette"
            class="cursor-pointer border border-slate-50/30 hover:bg-slate-900/90 bg-[#0D1117]/80 p-2 rounded-md flex gap-2 items-center"
            @click="showPalette = !showPalette"
          >
            <UIcon name="i-lucide-palette" class="w-4 h-4" />
            <span
              class="inline-block w-4 h-4 rounded border border-white/20"
              :style="{ backgroundColor: canvasBg }"
            />
          </button>

          <!-- Palette popover -->
          <div
            v-if="showPalette"
            ref="PaletteMenuRef"
            class="absolute right-0 mt-2 min-w-56 rounded-xl bg-[#0D1117] ring-1 ring-white/20 p-3 shadow-xl z-40"
            @keydown.escape="showPalette = false"
          >
            <div class="mb-2 text-xs uppercase tracking-wider text-slate-300">
              Canvas background
            </div>
            <div class="grid grid-cols-8 gap-2 mb-3">
              <button
                v-for="c in presetColors"
                :key="c"
                class="h-6 w-6 rounded-md border border-white/10 focus:outline-none focus:ring-2 focus:ring-indigo-400/50"
                :style="{ backgroundColor: c }"
                :aria-label="c"
                @click="setCanvasBg(c)"
              />
            </div>
            <div class="flex items-center gap-2">
              <input
                v-model="canvasBg"
                type="color"
                class="h-9 w-12 rounded-md border border-white/10 bg-transparent"
                aria-label="Pick custom color"
              >
              <input
                v-model="canvasBg"
                type="text"
                class="flex-1 rounded-lg border border-white/10 bg-white/5 px-2 py-1 text-sm text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
                placeholder="#0f172a"
              >
              <button
                class="px-2 py-1 rounded-md text-xs bg-white/10 hover:bg-white/20"
                @click="showPalette = false"
              >
                Done
              </button>
            </div>
            <div class="mt-2 text-[11px] text-slate-400">
              Hint: press <kbd class="px-1 rounded bg-white/10">Ctrl</kbd> /
              <kbd class="px-1 rounded bg-white/10">⌘</kbd> + wheel to zoom
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Canvas viewport (mouse zoom + background) -->
    <div
      ref="viewportRef"
      class="relative overflow-auto z-10 py-8 max-h-full max-w-full"
      :style="{ backgroundColor: canvasBg }"
      @click="selectedPageId = null"
      @wheel="onWheel"
    >
      <div
        class="relative"
        :style="{
          transform: 'scale(' + scale + ')',
          transformOrigin: 'top left',
          width: canvasWidth + 'px',
          height: canvasHeight + 'px',
        }"
      >
        <!-- Pages -->
        <div
          v-for="page in webPages"
          :key="page.path"
          class="absolute cursor-move group"
          :style="{
            top: page.top + 'px',
            left: page.left + 'px',
            width: page.width + 'px',
            height: page.height + 'px',
          }"
          @mousedown="startDrag($event, page)"
          @click.stop="selectedPageId = page.generatedPageId"
        >
          <!-- Hover Buttons -->
          <transition
            enter-active-class="transition ease-out duration-150"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
            leave-active-class="transition ease-in duration-100"
            leave-from-class="opacity-100 scale-100"
            leave-to-class="opacity-0 scale-95"
          >
            <div
              v-if="selectedPageId === page.generatedPageId"
              class="absolute -top-56 left-0 flex z-10 pointer-events-auto"
            >
              <button
                v-tooltip="'Preview'"
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 rounded-l-full cursor-pointer"
                @click.stop="openPreview(page.generatedPageId)"
              >
                <UIcon name="i-lucide-maximize-2" class="size-22" />
              </button>
              <button
                v-tooltip="'View HTML'"
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 cursor-pointer"
                @click.stop="viewHTML(page)"
              >
                <UIcon name="i-lucide-code-xml" class="size-24" />
              </button>
              <button
                v-tooltip="'Mobile view'"
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 cursor-pointer"
                @click.stop="mobileView(page)"
              >
                <UIcon name="i-lucide-smartphone" class="size-24" />
              </button>
              <button
                v-tooltip="'Desktop browser view'"
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 rounded-r-full cursor-pointer"
                @click.stop="desktopView(page)"
              >
                <UIcon name="i-lucide-screen-share" class="size-24" />
              </button>
            </div>
          </transition>

          <div
            class="flex justify-between items-end gap-4 pb-2 flex-wrap min-w-[600px]"
          >
            <div
              class="p-2 text-[50px] font-semibold"
              :style="{ color: accentTextColor}"
            >
              {{ page.label }}
            </div>
          </div>

          <iframe
            :srcdoc="page.htmlContent"
            class="w-full h-[calc(100%-100px)] pointer-events-none rounded-md shadow-lg bg-white border border-slate-300"
            :class="{
              'ring-4 ring-blue-500': selectedPageId === page.generatedPageId,
            }"
            sandbox="allow-scripts allow-same-origin" 
            referrerpolicy="no-referrer"
            @load="lockIframe"
          />

          <!-- Resize handle -->
          <div
            v-if="selectedPageId === page.generatedPageId"
            class="absolute bottom-[-10px] right-[-10px] w-[50px] h-[50px] bg-white border-6 border-blue-600 cursor-se-resize z-10"
            @mousedown.stop.prevent="startResize($event, page)"
          />
        </div>
      </div>
    </div>

    <!-- Preview & HTML Dialog -->
    <PreviewAndHtmlDialog
      v-model:selected-path="selectedPath"
      :type="type"
      :selected-page="selectedPage"
      :active-tab="activeTab"
      :web-pages="webPages"
      :open-preview="openPreview"
      :close-preview="closePreview"
      :on-change-active-tab="onChangeActiveTab"
    />

    <ResponsiveIframeSimulatorDialog
      :type="type"
      :web-pages="webPages"
      :selected-page="selectedPage"
      :selected-path="selectedPath"
      :project="project"
      :close-preview="closePreview"
      :initial-width="initialWidth"
      :initial-height="initialHeight"
      initital-domain="https://example.com"
      :initial-dpr="initialDpr"
      initial-scale-mode="dpr"
      :active-tab="activeTab"
    />
  </div>
</template>

<script setup lang="ts">
import { TinyColor } from '@ctrl/tinycolor'
import { reactive, ref, watch, onMounted } from "vue";
import type { WebPage } from "~/modules/pageStream";
import PreviewAndHtmlDialog from "./dialog/PreviewAndHtmlDialog.vue";
import ResponsiveIframeSimulatorDialog from "./dialog/ResponsiveIframeSimulatorDialog.vue";
import type { Project } from '~/pages/public/projects/preview/[projectId].vue';

const props = defineProps<{
  project: Project;
  webPages: WebPage[];
  selectedPageFromParent?: WebPage | null;
  onDeleteParentSelectedPage: () => void;
}>();

const canvasWidth = computed(() => {
  if (!webPages.length) return 1200 // default

  const maxRight = Math.max(
    ...webPages.map(p => p.left + p.width)
  )
  return maxRight + 200 // padding at the end
})

const canvasHeight = computed(() => {
  if (!webPages.length) return 800 // default

  const maxBottom = Math.max(
    ...webPages.map(p => p.top + p.height)
  )
  return maxBottom + 200 
})

/** Zoom state */
const scale = ref(0.2);
const MIN_SCALE = 0.1;
const MAX_SCALE = 3;

/** Canvas background (persisted) */
const canvasBg = ref<string>("#e5e7eb");
const showPalette = ref(false);
const presetColors = [
  "#0b1220",
  "#0f172a",
  "#111827",
  "#1f2937",
  "#0a0a0a",
  "#ffffff",
  "#f8fafc",
  "#e5e7eb",
  "#64748b",
  "#10b981",
  "#2563eb",
  "#f59e0b",
  "#ef4444",
  "#a855f7",
  "#22d3ee",
];
const PaletteMenuRef = ref<HTMLElement | null>(null);
const TriggerPaletteButtonRef = ref<HTMLElement | null>(null);
const setCanvasBg = (c: string) => {
  canvasBg.value = c;
  showPalette.value = false;
};

onMounted(() => {
  const saved = localStorage.getItem("public-preview-canvas:bg");
  if (saved) canvasBg.value = saved;
});
watch(canvasBg, (v) => localStorage.setItem("public-preview-canvas:bg", v));

/** Header controls */
const zoomIn = () => {
  scale.value = Math.min(MAX_SCALE, +(scale.value + 0.05).toFixed(3));
};
const zoomOut = () => {
  scale.value = Math.max(MIN_SCALE, +(scale.value - 0.05).toFixed(3));
};

/** Mouse-wheel zoom (Ctrl/⌘ + wheel) keeping pointer focus */
const viewportRef = ref<HTMLElement | null>(null);
const onWheel = (e: WheelEvent) => {
  if (!(e.ctrlKey || e.metaKey)) return; // allow normal scroll
  e.preventDefault();

  const vp = viewportRef.value;
  if (!vp) return;

  const rect = vp.getBoundingClientRect();
  const x = e.clientX - rect.left; // viewport coords
  const y = e.clientY - rect.top;

  const prev = scale.value;
  const delta = e.deltaY;
  const factor = delta > 0 ? 0.95 : 1.05; // smooth-ish
  const next = Math.min(
    MAX_SCALE,
    Math.max(MIN_SCALE, +(prev * factor).toFixed(4))
  );

  if (next === prev) return;

  const ratio = next / prev;

  // Keep the canvas point under the cursor stable:
  // scroll' = (scroll + pointer) * ratio - pointer
  const newScrollLeft = (vp.scrollLeft + x) * ratio - x;
  const newScrollTop = (vp.scrollTop + y) * ratio - y;

  scale.value = next;
  // Apply after scale so the new layout is correct
  vp.scrollLeft = newScrollLeft;
  vp.scrollTop = newScrollTop;
};

const selectedPageId = ref<string | null>(null);
const type = ref(1);
const initialWidth = ref(1200);
const initialHeight = ref(1400);
const initialDpr = ref(2);

const webPages = reactive(props.webPages);
const selectedPage = ref<null | (typeof webPages)[0]>(null);
const activeTab = ref<"preview" | "code">("preview");
const selectedPath = ref<null | string>(null);

watch(selectedPath, (value) => {
  if (!value) return;
  onChangePath(value);
});

const viewHTML = (page: WebPage) => {
  selectedPage.value = page;
  selectedPath.value = page.generatedPageId;
  type.value = 1;
  activeTab.value = "code";
};

const mobileView = (page: WebPage) => {
  selectedPage.value = page;
  selectedPath.value = page.generatedPageId;
  initialWidth.value = 460;
  initialHeight.value = 800;
  initialDpr.value = 1.5;
  type.value = 2;
};

const desktopView = (page: WebPage) => {
  selectedPage.value = page;
  selectedPath.value = page.generatedPageId;
  initialWidth.value = 1200;
  initialHeight.value = 1400;
  type.value = 2;
};

const { $modal } = useNuxtApp();

watch(
  () => props.selectedPageFromParent,
  (value) => {
    if (!value) return;
    openPreview(value.generatedPageId);
  }
);

const onChangePath = async (generatedPageId: string) => {
  const found = webPages.find(
    (page) => page.generatedPageId === generatedPageId
  );
  if (!found) {
    await $modal.alert({ 
      title: 'Error', 
      html: `<p>No page found</p>`, 
      variant: 'success' 
    });
    return;
  }
  selectedPage.value = found;
};

const openPreview = async (generatedPageId: string) => {
  const found = webPages.find(
    (page) => page.generatedPageId === generatedPageId
  );
  if (!found) {
     await $modal.alert({ 
      title: 'Error', 
      html: `<p>No page found</p>`, 
      variant: 'success' 
    });
    return;
  }
  selectedPage.value = found;
  selectedPath.value = found.generatedPageId;
  activeTab.value = "preview";
  type.value = 1;
};

function closePreview() {
  selectedPage.value = null;
  selectedPath.value = null;
  type.value = 1;
  props.onDeleteParentSelectedPage();
  initialDpr.value = 2;
}

const onChangeActiveTab = (tab: "preview" | "code") => {
  activeTab.value = tab;
};

// Drag
let draggedPage: WebPage | null = null;
let offsetX = 0;
let offsetY = 0;

const startDrag = (e: MouseEvent, page: WebPage) => {
  selectedPageId.value = page.generatedPageId;
  draggedPage = page;
  offsetX = e.clientX / scale.value - page.left;
  offsetY = e.clientY / scale.value - page.top;

  window.addEventListener("mousemove", onDrag);
  window.addEventListener("mouseup", stopDrag);
};

const onDrag = (e: MouseEvent) => {
  if (!draggedPage) return;
  draggedPage.left = e.clientX / scale.value - offsetX;
  draggedPage.top = e.clientY / scale.value - offsetY;
};

const stopDrag = () => {
  draggedPage = null;
  window.removeEventListener("mousemove", onDrag);
  window.removeEventListener("mouseup", stopDrag);
};

// Resize
let resizingPage: (typeof webPages)[0] | null = null;
let startWidth = 0;
let startHeight = 0;
let startX = 0;
let startY = 0;

const startResize = (e: MouseEvent, page: (typeof webPages)[0]) => {
  resizingPage = page;
  startWidth = page.width;
  startHeight = page.height;
  startX = e.clientX;
  startY = e.clientY;

  window.addEventListener("mousemove", onResize);
  window.addEventListener("mouseup", stopResize);
};

const onResize = (e: MouseEvent) => {
  if (!resizingPage) return;
  const deltaX = (e.clientX - startX) / scale.value;
  const deltaY = (e.clientY - startY) / scale.value;
  if (Math.max(100, startWidth + deltaX) < 400) return;
  resizingPage.width = Math.max(100, startWidth + deltaX);
  resizingPage.height = Math.max(100, startHeight + deltaY);
};

const stopResize = () => {
  resizingPage = null;
  window.removeEventListener("mousemove", onResize);
  window.removeEventListener("mouseup", stopResize);
};

const handleClickOutsidePaletteMenu = (event: MouseEvent) => {
  if (
    PaletteMenuRef.value &&
    !PaletteMenuRef.value.contains(event.target as Node) &&
    TriggerPaletteButtonRef.value &&
    !TriggerPaletteButtonRef.value.contains(event.target as Node)
  ) {
    showPalette.value = false;
  }
};

const accentTextColor = computed(() => {
  const bg = new TinyColor(canvasBg.value)
  const fg = bg.isDark()
    ? bg.clone().saturate(20).lighten(65) // background is dark → make text light
    : bg.clone().saturate(20).darken(65)  // background is light → make text dark
  return fg.toHexString()
})

onMounted(() => {
  document.addEventListener("click", handleClickOutsidePaletteMenu);
});
onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutsidePaletteMenu);
});
</script>
