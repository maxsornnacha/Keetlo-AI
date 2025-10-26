<!-- pages/orders/[receiptId]/receipt.vue (example) -->
<template>
  <main class="min-h-screen">
    <!-- States -->
    <div v-if="loading" class="h-[100vh] flex flex-col items-center justify-center gap-2 text-black/70">
      <UIcon
      name="i-lucide-loader-circle"
      class="size-12 text-indigo-400 animate-spin"
      />
      Loading receipt…
    </div>
    <div v-else-if="errorMsg" class="h-[100vh] grid place-items-center text-red-300">
      {{ errorMsg }}
    </div>

    <!-- Viewer -->
    <section v-else class="overflow-hidden h-screen">
     <header
      class="sticky top-0"
     >
      <div class="mx-auto container p-2 flex justify-between gap-4 ">
        <!-- Left: Back + Title -->
        <div class="flex items-center gap-3 min-w-0">
          <a
            :href="`${back as string}`"
            class="text-sm opacity-80 hover:text-gray-500 whitespace-nowrap"
            >← Back</a
          >
          <h1
            class="truncate max-w-[50vw] sm:max-w-[40vw] font-semibold"
          >
            PDF View - {{ receiptId }}
          </h1>
        </div>

        <!-- Right: Controls (UserProfile is in normal header flow, in front of everything) -->
        <div class="flex gap-2 items-center">
                <NuxtLink
        v-if="props.user.isLogin === 'yes'"
        v-tooltip="'My Projects'"
         to="/account/projects"
        class="flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
      >
        <UIcon name="i-lucide-folder-open" class="size-6" />
      </NuxtLink>
          <UserProfile :user="props.user" />
        </div>
      </div>
      </header>
      <iframe
        v-if="iframeSrc"
        ref="iframeEl"
        :src="iframeSrc"
        title="Receipt PDF"
        class="h-full w-full"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from "axios";
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute } from "vue-router";
import UserProfile from "~/components/userProfile.vue";
import type { User } from "~/types/User";

const config = useRuntimeConfig();
const route = useRoute();
const props = defineProps<{
  user: User
}>();
const back = route.query.back || "/";

const receiptId = String(route.params.receiptId ?? "");
const iframeSrc = ref<string | null>(null);
const loading = ref(true);
const errorMsg = ref<string | null>(null);
const iframeEl = ref<HTMLIFrameElement | null>(null);

async function loadPdfBlob(inv: string) {
  const response = await api.get(`${config.public.NUXT_PUBLIC_API_BASE}/orders/${encodeURIComponent(inv)}/receipt.pdf`, {
    responseType: "blob",
    headers: {
        "Accept": "application/pdf",
    }
  });
  const buf = await response.data.arrayBuffer();
  const blob = new Blob([buf], { type: "application/pdf" });
  return URL.createObjectURL(blob);
}

async function init() {
  loading.value = true;
  errorMsg.value = null;
  // revoke previous blob (if any)
  if (iframeSrc.value?.startsWith("blob:")) {
    URL.revokeObjectURL(iframeSrc.value);
  }
  try {
    const blobUrl = await loadPdfBlob(receiptId);
    iframeSrc.value = blobUrl;
  } catch (e: unknown) {
    if(axios.isAxiosError(e)){
    errorMsg.value = e?.message ?? "Failed to load receipt.";
    iframeSrc.value = null;
    }
  } finally {
    loading.value = false;
  }
}

onMounted(init);

watch(
  () => route.params.receiptId,
  async (next) => {
    if (!next) return;
    await init();
  }
);

onBeforeUnmount(() => {
  if (iframeSrc.value?.startsWith("blob:")) {
    URL.revokeObjectURL(iframeSrc.value);
  }
});
</script>
