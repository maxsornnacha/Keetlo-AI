<!-- components/PageCanvas.vue -->
<template>
  <div class="relative w-full h-screen">
    <!-- Header -->
    <div
      class="relative sticky top-0 left-0 flex items-center justify-between gap-4 p-2 opacity-90 backdrop-blue-sm text-black z-30"
    >
      <div class="hidden lg:block">
        <h2 class=" font-semibold">
          {{ props.project?.title && props.project?.title.length > 50 ? props.project.title.slice(0, 50)+"..." : props.project.title || "No title" }}
        </h2>
      </div>

      <div class="flex items-center gap-2">
        <button
        v-tooltip="'Collapsed'"
         class="cursor-pointer flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
          @click="props.toggleChatDisplay"
        >
              <UIcon
      :name="props.chatDisplayIsCollapsed ? 'i-lucide-panel-right-open' : 'i-lucide-panel-left-close'"
      class="size-5"
    />
        </button>
        <!-- Download project -->
        <button
                v-tooltip="'Download Project'"
            class="cursor-pointer flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
          @click="props.downloadProject"
        >
          <UIcon name="i-lucide-download" class="size-5" />
        </button>

        <!-- Zoom controls -->
        <div class="flex gap-2 border border-slate-50/30 p-1 rounded-lg">
          <button
            class="px-3 py-1 text-black border border-gray-200 rounded cursor-pointer hover:bg-gray-200"
            @click="zoomOut"
          >
            -
          </button>
          <span class="text-black flex gap-1 items-center">
            <UIcon name="i-lucide-search" class="size-4" />
            ({{ (scale * 700).toFixed(0) }}%)
          </span>
          <button
            class="px-3 py-1 text-black border border-gray-200 rounded cursor-pointer hover:bg-gray-200"
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
            class="cursor-pointer border border-gray-200 hover:bg-gray-200 p-2 rounded-md flex gap-2 items-center"
            @click="showPalette = !showPalette"
          >
            <UIcon name="i-lucide-palette" class="w-4 h-4" />
            <span
              class="inline-block w-4 h-4 rounded border border-gray-200"
              :style="{ backgroundColor: canvasBg }"
            />
          </button>

          <!-- Palette popover -->
          <div
            v-if="showPalette"
            ref="PaletteMenuRef"
            class="absolute right-0 mt-2 min-w-56 rounded-xl bg-white ring-1 ring-white/20 p-3 shadow-xl z-40"
            @keydown.escape="showPalette = false"
          >
            <div class="mb-2 text-xs uppercase tracking-wider text-slate-700">
              Canvas background
            </div>
            <div class="grid grid-cols-8 gap-2 mb-3">
              <button
                v-for="c in presetColors"
                :key="c"
                class="h-6 w-6 rounded-md border border-gray-200 focus:outline-none focus:border focus:border-indigo-500 cursor-pointer"
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
                class="px-2 py-1 rounded-md text-xs bg-gray-100 hover:bg-gray-200 cursor-pointer"
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

        <!-- Public toggle -->
        <button
          v-tooltip="'Public'"
          class="cursor-pointer flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
          @click="props.onOpenChangePublicStatusDialog"
        >
          <UIcon
            name="i-lucide-globe"
            class="size-5"
            :class="
              project?.isPublic === 1
                ? 'text-emerald-600 animate-pulse'
                : 'text-red-500'
            "
          />
        </button>
      </div>
    </div>

    <!-- Canvas viewport (mouse zoom + background) -->
    <div
      ref="viewportRef"
      class="relative overflow-auto z-10 py-8 max-h-full max-w-full rounded-lg border border-slate-200"
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
          @mousedown="!loadingCreatePages && startDrag($event, page)"
          @click.stop="selectedPageId = page.generatedPageId"
        >
        <div v-if="loadingCreatePages" class="absolute w-full h-full bg-white/10 flex flex-col items-center justify-center">
            <div class="text-black text-[60px] flex flex-col gap-4 items-center justify-center">
              <UIcon
                name="i-lucide-loader-circle"
                class="size-[100px] text-indigo-700 animate-spin"
              />
              Loading
            </div>
         </div>
        <div class="h-full w-full">
          <!-- Hover Buttons -->
          <transition
            v-if="!loadingCreatePages"
            enter-active-class="transition ease-out duration-150"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
            leave-active-class="transition ease-in duration-100"
            leave-from-class="opacity-100 scale-100"
            leave-to-class="opacity-0 scale-95"
          >
            <div
              v-if="selectedPageId === page.generatedPageId"
              class="absolute -top-56 left-0 flex z-10 pointer-events-auto border border-gray-200 rounded-full"
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
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 cursor-pointer"
                @click.stop="desktopView(page)"
              >
                <UIcon name="i-lucide-screen-share" class="size-24" />
              </button>
              <button
                v-tooltip="'Download HTML file'"
                class="text-white px-8 border-r-2 border-slate-50/90 bg-[#0D1117] hover:bg-slate-700 py-8 cursor-pointer"
                @click.stop="props.downloadSinglePage(page)"
              >
                <UIcon name="i-lucide-download" class="size-24" />
              </button>
              <button
                v-tooltip="'Delete'"
                class="text-white px-8 bg-[#0D1117] hover:bg-slate-700 py-8 rounded-r-full cursor-pointer"
                @click.stop="deletePage(page)"
              >
                <UIcon name="i-lucide-trash-2" class="size-24" />
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
            v-if="selectedPageId === page.generatedPageId && !loadingCreatePages"
            class="absolute bottom-[-10px] right-[-10px] w-[50px] h-[50px] bg-white border-6 border-blue-600 cursor-se-resize z-10"
            @mousedown.stop.prevent="startResize($event, page)"
          />
        </div>
        </div>

        <!-- Loading Creating Page -->
        <div
          v-if="props.loadingCreatePages != null"
          class="absolute rounded-lg bg-gray-200 border border-black animate-pulse overflow-hidden shadow-md"
          :style="{
            top: props.loadingCreatePages.top + 'px',
            left: props.loadingCreatePages.left + 'px',
            width: props.loadingCreatePages.width + 'px',
            height: props.loadingCreatePages.height + 'px',
          }"
        >
          <div
            class="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent animate-[shimmer_1.5s_infinite] border border-slate-50/20"
          />
          <div
            v-if="
              props.loadingCreatePages !== null &&
              props.progressCreatePages.status
            "
            class="flex justify-center items-center h-full w-full"
          >
            <div class="w-full p-16">
              <div class="flex justify-between mb-2">
                <span class="text-black text-[50px] font-medium">
                  {{ props.progressCreatePages.label }}
                </span>
                <span class="text-black text-[50px] font-medium">
                  {{ props.progressCreatePages.progress }}%
                </span>
              </div>
              <div class="w-full bg rounded-full h-10 overflow-hidden">
                <div
                  class="bg-indigo-500 h-10 rounded-full transition-all duration-500"
                  :style="{ width: props.progressCreatePages.progress + '%' }"
                />
              </div>
            </div>
          </div>
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

    <PageLoader v-if="loadingOnDeletePage"/>
  </div>
</template>

<script setup lang="ts">
import { TinyColor } from '@ctrl/tinycolor'
import SockJS from "sockjs-client";
import debounce from 'lodash/debounce'
import { Client } from "@stomp/stompjs";
import { reactive, ref, watch, onMounted } from "vue";
import type { WebPage } from "~/modules/pageStream";
import type { PagePosition } from "~/types/PagePostion";
import type { Project } from "~/types/Projects";
import PreviewAndHtmlDialog from "./dialog/PreviewAndHtmlDialog.vue";
import ResponsiveIframeSimulatorDialog from "./dialog/ResponsiveIframeSimulatorDialog.vue";
import axios from "axios";
import PageLoader from './ui/PageLoader.vue';
const { $modal } = useNuxtApp();

const props = defineProps<{
  progressCreatePages: { progress: number; label: string; status: boolean };
  project: Project;
  webPages: WebPage[];
  selectedPageFromParent?: WebPage | null;
  onDeleteParentSelectedPage: () => void;
  loadingCreatePages: PagePosition | null;
  getProject: () => Promise<void>;
  getPages: () => Promise<void>;
  downloadProject: () => void;
  downloadSinglePage: (arg: WebPage) => void;
  onOpenChangePublicStatusDialog: () => void;
  toggleChatDisplay: () => void;
  chatDisplayIsCollapsed: boolean;
}>();
const loadingOnDeletePage = ref(false);

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
const scale = ref(0.15);
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
  const saved = localStorage.getItem("canvas:bg");
  if (saved) canvasBg.value = saved;
});
watch(canvasBg, (v) => localStorage.setItem("canvas:bg", v));

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
const config = useRuntimeConfig();
let stompClient: Client;

onMounted(() => {
  const socket = new SockJS(`${config.public.NUXT_PUBLIC_API_BASE}/ws`);
  stompClient = new Client({
    webSocketFactory: () => socket,
    onConnect: () => console.log("Connected to WebSocket"),
  });
  stompClient.activate();

  window.addEventListener("keydown", (e) => {
    if (e.key === "Escape") selectedPageId.value = null;
  });
});

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

const deletePage = async (page: WebPage) => {
  const ok = await $modal.confirm({
  title: 'Delete page',
  html: `<p>Are you sure you want to delete '${page.label}'?</p>`,
  confirmText: 'Confirm',
  cancelText: 'Cancel',
  variant: 'default'
})
if (!ok){return;}
    if (page.generatedPageId === props.project.indexPage) {
      alert(
        "This page is the main public main/index page, please change to another one before deleting"
      );
      await $modal.alert({ 
        title: 'Project successfully craeted!', 
        html: `</div>This page is the main public main/index page, please change to another one before deleting.</p>`,
         variant: 'danger' 
        });
      return;
    }
    try {
      loadingOnDeletePage.value = true;
      await api.delete(
        `${config.public.NUXT_PUBLIC_API_BASE}/page/delete/${page.generatedPageId}`
      );
     await props.getProject();
      await props.getPages();
      await $modal.alert({ 
        title: 'Page successfully deleted!', 
        html: `<p>Page got deleted successfully!"</p>`, 
        variant: 'success' 
      });
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
         await $modal.alert({ 
            title: 'Error',
             html: error?.response?.data?.message || "An error occurred",
             variant: 'danger' 
            });
      } else {
          await $modal.alert({ 
            title: 'Error',
             html: "An unexpected error occurred.",
             variant: 'danger' 
            });
      }
    } finally {
      loadingOnDeletePage.value = false;
    }
};

watch(
  () => props.selectedPageFromParent,
  (value) => {
    if (!value) return;
    openPreview(value.generatedPageId);
  }
);

const onChangePath = (generatedPageId: string) => {
  const found = webPages.find(
    (page) => page.generatedPageId === generatedPageId
  );
  if (!found) {
    alert("No page found");
    return;
  }
  selectedPage.value = found;
};

const openPreview = (generatedPageId: string) => {
  const found = webPages.find(
    (page) => page.generatedPageId === generatedPageId
  );
  if (!found) {
    alert("No page found");
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
  if (draggedPage) sendUpdateDebounced(draggedPage);
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
  if (resizingPage) sendUpdateDebounced(resizingPage);
  resizingPage = null;
  window.removeEventListener("mousemove", onResize);
  window.removeEventListener("mouseup", stopResize);
};

// Persist page updates
const sendUpdate = (page: {
  generatedPageId: string;
  left: number;
  top: number;
  width: number;
  height: number;
}) => {
  if (!stompClient || !stompClient.connected) return;
  const token = getToken();

  stompClient.publish({
    destination: "/app/update-page",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify({
      generatedPageId: page.generatedPageId,
      left: page.left,
      top: page.top,
      width: page.width,
      height: page.height,
    }),
  });
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
const sendUpdateDebounced = debounce(sendUpdate, 200);
</script>
