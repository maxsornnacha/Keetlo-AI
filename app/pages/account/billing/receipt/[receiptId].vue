<!-- pages/orders/[receiptId]/receipt.vue (example) -->
<template>
  <main class="min-h-screen">
    <!-- States -->
    <div v-if="loading" class="h-[100vh] grid place-items-center text-white/70">
      Loading receipt…
    </div>
    <div v-else-if="errorMsg" class="h-[100vh] grid place-items-center text-red-300">
      {{ errorMsg }}
    </div>

    <!-- Viewer -->
    <section v-else class="overflow-hidden border border-white/10 bg-slate-900/40">
      <iframe
        v-if="iframeSrc"
        ref="iframeEl"
        :src="iframeSrc"
        title="Receipt PDF"
        class="w-full h-[100vh] bg-white"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from "axios";
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute } from "vue-router";

const config = useRuntimeConfig();
const route = useRoute();

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
