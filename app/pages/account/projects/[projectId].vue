<!-- eslint-disable vue/no-v-html -->
<template>
  <div v-if="!loadingProject" class="relative flex h-screen">
    <!-- LEFT: Chat / Controls -->
    <section
      class="flex flex-col backdrop-blur"
      :style="{ width: isCollapsed ? 0 : leftWidth + 'px' }"
    >
      <!-- Header -->
      <ProjectHeader
        :user="props.user"
        :project="project"
        :download-project="downloadProject"
        :on-open-change-public-status-dialog="onOpenChangePublicStatusDialog"
        :open-detail="openDetail"
        :open-edit="openEdit"
        :on-delete="onDelete"
        :class="isCollapsed ? 'hidden' : ''"
      />

      <!-- Messages -->
      <div
        ref="messagesContainer"
        class="flex-1 overflow-y-auto px-4 py-6 lg:px-5 lg:py-6"
      >
        <div
          v-for="(message, index) in project?.messages"
          :key="message?.projectMessageId"
          class="relative mb-6 space-y-3"
        >
          <!-- Meta row -->
          <div
            class="flex items-center gap-3"
            :class="message.role === 'USER' ? 'justify-start' : 'justify-start'"
          >
            <nuxt-img
              v-if="message.role === 'USER'"
              :src="
                !props.user.avatarUrl
                  ? '/images/accounts/account.jpg'
                  : isValidUrl(props.user.avatarUrl)
                  ? props.user.avatarUrl
                  : `${config.public.NUXT_PUBLIC_API_BASE}${props.user.avatarUrl}`
              "
              class="h-8 w-8 rounded-full object-cover ring-1 ring-white/10"
            />
            <nuxt-img
              v-else
              src="/images/accounts/ai.png"
              class="h-8 w-8 rounded-full object-cover ring-1 ring-white/10 bg-yellow-500"
            />
            <p
              class="text-[11px] font-medium uppercase tracking-wide text-gray-500"
            >
              <span v-if="message.role === 'USER'">
                {{ props.user.firstname }} {{ props.user.lastname }}
              </span>
              <span v-else>Keetlo Assistant AI</span>
            </p>
          </div>

          <!-- Bubble -->
          <div
            class="max-w-[42rem] rounded-2xl border px-4 py-3 text-black"
            :class="
              message.role === 'USER'
                ? 'bg-gray-100 border-gray-200'
                : 'bg-gray-200 border-gray-200'
            "
          >
            <div class="prose prose-invert prose-sm max-w-none">
              <p class="whitespace-pre-wrap" v-html="message.message" />
            </div>

            <!-- typing spinner for last msg -->
            <div
              v-if="
                loadingSendMessage && index === project?.messages.length - 1
              "
              class="mt-2 flex items-center gap-2 text-gray-700"
            >
              <UIcon
                name="i-lucide-loader-circle"
                class="size-4 animate-spin"
              />
              <span class="text-xs">Thinking…</span>
            </div>
          </div>

          <!-- Generated page thumbs -->
          <div
            v-if="
              loadingCreatePages != null ||
              webPages.filter(
                (p) => p.projectMessageId === message?.projectMessageId
              ).length > 0
            "
            class="flex flex-wrap items-center gap-3"
          >
            <template
              v-for="page in webPages.filter(
                (p) => p.projectMessageId === message?.projectMessageId
              )"
              :key="page.generatedPageId"
            >
              <div
                class="border border-gray-50/20 rounded-md transition hover:scale-[1.02]"
              >
                <button
                  class="cursor-pointer relative h-[108px] w-[108px] overflow-hidden rounded-lg bg-white shadow"
                  @click="onPageSelection(page)"
                >
                  <iframe
                    :srcdoc="page.htmlContent"
                    class="absolute left-0 top-0 h-[1200px] w-[1200px] origin-top-left scale-[0.09]"
                    style="pointer-events: none"
                    sandbox="allow-scripts allow-same-origin"
                    referrerpolicy="no-referrer"
                    @load="lockIframe"
                  />
                  <div
                    class="pointer-events-none absolute inset-0 rounded-lg ring-1 ring-black/5"
                  />
                </button>
              </div>
            </template>

            <!-- Loading placeholder while creating -->
            <div
              v-if="
                loadingCreatePages != null &&
                index === project?.messages.length - 1
              "
              class="relative h-[108px] w-[108px] animate-pulse overflow-hidden rounded-lg border border-gray-200 bg-gray-100"
            >
              <div
                class="absolute inset-0 bg-gradient-to-r from-transparent via-black/20 to-transparent animate-[shimmer_1.3s_infinite]"
              />
            </div>
          </div>

          <!-- Create pages CTA (only for last AI message) -->
          <div
            v-if="
              index === project?.messages.length - 1 &&
              message.role === 'AI' &&
              !loadingSendMessage
            "
            class="pt-1"
          >
            <div
              v-if="loadingCreatePages !== null && progressCreatePages.status"
              class="mb-3"
            >
              <ProgressBarPercentage
                :progress="progressCreatePages.progress"
                :label="progressCreatePages.label"
              />
            </div>

            <button
              v-if="
                ((props.user.leftRequests !== null &&
                  props.user.leftRequests > 0) ||
                  props.user.limitRequests === 0) &&
                !webPages.find(
                  (page) => page.projectMessageId === message.projectMessageId
                )
              "
              :disabled="loadingCreatePages != null"
              class="inline-flex min-w-[150px] items-center justify-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-xs font-medium text-gray-700 transition hover:bg-gray-200 disabled:opacity-60"
              :class="!(loadingCreatePages !== null) ? 'cursor-pointer' : ''"
              @click="onCreatePages(message.projectMessageId, 
              (index-1) > 0? 
              (project?.messages[index-1]?.message + ' ' +message.message) 
              : message.message
              )"
            >
              <UIcon
                v-if="loadingCreatePages !== null"
                name="i-lucide-loader-circle"
                class="size-4 animate-spin"
              />
              <span
                v-if="loadingCreatePages !== null && progressCreatePages.status"
              >
                {{ progressCreatePages.label }}
              </span>
              <span v-else>Create Webpages</span>
            </button>
          </div>

          <!-- Divider between older messages -->
          <div
            v-if="index !== project?.messages.length - 1"
            class="mt-2 h-px w-full bg-gradient-to-r from-transparent via-white/10 to-transparent"
          />
        </div>
      </div>

      <!-- Composer -->
      <div class="border-t border-gray-200 px-4 py-3 backdrop-blur">
        <div class="rounded-xl border border-gray-200 bg-white shadow-inner">
          <textarea
            ref="textareaRef"
            v-model="userInput"
            maxlength="2000"
            rows="3"
            class="form-textarea block w-full resize-none rounded-t-xl border-0 bg-transparent px-4 py-3 text-black placeholder:text-gray-500 focus:outline-none focus:border focus:border-indigo-500"
            placeholder="Describe what to build, or ask for changes…"
            @input="autoResize"
            @keydown.enter.exact.prevent="onSendingMessage(userInput)"
            @keydown.meta.enter.prevent="onSendingMessage(userInput)"
            @keydown.ctrl.enter.prevent="onSendingMessage(userInput)"
          />
          <div
            class="flex items-center justify-between gap-3 border-t border-gray-200 px-3 py-2"
          >
            <p class="mt-2 text-xs text-black/50">
              Tip: Press
              <kbd class="rounded bg-white/10 px-1.5 py-0.5">Shift + Enter</kbd>
              for new line,
              <kbd class="rounded bg-white/10 px-1.5 py-0.5">⌘/Ctrl</kbd> +
              <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> Or
              <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> to
              generate. to generate.
            </p>
            <div class="flex flex-col items-end gap-2">
              <button
                class="cursor-pointer inline-flex min-w-[96px] items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm text-white transition hover:bg-indigo-700 disabled:opacity-60"
                :disabled="
                  loadingSendMessage ||
                  loadingCreatePages !== null ||
                  !userInput
                "
                @click="onSendingMessage(userInput)"
              >
                <UIcon
                  v-if="!loadingSendMessage"
                  name="i-lucide-send"
                  class="size-4"
                />
                <UIcon
                  v-else
                  name="i-lucide-loader-circle"
                  class="size-4 animate-spin"
                />
                <span>{{ loadingSendMessage ? "Sending…" : "Send" }}</span>
              </button>
              <p class="text-xs text-gray-500">{{ userInput.length }}/2000</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Resize handle -->
    <div
      class="relative h-full w-[2px] flex-shrink-0 cursor-col-resize group hover:bg-indigo-500 duration-300 transition"
      aria-label="Resize sidebar"
      role="separator"
      :aria-orientation="'vertical'"
      @mousedown.prevent="startResizeLeft"
      @dblclick="resetLeftWidth"
    >
      <!-- visual handle -->
      <div
        class="mx-auto h-full w-[2px] bg-white/10 group-hover:bg-white/20 transition"
      />
      <!-- bigger invisible hit area for easier dragging -->
      <div class="absolute inset-0 -mx-2" />
    </div>

    <!-- RIGHT: Canvas / Preview -->
    <section class="flex-1 overflow-hidden">
      <PageCanvas
        :project="project"
        :web-pages="webPages"
        :selected-page-from-parent="selectedPage"
        :on-delete-parent-selected-page="onDeleteSelectedPage"
        :loading-create-pages="loadingCreatePages"
        :progress-create-pages="progressCreatePages"
        :get-project="getProject"
        :get-pages="getPages"
        :download-project="downloadProject"
        :download-single-page="downloadSinglePage"
        :on-open-change-public-status-dialog="onOpenChangePublicStatusDialog"
        :toggle-chat-display="toggleChatDisplay"
        :chat-display-is-collapsed="isCollapsed"
      />
    </section>

    <!-- PUBLIC STATUS -->
    <UpdatePublicDialog
      v-model:is-open="showUpdateDialog"
      :is-public="project.isPublic"
      :index-page="project.indexPage"
      :web-pages="webPages"
      @save="onUpdatePublic"
    />

    <!-- DETAIL / EDIT -->
    <ProjectDetailDialog
      v-if="project"
      :project="project"
      :is-open="isDetailOpen"
      @on-close="closeDetail"
      @edit="openEdit"
    />
    <EditProjectDialog
      v-if="project"
      :project="project"
      :is-open="isEditOpen"
      @on-close="closeEdit"
      @save="saveEdit"
    />
    <PageLoader
      v-if="
        loadingOnEditProject || loadingOnPublicProject || loadingOnDeleteProject
      "
    />
  </div>
  <div v-else class="flex flex-col justify-center items-center mt-56">
    <UIcon
      name="i-lucide-loader-circle"
      class="size-12 text-indigo-400 animate-spin"
    />
    <p class="mt-3 text-sm text-gray-700">
      Loading chat session {{ projectId }}
    </p>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import ProjectHeader from "~/components/projectHeader.vue";
import type { User } from "~/types/User";
import type { Project } from "~/types/Projects";
import { useUserStore } from "#imports";
import { nextTick, reactive, ref, onMounted, watch } from "vue";
import { streamPages, type WebPage } from "~/modules/pageStream";
import type { PagePosition } from "~/types/PagePostion";
import ProgressBarPercentage from "~/components/ui/ProgressBarPercentage.vue";
import JSZip from "jszip";
import * as FileSaver from "file-saver";
import UpdatePublicDialog from "~/components/dialog/UpdatePublicDialog.vue";
import ProjectDetailDialog from "~/components/dialog/ProjectDetailDialog.vue";
import EditProjectDialog from "~/components/dialog/EditProjectDialog.vue";
import PageLoader from "~/components/ui/PageLoader.vue";

const saveAs = FileSaver.saveAs;
const props = defineProps<{ user: User }>();
const route = useRoute();
const router = useRouter();
const config = useRuntimeConfig();
const projectId = route.params.projectId as string;

const loadingProject = ref(true);
const loadingSendMessage = ref(false);
const loadingCreatePages = ref<PagePosition | null>(null);
const progressCreatePages = ref({
  progress: 0,
  label: "",
  status: false,
});
const userInput = ref("");
const errorMessage = ref<string | null>(null);

const messagesContainer = ref<HTMLDivElement | null>(null);

const userStore = useUserStore();
const project = reactive<Project>({
  projectId: "",
  title: "",
  description: "",
  type: "",
  tags: [],
  messages: [],
  updatedAt: new Date(),
  isPublic: 0,
  indexPage: "",
  mainHtmlContent: "",
});

const webPages = reactive<WebPage[]>([]);
const selectedPage = ref<null | WebPage>(null);
const showUpdateDialog = ref(false);

const isDetailOpen = ref(false);
const isEditOpen = ref(false);

const textareaRef = ref<HTMLTextAreaElement | null>(null);
const loadingOnEditProject = ref(false);
const loadingOnDeleteProject = ref(false);
const loadingOnPublicProject = ref(false);
const loadingOnDownloadProject = ref(false);
const { $modal } = useNuxtApp();

// --- Resizable left panel ---
const MIN_LEFT = 430; // px
const MAX_LEFT = 760; // px
const leftWidth = ref<number>(
  parseInt(localStorage.getItem("left:width") || "520", 10) || 520
);
const dragging = ref(false);

const url = useRequestURL();
const canonicalUrl = computed(
  () => `${config.public.SITE_URL}${url.pathname}${url.search}`
);

const pageTitle = computed(() =>
  project?.title
    ? `${project.title} – Project Editor | Keetlo`
    : `Project Editor – Keetlo`
);

const pageDesc = computed(() => {
  const pageCount = Array.isArray(webPages) ? webPages.length : 0;
  const updated = project?.updatedAt
    ? new Intl.DateTimeFormat(undefined, { dateStyle: "medium" }).format(
        new Date(project.updatedAt)
      )
    : null;

  return [
    `Design, generate, and refine AI-built HTML + Tailwind pages in real time.`,
    pageCount ? `Includes ${pageCount} page${pageCount > 1 ? "s" : ""}.` : null,
    updated ? `Last updated ${updated}.` : null,
  ]
    .filter(Boolean)
    .join(" ");
});

useSeoMeta({
  // Core (dashboard/private page = noindex)
  title: pageTitle.value,
  description: pageDesc.value,
  robots: "noindex,nofollow,noarchive,max-image-preview:large",

  // Open Graph
  ogUrl: canonicalUrl.value,
  ogSiteName: "Keetlo",
  ogType: "website",
  ogTitle: pageTitle.value,
  ogDescription: pageDesc.value,
  ogImage: `${config.public.SITE_URL}/images/og/project-editor.png`,

  // Twitter
  twitterCard: "summary_large_image",
  twitterTitle: pageTitle.value,
  twitterDescription: pageDesc.value,
  twitterImage: `${config.public.SITE_URL}/images/og/project-editor.png`,
});

// Canonical (still helpful for deduping URL variants)
useHead({
  link: [{ rel: "canonical", href: canonicalUrl.value }],
});

const startResizeLeft = () => {
  dragging.value = true;
  document.body.classList.add("select-none", "cursor-col-resize");
  window.addEventListener("mousemove", onResizeLeft);
  window.addEventListener("mouseup", stopResizeLeft, { once: true });
};

const onResizeLeft = (e: MouseEvent) => {
  if (!dragging.value) return;
  const next = Math.min(MAX_LEFT, Math.max(MIN_LEFT, e.clientX)); // clientX from left edge
  leftWidth.value = next;
};

const stopResizeLeft = () => {
  dragging.value = false;
  document.body.classList.remove("select-none", "cursor-col-resize");
  window.removeEventListener("mousemove", onResizeLeft);
  localStorage.setItem("left:width", String(leftWidth.value));
};

// Optional: double-click handle to reset
const resetLeftWidth = () => {
  leftWidth.value = 520;
  localStorage.setItem("left:width", String(leftWidth.value));
};

// Dialog controls
const openDetail = () => {
  isDetailOpen.value = true;
};

const closeDetail = () => {
  isDetailOpen.value = false;
};

const openEdit = () => {
  isDetailOpen.value = false;
  isEditOpen.value = true;
};

const closeEdit = () => {
  isEditOpen.value = false;
};

const onOpenChangePublicStatusDialog = () => {
  showUpdateDialog.value = true;
};

const saveEdit = async (updatedProject: Project) => {
  try {
    loadingOnEditProject.value = true;
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/update`,
      updatedProject
    );
    await $modal.alert({
      title: "Success",
      html: `<p>Projet got updated successfully!</p>`,
      variant: "success",
    });
    await getProject();
    closeEdit();
  } catch (error) {
    if (axios.isAxiosError(error)) {
      await $modal.alert({
        title: "Error",
        html: `<p>${error?.response?.data?.message || "An error occurred"}</p>`,
        variant: "danger",
      });
    } else {
      await $modal.alert({
        title: "Error",
        html: `<p>An unexpected error occurred.</p>`,
        variant: "danger",
      });
    }
  } finally {
    loadingOnEditProject.value = false;
  }
};

// Fetch project data
const getProject = async () => {
  try {
    const response = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/${projectId}`
    );
    Object.assign(project, response.data);
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value =
        error?.response?.data?.message || "An error occurred";
    } else {
      errorMessage.value = "An unexpected error occurred.";
    }
    router.push(`/account/projects`);
  }
};

const getPages = async () => {
  try {
    const response = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/page/list/${projectId}`
    );
    Object.assign(webPages, response.data);
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value =
        error?.response?.data?.message || "An error occurred";
    } else {
      errorMessage.value = "An unexpected error occurred.";
    }
  }
};

// Auto-scroll when messages update
watch(
  () => [project.messages, progressCreatePages],
  async () => {
    await nextTick();
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  },
  { deep: true }
);

watch(
  () => project.messages.length,
  (len, oldLen) => {
    if (len === 1 && oldLen === 0) {
      const firstMsg = project.messages[0];
      if (firstMsg?.role === "USER") {
        onSendingMessage(firstMsg.message, true);
      }
    }
  }
);

function autoResize() {
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = "0px";
  el.style.height = Math.min(el.scrollHeight, window.innerHeight * 0.55) + "px";
}

// Send message & handle AI streaming
const onSendingMessage = async (input: string, firstTry: boolean = false) => {
  if (!input.trim()) {
    return;
  }
  if (
    loadingSendMessage.value ||
    loadingCreatePages.value !== null ||
    (!userInput.value && !firstTry)
  ) {
    return;
  }

  loadingSendMessage.value = true;

  // Add user message
  if (project.messages.length > 1) {
    const userMessage = {
      projectMessageId: Date.now().toString(),
      role: "USER",
      message: input,
      createdAt: new Date(),
    };
    project.messages.push(userMessage);
  }

  const myToken = getToken();
  const inputText = input;
  userInput.value = "";

  try {
    const payloadUser = {
      input: inputText,
      projectId: projectId,
      firstTry: firstTry,
    };
    const res = await fetch(
      `${config.public.NUXT_PUBLIC_API_BASE}/message/user`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${myToken}`,
        },
        body: JSON.stringify(payloadUser),
      }
    );

    await userStore.fetchUser();

    const reader = res.body?.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    if (reader) {
      const aiMessage = {
        projectMessageId: `ai-${Date.now()}`,
        role: "AI",
        message: "",
        createdAt: new Date(),
      };
      project.messages.push(aiMessage);
      const aiIndex = project.messages.length - 1;

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        for (const line of lines) {
          let cleaned = line.replace(/^data:\s*/, "").trim();
          cleaned = cleaned
            .replace(/```json/g, "")
            .replace(/json/g, "")
            .replace(/```/g, "");

          if (cleaned) {
            const words = cleaned.split(" ");
            let delay = 0;
            const speed = 50;

            for (const word of words) {
              setTimeout(() => {
                project.messages[aiIndex]!.message += detectEnglishText(word)
                  ? word + " "
                  : word;
                project.messages[aiIndex]!.message = project.messages[
                  aiIndex
                ]!.message.replace(/&\s*nbsp\s*;/g, "&nbsp;");
              }, delay);
              delay += speed;
            }
          }
        }
      }

      const payloadAi = {
        projectId: projectId,
        role: "AI",
        message: project.messages[aiIndex]!.message,
      };
      if (project.messages[aiIndex]!.message) {
        const response = await api.post(
          `${config.public.NUXT_PUBLIC_API_BASE}/message/ai`,
          payloadAi
        );
        project.messages[aiIndex]!.projectMessageId = response.data;
      }
    }
  } catch (err) {
    console.error("Error sending message:", err);
  } finally {
    setTimeout(() => {
      loadingSendMessage.value = false;
    }, 1000);
  }
};

const handlePagePosition = (index: number) => {
  const CELL_WIDTH = 24; // multiplier for left
  const CELL_HEIGHT = 30; // multiplier for top
  const BASE = 60;
  const COLS = 3; // 3 items per row

  const row = Math.floor(index / COLS);
  const col = index % COLS;

  const result = {
    width: 1200,
    height: 1400,
    top: row === 0 ? BASE : BASE * (row * CELL_HEIGHT), // base + step
    left: col === 0 ? BASE : BASE * (col * CELL_WIDTH), // base + step
  };

  return result;
};

watch(webPages, (value) => {
  if (loadingCreatePages.value != null) {
    loadingCreatePages.value = handlePagePosition(value.length);
    progressCreatePages.value.status = true;
    progressCreatePages.value.progress = 1;
    progressCreatePages.value.label = "Starting page build process";
  }
});

const onCreatePages = async (projectMessageId: string, message: string) => {
  try {
    const myToken = getToken();
    const pagePosition = handlePagePosition(webPages.length);
    loadingCreatePages.value = pagePosition;
    progressCreatePages.value.status = true;
    progressCreatePages.value.progress = 1;
    progressCreatePages.value.label = "Starting page build process";
    let content = "";
    await streamPages(
      message,
      config.public.NUXT_PUBLIC_API_BASE,
      myToken ? myToken : "",
      (streamContent) => {
        content += streamContent;
        if (content.includes("</html")) {
          progressCreatePages.value.progress = 90;
          progressCreatePages.value.label =
            "Page structure complete. Almost ready!";
        } else if (content.trim().includes("</footer")) {
          progressCreatePages.value.progress = 70;
          progressCreatePages.value.label = "Finishing footer section...";
        } else if (content.trim().includes("<footer")) {
          progressCreatePages.value.progress = 60;
          progressCreatePages.value.label = "Building footer section...";
        } else if (content.trim().includes("</main")) {
          progressCreatePages.value.progress = 50;
          progressCreatePages.value.label = "Main content completed!";
        } else if (content.trim().includes("<main")) {
          progressCreatePages.value.progress = 40;
          progressCreatePages.value.label = "Creating main content section...";
        } else if (content.trim().includes("</header")) {
          progressCreatePages.value.progress = 30;
          progressCreatePages.value.label = "Header section completed!";
        } else if (content.trim().includes("<header")) {
          progressCreatePages.value.progress = 20;
          progressCreatePages.value.label = "Starting header section...";
        } else {
          progressCreatePages.value.progress = 10;
          progressCreatePages.value.label = "Initializing page creation...";
        }
      },
      async (page: WebPage) => {
          const pagePosition = handlePagePosition(webPages.length);

          const payload1 = {
            ...page,
            ...pagePosition,
            projectMessageId: projectMessageId,
          };

          progressCreatePages.value.progress = 95;
          progressCreatePages.value.label = "Page is hydrating all the images!";
          const hydratedHtmlContentResponse = await api.post<{
            hydratedHtmlContent: string;
          }>(
            `${config.public.NUXT_PUBLIC_API_BASE}/page/hydrate-images`,
            payload1
          );

          const payload2 = {
            ...page,
            ...pagePosition,
            projectMessageId: projectMessageId,
            htmlContent: hydratedHtmlContentResponse.data.hydratedHtmlContent,
          };

          progressCreatePages.value.progress = 99;
          progressCreatePages.value.label =
            "Page is uploading into Keelo System!";
          const response = await api.post(
            `${config.public.NUXT_PUBLIC_API_BASE}/page/upload`,
            payload2
          );

          page = {
            ...page,
            ...pagePosition,
            projectMessageId: projectMessageId,
            generatedPageId: response.data,
            htmlContent: hydratedHtmlContentResponse.data.hydratedHtmlContent,
          };

          progressCreatePages.value.progress = 100;
          progressCreatePages.value.label = "Page is ready to use!";

          webPages.push(page);
          loadingCreatePages.value = null;
          progressCreatePages.value.status = false;
          setTimeout(() => {
            content = "";
            progressCreatePages.value.progress = 0;
            progressCreatePages.value.label = "";
          }, 1000);
        }
    );
  } catch (error) {
    await $modal.alert({
      title: "Error",
      html: `<p>${error}</p>`,
      variant: "danger",
    });
    loadingCreatePages.value = null;
    progressCreatePages.value.status = false;
  }
};

const onPageSelection = (page: WebPage | null) => {
  selectedPage.value = page;
};

const onDeleteSelectedPage = () => {
  selectedPage.value = null;
};

watch(loadingProject, async (val) => {
  if (!val) {
    await nextTick();
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  }
});

const downloadProject = async () => {
  if (!webPages.length) {
    await $modal.alert({
      title: "Success",
      html: `<p>No pages to download</p>`,
      variant: "success",
    });
    return;
  }

  const zip = new JSZip();

  webPages.forEach((page, index) => {
    const fileName = `${page.label || "page-" + (index + 1)}.html`;
    zip.file(fileName, page.htmlContent);
  });

  try {
    loadingOnDownloadProject.value = true;
    const content = await zip.generateAsync({ type: "blob" });
    saveAs(content, `${project.title || "project"}.zip`);
    await $modal.alert({
      title: "Success",
      html: `<p>Projects downloaded successfully</p>`,
      variant: "success",
    });
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    await $modal.alert({
      title: "Error",
      html: `<p>Failed to download project</p>`,
      variant: "danger",
    });
  } finally {
    loadingOnDownloadProject.value = true;
  }
};

const downloadSinglePage = async (page: WebPage) => {
  if (!page || !page.htmlContent) {
    await $modal.alert({
      title: "Success",
      html: `<p>No content to download</p>`,
      variant: "success",
    });
    return;
  }

  const fileName = `${page.label || "page"}.html`;

  try {
    loadingOnDownloadProject.value = true;
    const blob = new Blob([page.htmlContent], {
      type: "text/html;charset=utf-8",
    });
    saveAs(blob, fileName);
    await $modal.alert({
      title: "Success",
      html: `<p>Page downloaded successfully</p>`,
      variant: "success",
    });
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    await $modal.alert({
      title: "Error",
      html: `<p>Failed to download page</p>`,
      variant: "danger",
    });
  } finally {
    loadingOnDownloadProject.value = false;
  }
};

const onUpdatePublic = async (item: {
  isPublic: number;
  indexPage: string;
}) => {
  try {
    loadingOnPublicProject.value = true;
    const payload = {
      projectId: project.projectId,
      isPublic: item.isPublic,
      indexPage: item.indexPage,
    };
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/update-public`,
      payload
    );
    await getProject();
    await $modal.alert({
      title: "Success",
      html: `<p>Project updated successfully!</p>`,
      variant: "success",
    });
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    await $modal.alert({
      title: "Error",
      html: `<p>Failed to update project.</p>`,
      variant: "danger",
    });
  } finally {
    loadingOnPublicProject.value = false;
  }
};

const onDelete = async (projectId: string) => {
  try {
    loadingOnDeleteProject.value = true;
    const ok = await $modal.confirm({
      title: "Confirm",
      html: "<p>Are you sure you would to delete the project?</p>",
      confirmText: "Delete the project",
      cancelText: "Cancel",
      variant: "default",
    });
    if (!ok) {
      return;
    }
    await api.delete(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/delete/${projectId}`
    );
    const successOk = await $modal.alert({
      title: "Project successfully craeted!",
      html: `<p>Project deleted successfully!</p>`,
      variant: "success",
    });
    if (successOk) {
      router.push("/account/projects");
    }
    setTimeout(() => {
      router.push("/account/projects");
    }, 1500);

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    await $modal.alert({
      title: "Error",
      html: `<p>Failed to delete project.</p>`,
      variant: "danger",
    });
  } finally {
    loadingOnDeleteProject.value = false;
  }
};

const isCollapsed = ref(false);

const toggleChatDisplay = () => {
  isCollapsed.value = !isCollapsed.value;
};

onMounted(async () => {
  await getProject();
  await getPages();
  await nextTick();
  loadingProject.value = false;
});
</script>
