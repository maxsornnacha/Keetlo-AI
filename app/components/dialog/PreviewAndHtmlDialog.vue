<!-- eslint-disable vue/no-dupe-keys -->
<template>
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="selectedPage && type === 1"
      class="fixed inset-0 z-[9999] flex flex-col"
      role="dialog"
      aria-modal="true"
      @click.self="closePreview"
    >
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/70" @click.self="closePreview"/>

      <!-- Panel -->
      <div
        class="relative z-10 mx-auto my-4 flex w-[min(1200px,95vw)] flex-1 flex-col overflow-hidden rounded-2xl border border-white/10 bg-[#0D1117]/95 shadow-2xl ring-1 ring-white/10"
      >
        <!-- Header / Title bar -->
        <div
          class="flex items-center justify-between gap-3 px-4 py-3 sm:px-6 sticky top-0 bg-gray-50 backdrop-blur supports-[backdrop-filter]:bg-gray-50 border-b border-gray-200"
        >
          <div class="min-w-0">
            <div class="text-xs text-indigo-500 inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-gray-100 px-2.5 py-1">
              <UIcon name="i-lucide-file-code" class="size-4" />
              Preview & Code
            </div>
            <h2 class="mt-1 truncate text-lg font-semibold text-black">
              {{ selectedPage.label.toUpperCase() }}
            </h2>
          </div>

          <div class="flex items-center gap-2">
            <!-- Tabs -->
            <div class="hidden sm:flex rounded-xl border border-gray-200 bg-gray-100">
              <button
                class="px-3 py-1.5 text-sm rounded-lg transition"
                :class="activeTab === 'preview'
                  ? 'bg-gray-200 text-black'
                  : 'cursor-pointer text-gray-500 hover:bg-gray-100'"
                @click="onChangeActiveTab('preview')"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-maximize-2" class="size-4" />
                  Preview
                </div>
              </button>
              <button
                class="px-3 py-1.5 text-sm rounded-lg transition"
                :class="activeTab === 'code'
                   ? 'bg-gray-200 text-black'
                  : 'cursor-pointer text-gray-500 hover:bg-gray-100'"
                @click="onChangeActiveTab('code')"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-code-xml" class="size-4" />
                  HTML
                </div>
              </button>
            </div>

            <!-- Close -->
            <button
              class="cursor-pointer rounded-md pt-2 px-2 pb-1 text-black hover:bg-gray-200 transition"
              aria-label="Close preview"
              @click="closePreview"
            >
              <UIcon name="i-lucide-x" class="size-5 " />
            </button>
          </div>
        </div>

        <!-- Toolbar -->
        <div class="px-4 sm:px-6 py-2 flex flex-wrap items-center gap-3 border-b border-gray-200 bg-gray-50">
          <div class="flex items-center gap-2 text-sm text-gray-700">
            <span class="hidden sm:inline">Path Directory:</span>
            <select
              :value="selectedPath"
              class="h-9 min-w-[12rem] rounded-lg border border-gray-200 bg-white px-3 text-sm text-black focus:outline-none focus:border focus:border-indigo-500 cursor-pointer"
              @change="emit('update:selectedPath', ($event.target as HTMLSelectElement).value)"
            >
              <option
                v-for="page in webPages"
                :key="page.generatedPageId"
                :value="page.generatedPageId"
              >
                {{ page.path }}
              </option>
            </select>
          </div>

          <!-- Mobile tabs fallback -->
          <div class="sm:hidden ml-auto inline-flex rounded-xl border border-gray-200 bg-gray-100">
            <button
              class="px-3 py-1 text-sm rounded-md transition"
              :class="activeTab === 'preview'
                ? 'bg-gray-200 text-black'
                  : 'cursor-pointer text-gray-500 hover:bg-gray-100'"
              @click="onChangeActiveTab('preview')"
            >   <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-maximize-2" class="size-4" />
                  Preview
                </div></button>
            <button
              class="px-3 py-1 text-sm rounded-md transition"
              :class="activeTab === 'code'
                  ? 'bg-gray-200 text-black'
                  : 'cursor-pointer text-gray-500 hover:bg-gray-100'"
              @click="onChangeActiveTab('code')"
            >
               <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-code-xml" class="size-4" />
                  HTML
                </div></button>
          </div>

          <div class="ml-auto hidden sm:block text-xs text-gray-400">
            Tip: <kbd class="rounded bg-white/10 px-1">Esc</kbd> to close
          </div>
        </div>

        <!-- Content area -->
        <div class="flex-1 overflow-hidden">
          <!-- Preview -->
          <iframe
            v-show="activeTab === 'preview'"
            :srcdoc="selectedPage.htmlContent"
            class="h-full w-full border-0 bg-white"
            sandbox="allow-scripts allow-same-origin" 
            referrerpolicy="no-referrer"
            @load="lockIframe"
          />

          <!-- Code -->
          <div v-show="activeTab === 'code'" class="flex h-full flex-col">
            <div class="flex-1 overflow-auto">
              <CodeViewer :html="prettyHtml" />
            </div>
            <div class="border-t border-white/10 bg-gray-50 px-4 py-3 sm:px-6">
              <div class="flex justify-end">
                <button
                  class="cursor-pointer inline-flex items-center gap-2 rounded-lg bg-indigo-500 px-4 py-2 text-sm text-white hover:bg-indigo-600 active:scale-[.99] transition"
                  @click="copyHTML(prettyHtml)"
                >
                  <UIcon name="i-lucide-clipboard-copy" class="size-4" />
                  Copy HTML
                </button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </Transition>
</template>
<script setup lang="ts">
import type { WebPage } from '~/modules/pageStream';
import CodeViewer from '../CodeViewer.vue';
const { $modal } = useNuxtApp();

const props = defineProps<{
    type: number;
    selectedPage: WebPage | null;
    activeTab: string;
    selectedPath: string | null;
    webPages: WebPage[];
    openPreview: (generatedPageId: string)=>void;
    closePreview: ()=>void;
    onChangeActiveTab: (tab: "preview" | "code") => void;
}>();
const emit = defineEmits<{
  (e: 'update:selectedPath', value: string | null): void
}>()
const { selectedPage, selectedPath, webPages, closePreview } = toRefs(props);
const prettyHtml = ref<string>('');

const { formatHTML } = useFormatHTML()

watch(
  selectedPage,
     (value) => {
    if (!value?.htmlContent) {
      prettyHtml.value = ''
      return
    }
    prettyHtml.value = formatHTML(value.htmlContent)
  },
  { immediate: true }
)


const copyHTML = (htmlContent: string) => {
  navigator.clipboard
    .writeText(htmlContent)
    .then(async () => {
      await $modal.alert({ 
      title: 'Success', 
      html: `<p>HTML copied!</p>`, 
      variant: 'success' 
      });
    })
    .catch(async () =>{
     await $modal.alert({ 
      title: 'Error', 
      html: `<p>Failed to copy</p>`, 
      variant: 'danger' 
      });
    });
};
</script>