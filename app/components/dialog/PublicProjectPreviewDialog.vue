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
      v-if="selectedProject"
      class="fixed inset-0 z-50 flex flex-col"
      role="dialog"
      aria-modal="true"
      @click.self="closePreview"
    >
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/70" @click.self="closePreview"/>

      <!-- Panel -->
      <div
        class="relative z-10 mx-auto my-4 flex w-[min(1200px,95vw)] flex-1 flex-col overflow-hidden rounded-2xl bg-white shadow-2xl ring-1 ring-white/10"
      >
        <!-- Header / Title bar -->
        <div
          class="flex items-center justify-between gap-3 px-4 py-3 sm:px-6 sticky top-0 lg:border-b lg:border-gray-200"
        >
          <div class="min-w-0">
            <div class="text-xs text-indigo-500 inline-flex items-center gap-2 rounded-lg border border-black/10 bg-black/5 px-2.5 py-1">
              <UIcon name="i-lucide-file-code" class="size-4" />
              Preview
            </div>
            <h2 class="mt-1 truncate text-lg text-black font-semibold">
              {{ props.selectedProject?.title && props.selectedProject?.title.length > 50 ? props.selectedProject?.title.slice(0,50)+"..." : props.selectedProject?.title || "No title"}}
            </h2>
          </div>

          <div class="flex items-center gap-2">
            <!-- Tabs -->
            <div class="hidden lg:flex gap-2 rounded-xl p-1">
              <button
               v-if="props.selectedProject?.projectId"
               class="border border-gray-200 px-3 py-1.5 text-sm rounded-lg transition text-black hover:bg-gray-200 cursor-pointer"
                @click="onRemix(props.selectedProject?.projectId)"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-shuffle" class="size-4" />
                  Remix
                </div>
              </button>
              <a
                class="border border-gray-200 px-3 py-1.5 text-sm rounded-lg transition text-black hover:bg-gray-200 cursor-pointer"
                :href="props.selectedProject?.link + `?back=${route.fullPath}`"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-folder" class="size-4" />
                  View Project
                </div>
              </a>
            </div>

            <!-- Close -->
            <button
              class="cursor-pointer rounded-md px-2 pt-2 pb-1 hover:bg-gray-100 transition"
              aria-label="Close preview"
              @click="closePreview"
            >
              <UIcon name="i-lucide-x" class="size-5" />
            </button>
          </div>
        </div>

        <div class="lg:hidden border-b border-gray-200 py-1 flex justify-center">
               <div class="flex flex-1 gap-2 rounded-xl p-1">
              <button
               class="w-full border border-gray-200 px-3 py-1.5 text-sm rounded-lg transition text-black hover:bg-gray-200 cursor-pointer"
                @click="onRemix(props.selectedProject?.projectId as string)"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-shuffle" class="size-4" />
                  Remix
                </div>
              </button>
              <a
               class="w-full flex justify-center border border-gray-200 px-3 py-1.5 text-sm rounded-lg transition text-black hover:bg-gray-200 cursor-pointer"
                :href="props.selectedProject?.link"
              >
                <div class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-folder" class="size-4" />
                  View Project
                </div>
              </a>
            </div>
        </div>

        <!-- Content area -->
        <div class="flex-1 overflow-hidden">
          <!-- Preview -->
          <iframe
            :srcdoc="props.selectedProject?.mainHtmlContent"
            class="h-full w-full border-0 bg-white"
            sandbox="allow-scripts allow-same-origin" 
            referrerpolicy="no-referrer"
            @load="lockIframe"
          />
        </div>
      </div>
      <PageLoader v-if="loadingOnRemix"/>
    </div>
  </Transition>
</template>
<script setup lang="ts">
import axios from 'axios';
import type { PublicProject } from '~/types/PublicProject';
import PageLoader from '../ui/PageLoader.vue';
const route = useRoute();
const config = useRuntimeConfig();
const props = defineProps<{
    selectedProject: PublicProject | null;
    closePreview: ()=>void;
}>();
const { selectedProject, closePreview } = toRefs(props);
const loadingOnRemix = ref(false);
const { $modal } = useNuxtApp()

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
</script>