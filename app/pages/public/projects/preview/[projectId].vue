<!-- /pages/public/projects/[projectId]/[indexPage].vue -->
<template>
  <div class="min-h-screen bg-[#0D1117] text-white flex flex-col">
    <!-- Topbar (normal flow, not fixed/absolute; sits above content and renders in front) -->
    <header
      v-if="!hiddenTheHeader"
      class="relative bg-[#0D1117] border-b border-white/10"
    >
      <div class="px-4 py-3 grid grid-cols-2 lg:grid-cols-3 items-center gap-3">
        <!-- Left: Back + Title -->
        <div class="flex items-center gap-3 min-w-0">
          <a
            :href="`${back as string}`"
            class="text-sm opacity-80 hover:opacity-100 whitespace-nowrap"
            >← Back</a
          >
          <h1
            class="text-base sm:text-lg font-semibold truncate max-w-[50vw] sm:max-w-[40vw]"
          >
            {{ project.title || "Project" }}
          </h1>
        </div>

        <!-- Center: Tabs -->
        <nav
          class="hidden ml-auto sm:mx-auto w-full col-span-1 lg:flex justify-center items-center"
        >
          <h2
            class="inline-flex items-center gap-1 rounded-lg border border-white/10 bg-white/5 p-1"
            role="tablist"
            aria-label="Preview mode"
          >
            <button
              class="px-3 py-1.5 text-sm rounded-md transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
              :class="
                activeTab === 'preview'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-300 hover:bg-white/10 cursor-pointer'
              "
              role="tab"
              :aria-selected="activeTab === 'preview'"
              @click="setTab('preview')"
            >
              UX/UI Preview
            </button>
            <button
              class="px-3 py-1.5 text-sm rounded-md transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
              :class="
                activeTab === 'canvas'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-300 hover:bg-white/10 cursor-pointer'
              "
              role="tab"
              :aria-selected="activeTab === 'canvas'"
              @click="setTab('canvas')"
            >
              Canvas
            </button>
          </h2>
        </nav>

        <!-- Right: Controls (UserProfile is in normal header flow, in front of everything) -->
        <div
          class="flex items-center gap-3 ml-auto order-2 sm:order-none justify-end"
        >
          <button
            @click="onRemix(project.projectId)"
            class="border border-white/10 bg-white/5 px-3 py-1.5 text-sm rounded-lg transition text-slate-300 hover:bg-white/20 cursor-pointer"
          >
            <div class="inline-flex items-center gap-2">
              <UIcon name="i-lucide-layers" class="size-4" />
              Remix
            </div>
          </button>
          <UserProfile :user="props.user" />
        </div>
      </div>

      <nav
        class="lg:hidden ml-auto sm:mx-auto w-full col-span-2 lg:col-span-1 flex justify-center"
      >
        <div
          class="inline-flex items-center gap-1 rounded-lg border border-white/10 bg-white/5 p-1"
          role="tablist"
          aria-label="Preview mode"
        >
          <button
            class="px-3 py-1.5 text-sm rounded-md transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
            :class="
              activeTab === 'preview'
                ? 'bg-indigo-600 text-white'
                : 'text-slate-300 hover:bg-white/10 cursor-pointer'
            "
            role="tab"
            :aria-selected="activeTab === 'preview'"
            @click="setTab('preview')"
          >
            UX/UI Preview
          </button>
          <button
            class="px-3 py-1.5 text-sm rounded-md transition focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
            :class="
              activeTab === 'canvas'
                ? 'bg-indigo-600 text-white'
                : 'text-slate-300 hover:bg-white/10 cursor-pointer'
            "
            role="tab"
            :aria-selected="activeTab === 'canvas'"
            @click="setTab('canvas')"
          >
            Canvas
          </button>
        </div>
      </nav>
    </header>

    <!-- Content -->
    <main class="flex-1 w-full relative isolate">
      <!-- Floating controls (bottom-right) — Preview only -->
      <div
        v-if="activeTab === 'preview'"
        class="fixed bottom-4 right-4 md:bottom-6 md:right-6 z-20 pointer-events-auto"
        :style="{
          marginBottom: 'env(safe-area-inset-bottom)',
          marginRight: 'env(safe-area-inset-right)',
        }"
      >
        <div
          class="flex items-center gap-2 sm:gap-3 rounded-xl border border-white/10 bg-[#0D1117]/90 backdrop-blur px-2.5 py-2 sm:px-3 shadow-lg z-0 m-2"
          role="group"
          aria-label="Preview controls"
        >
          <!-- Page select -->
          <label class="sr-only" for="page-select">Select page</label>
          <select
            id="page-select"
            class="h-9 min-w-[10rem] sm:min-w-[12rem] max-w-[65vw] truncate rounded-lg border border-white/10 bg-[#0D1117] px-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
            v-model="selectedPageId"
          >
            <option
              v-for="page in project.pages"
              :key="page.generatedPageId"
              :value="page.generatedPageId"
            >
              {{ page.path }}
            </option>
          </select>

          <!-- Divider -->
          <div class="hidden sm:block w-px h-6 bg-white/10"></div>

          <!-- Fit toggle -->
          <label
            class="inline-flex items-center gap-2 cursor-pointer select-none"
          >
            <input
              type="checkbox"
              class="accent-indigo-500"
              v-model="fitToWidth"
            />
            <span class="text-xs sm:text-sm opacity-90">Fit to width</span>
          </label>

          <!-- Divider -->
          <div class="hidden sm:block w-px h-6 bg-white/10"></div>

          <!-- Fit toggle -->
          <label
            class="inline-flex items-center gap-2 cursor-pointer select-none"
          >
            <input
              type="checkbox"
              class="accent-indigo-500"
              v-model="hiddenTheHeader"
            />
            <span class="text-xs sm:text-sm opacity-90">Hidden the header</span>
          </label>
        </div>
      </div>

      <!-- PREVIEW TAB -->
      <section
        v-if="activeTab === 'preview'"
        class="w-full flex justify-center z-0"
        :class="fitToWidth ? '' : 'p-4 sm:p-6'"
      >
        <div class="relative w-full" :style="containerStyle">
          <!-- Skeleton while loading -->
          <div
            v-if="frameLoading"
            class="absolute inset-0 animate-pulse bg-gradient-to-br from-white/5 to-white/0 rounded-none sm:rounded border border-white/10"
          ></div>

          <iframe
            v-show="!frameLoading"
            :srcdoc="selectedPreviewPage?.htmlContent"
            title="Project Preview"
            :style="iframeStyle"
            class="block bg-white relative z-10"
            sandbox="allow-scripts allow-same-origin"
            referrerpolicy="no-referrer"
            @load="onFrameLoad"
          />
        </div>
      </section>

      <!-- CANVAS TAB -->
      <section v-else class="p-0 sm:p-2">
        <PublicPageCanvas
          :project="project"
          :web-pages="project.pages"
          :selected-page-from-parent="selectedPage"
          :on-delete-parent-selected-page="onDeleteSelectedPage"
        />
      </section>
    </main>

    <PageLoader v-if="loadingOnRemix"/>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  watch,
} from "vue";
import { useRoute } from "vue-router";
import type { WebPage } from "~/modules/pageStream";
import type { User } from "~/types/User";
import PublicPageCanvas from "~/components/publicPageCanvas.vue";
import UserProfile from "~/components/userProfile.vue";
import PageLoader from "~/components/ui/PageLoader.vue";

const config = useRuntimeConfig();
const route = useRoute();
const { $modal } = useNuxtApp();
const loadingOnRemix = ref(false);

const props = defineProps<{ user: User }>();

/** State: project */
export type Project = {
  projectId: string;
  title: string;
  description: string;
  type: string;
  tags: string[];
  indexPage: string;
  mainHtmlContent: string;
  pages: WebPage[];
};
const project = reactive<Project>({
  projectId: "",
  title: "",
  description: "",
  type: "",
  tags: [],
  indexPage: "",
  mainHtmlContent: "",
  pages: [],
});

/** Routing */
const projectId = computed(() => route.params.projectId as string);
const back = ref(route.query.back || "/");

/** Tabs */
const activeTab = ref<"preview" | "canvas">("preview");
function setTab(t: "preview" | "canvas") {
  activeTab.value = t;
}

/** Preview controls */
const fitToWidth = ref(true);
const hiddenTheHeader = ref(false);
const NATURAL_WIDTH = 1440;
const NATURAL_HEIGHT = 900;
const vh = ref(0);
const frameLoading = ref(true);

const selectedPageId = ref<string>(""); // generatedPageId
const selectedPreviewPage = computed(() =>
  project.pages.find((p) => p.generatedPageId === selectedPageId.value)
);
/** Canvas selection */
const selectedPage = ref<null | WebPage>(null);
const onDeleteSelectedPage = () => {
  selectedPage.value = null;
};
const canonicalUrl = computed(() => config.public.SITE_URL + route.fullPath)

function clip(str = '', n = 170) {
  const s = str.replace(/\s+/g, ' ').trim()
  return s.length <= n ? s : s.slice(0, n - 1).trimEnd() + '…'
}

const seoTitle = computed(() =>
  project.title
    ? `${project.title} – Live Preview & Canvas | Keetlo`
    : 'Project Preview – Explore & Remix | Keetlo'
)

const seoDesc = computed(() =>
  project.description
    ? clip(project.description, 170)
    : 'Explore a public, AI-generated HTML project on Keetlo. Switch between a pixel-perfect UX/UI preview and an interactive canvas, browse individual pages, inspect clean markup, and instantly remix the project into your workspace.'
)

// Prefer a dynamic OG image if your backend provides one; fall back to a static image.
const ogImage = computed(() =>
  `${config.public.SITE_URL}/images/og/public-project.png`
)

useSeoMeta({
  // Core
  title: seoTitle,
  description: seoDesc,
  robots: 'index,follow,max-image-preview:large',

  // Open Graph
  ogUrl: canonicalUrl,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: seoTitle,
  ogDescription: seoDesc,
  ogImage,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: seoTitle,
  twitterDescription: seoDesc,
  twitterImage: ogImage,
})

// Canonical + JSON-LD
useHead(() => ({
  link: [{ rel: 'canonical', href: canonicalUrl.value }],
  script: [
    {
      type: 'application/ld+json',
      children: JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'CreativeWork',
        name: project.title || 'Project Preview',
        description: seoDesc.value,
        url: canonicalUrl.value,
        isAccessibleForFree: true,
        publisher: { '@type': 'Organization', name: 'Keetlo', url: config.public.SITE_URL },
      }),
    },
  ],
}))

const onRemix = async (projectId: string) => {
  if(!projectId){return}
 const ok = await $modal.confirm({
  title: 'Remix this project to create a safe, independent copy?',
  html: '<p>We’ll duplicate the entire project so you can explore new ideas without risking the original. All settings and pages will be preserved, and you can iterate freely on the copy.</p>',
  confirmText: 'Create the remix',
  cancelText: 'Keep the original as is',
  variant: 'default'
})
if (!ok){return;}
  try{
    loadingOnRemix.value = true;
    const response = await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${projectId}/remix`);
    const newProjectId = response.data.projectId;
    const successOk = await $modal.alert({ 
      title: 'Project successfully craeted!', 
      html: `<p>Project got remixed.</p>`, 
      variant: 'success' 
    });
    const newProjectPath =  `/account/projects/${newProjectId}`;
    if(successOk){
      window.location.href = newProjectPath;
    }
    setTimeout(()=>{
      window.location.href = newProjectPath;
    }, 1500)

  }catch (error : unknown) {
     if (axios.isAxiosError(error)) {
         if (error.status === 401) {
          window.location.href = `/auth/login?next=${window.location.href}`;
         } else {
           await $modal.alert({ 
            title: 'Remix failed!',
             html: error?.response?.data?.message? 
             error?.response?.data?.message: 
             "Can not remix the project, please try again", 
             variant: 'danger' 
            });
         }
    } else {
         await $modal.alert({ 
            title: 'Remix failed!',
             html:"Can not remix the project, please try again",
             variant: 'danger' 
            });
    }
  } finally {
    loadingOnRemix.value = false
  }
}

/** Layout sizing */
function updateVH() {
  vh.value = window.innerHeight || document.documentElement.clientHeight || 800;
}

const containerStyle = computed(() => {
  if (fitToWidth.value) return { width: "100%" };
  return { width: "100%", display: "flex", justifyContent: "center" };
});

const iframeStyle = computed(() => {
  if (fitToWidth.value) {
    return { width: "100%", height: `${vh.value}px` };
  }
  return {
    width: `${NATURAL_WIDTH}px`,
    height: `${NATURAL_HEIGHT}px`,
    maxWidth: "100%",
  };
});

/** Data fetching */
async function getProject() {
  try {
    const { data } = await axios.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${projectId.value}`
    );
    Object.assign(project, data);
    // Init selection: use indexPage if present; else first page
    const initial =
      (data.pages || []).find(
        (p: WebPage) => p.generatedPageId === data.indexPage
      ) || data.pages?.[0];
    selectedPageId.value = initial?.generatedPageId || "";
    frameLoading.value = false;
  } catch (error) {
    window.location.href = back.value as string;
  }
}

/** React when page changes */
watch(selectedPageId, () => {
  frameLoading.value = true;
});

/** Iframe load handler */
function onFrameLoad() {
  frameLoading.value = false;
  const frame = document.querySelector(
    'iframe[title="Project Preview"]'
  ) as HTMLIFrameElement | null;
  if (!frame || !frame.contentDocument) return;
  const doc = frame.contentDocument;
  const base = doc.createElement("base");
  base.setAttribute("target", "_self");
  doc.head?.prepend(base);
  doc.addEventListener(
    "click",
    (ev) => {
      const a = (ev.target as HTMLElement)?.closest(
        "a"
      ) as HTMLAnchorElement | null;
      if (a && a.getAttribute("href") === "#") ev.preventDefault();
    },
    { capture: true }
  );
}

/** Mount/unmount */
onMounted(() => {
  getProject();
  updateVH();
  window.addEventListener("resize", updateVH, { passive: true });
});
onBeforeUnmount(() => {
  window.removeEventListener("resize", updateVH);
});
</script>
