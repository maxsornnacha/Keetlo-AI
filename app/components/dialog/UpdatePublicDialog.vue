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
      v-if="isOpen"
      class="fixed inset-0 z-50 flex items-center justify-center"
      aria-label="Update Project Public Status"
      role="dialog"
      aria-modal="true"
      @click.self="close"
    >
      <!-- Ambient overlay -->
      <div class="absolute inset-0 bg-black/70"/>

      <!-- Panel -->
      <div
        class="bg-gray-50 relative w-full max-w-2xl rounded-2xl border border-gray-200 p-0 text-gray-700 shadow-2xl ring-1 ring-gray-200"
      >
        <!-- Thin accent line -->
        <div class="absolute inset-x-0 -top-px h-px bg-gradient-to-r from-transparent via-white/40 to-transparent"/>

        <!-- Header -->
        <div class="flex items-start justify-between gap-4 px-6 pt-6">
          <div class="space-y-1">
            <div class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-gray-100 px-2.5 py-1 text-xs text-indigo-500">
              <UIcon name="i-lucide-globe" class="size-4" />
              Visibility
            </div>
            <h2 class="text-xl text-black leading-tight">Update Project Public Status</h2>
            <p class="text-sm text-gray-400">
              Control whether your project is publicly accessible and choose which page is the main entry point.
            </p>
          </div>

            <button
              class="group cursor-pointer rounded-md px-2 pt-2 pb-1 hover:bg-gray-200 text-black transition"
              aria-label="Close dialog"
              @click="close"
            >
              <UIcon name="i-lucide-x" class="size-5" />
            </button>
        </div>

        <!-- Body -->
        <div class="px-6 pb-6 pt-4 max-h-[70dvh] overflow-auto">
          <!-- Public toggle -->
          <div class="rounded-xl border border-gray-200 bg-gray-100 p-4">
            <div class="flex items-center justify-between gap-4">
              <div class="space-y-0.5">
                <div class="flex items-center gap-2">
                  <UIcon
                        :name="isPublicLocal ? 'i-lucide-globe' : 'i-lucide-lock'" class="size-5 text-gray-700"
                         :class="isPublicLocal ? 'text-emerald-500' : 'text-gray-300'"/>
                  <span class="font-medium">
                    {{ isPublicLocal ? 'Project is Public' : 'Project is Private' }}
                  </span>
                </div>
                <p class="text-sm text-gray-400">
                  {{ isPublicLocal
                    ? 'Anyone with the link can view the public index page.'
                    : 'Only you can access this project. It will not be publicly visible.'
                  }}
                </p>
              </div>

              <!-- Pretty switch (keeps v-model intact) -->
              <label class="relative inline-flex cursor-pointer items-center">
                <input v-model="isPublicLocal" type="checkbox" class="sr-only peer">
                <div
                  class="h-7 w-12 rounded-full bg-gray-600/60 transition peer-checked:bg-emerald-500/70 ring-1 ring-inset ring-gray-200"
                />
                <span
                  class="pointer-events-none absolute left-1 top-1 h-5 w-5 rounded-full bg-white shadow
                         transition-transform peer-checked:translate-x-5"
                />
              </label>
            </div>

            <!-- Warning when making public -->
            <div
              v-if="isPublicLocal"
              class="mt-4 inline-flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-100 px-3 py-2 text-amber-500"
            >
              <UIcon name="i-lucide-alert-triangle" class="mt-0.5 size-4" />
              <p class="text-xs leading-relaxed">
                Public projects can be viewed by anyone with the link. Be sure your index page doesn’t contain sensitive information.
              </p>
            </div>
          </div>

          <!-- Index page selector -->
          <div class="mt-5">
            <div class="mb-2 flex items-center justify-between">
              <label class="font-medium">Select Main Page (Index Page)</label>
              <!-- Fallback select (keeps your binding intact) -->
              <select
                v-model="selectedIndexPageLocal"
                class="hidden md:block rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:border focus:border-indigo-500 cursor-pointer"
              >
                <option value="" disabled>Select a page</option>
                <option
                  v-for="page in webPages"
                  :key="page.generatedPageId"
                  :value="page.generatedPageId"
                >
                  {{ page.label }}
                </option>
              </select>
            </div>

            <!-- Visual picker (radio cards) -->
            <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <label
                v-for="page in webPages"
                :key="page.generatedPageId"
                class="group relative cursor-pointer overflow-hidden rounded-xl border border-gray-200 bg-gray-100 p-3 transition
                       hover:border-indigo-500"
              >
                <input
                  v-model="selectedIndexPageLocal"
                  class="peer sr-only"
                  type="radio"
                  :value="page.generatedPageId"
                >
                <div class="flex items-center gap-3">
                  <!-- tiny live preview -->
                  <div class="relative h-14 w-20 overflow-hidden rounded-md border border-gray-200 bg-white">
                    <iframe
                      :srcdoc="page.htmlContent"
                      class="absolute left-0 top-0 h-[1000px] w-[1200px] origin-top-left scale-[0.06] pointer-events-none"
                      sandbox="allow-scripts allow-same-origin" 
                      referrerpolicy="no-referrer"
                       @load="lockIframe"
                    />
                  </div>
                  <div class="min-w-0">
                    <div class="truncate text-sm font-medium text-black">{{ page.label }}</div>
                    <div class="text-xs text-gray-400">{{ page.path }}</div>
                  </div>
                </div>

                <!-- check mark -->
                <div
                  class="pointer-events-none absolute right-3 top-3 hidden rounded-full bg-emerald-500 px-1 pt-1 text-white ring-1 ring-emerald-500/30 peer-checked:block"
                >
                  <UIcon name="i-lucide-check" class="size-4" />
                </div>
              </label>
            </div>

            <p class="mt-2 text-xs text-gray-400">
              The selected page becomes your public landing page.
            </p>
          </div>
        </div>

        <!-- Footer actions (sticky inside panel) -->
        <div class="sticky bottom-0 flex items-center justify-end gap-2 rounded-b-2xl border-t border-gray-200 px-6 py-4 backdrop-blur-sm">
          <button
            class="min-w-[120px] cursor-pointer rounded-lg border border-gray-200 px-4 py-2 text-sm hover:bg-gray-200 transition"
            @click="close"
          >
            Cancel
          </button>
          <button
            class="min-w-[120px] cursor-pointer rounded-lg bg-indigo-500 px-4 py-2 text-sm text-white hover:bg-indigo-600 active:scale-[.99] transition"
            @click="save"
          >
            Save changes
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>


<script setup lang="ts">
import type { WebPage } from "~/modules/pageStream";
import { ref, watch } from "vue";
const { $modal } = useNuxtApp();

const props = defineProps<{
  isOpen: boolean;
  isPublic: number;
  indexPage: string | null;
  webPages: WebPage[];
}>();

const emit = defineEmits<{
  (e: "update:isOpen", value: boolean): void;
  (e: "save", payload: { isPublic: number; indexPage: string }): void;
}>();

const isPublicLocal = ref(props.isPublic === 1 ? true : false);
const selectedIndexPageLocal = ref(props.indexPage);

watch(
  () => props.isPublic,
  (v) => (isPublicLocal.value = v === 1 ? true : false)
);
watch(
  () => props.indexPage,
  (v) => (selectedIndexPageLocal.value = v)
);

const close = () => emit("update:isOpen", false);

const save = async () => {
  if (!selectedIndexPageLocal.value) {
    await $modal.alert({ 
      title: 'Warning', 
      html: `<p>Please select a main page (Index Page).</p>`, 
      variant: 'danger' 
    });
    return;
  }
  emit("save", {
    isPublic: isPublicLocal.value ? 1 : 0,
    indexPage: selectedIndexPageLocal.value,
  });
  close();
};
</script>
