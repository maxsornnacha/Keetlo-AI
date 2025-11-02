<template>
  <div class="relative w-full flex flex-col items-center">
    <!-- Card container -->
    <div
      class="w-full max-w-[800px] relative transition border border-gray-50 bg-white border border-gray-200 rounded-[30px]"
    >
      <!-- Floating label -->
      <label
        for="project-idea"
        class="absolute -top-3 left-4 inline-flex items-center gap-2 px-2 rounded-full text-[11px] font-semibold tracking-wide uppercase bg-[#0f1320] text-white ring-1 ring-white/10"
      >
        <UIcon name="i-lucide-sparkles" class="size-3.5" />
        Project Brief
      </label>

      <!-- simple previews (files only) -->
      <div v-if="filesValue.length" class="flex gap-2 items-center overflow-x-auto w-full py-2 px-4">
        <div v-for="(file, i) in filesValue" :key="i" class="relative group shrink-0 border border-gray-200 rounded-md p-2">
          <img
            v-if="file.base64.startsWith('data:image/')"
            :src="file.base64"
            class="w-16 h-16 object-cover cursor-pointer"
            @click="onOpenImagePreview(file)"
          >
          <div v-else class="flex gap-2 items-center">
           <div>
              <UIcon name="i-lucide-file" class="size-5" />
           </div> 
          <div class="text-xs break-all flex flex-col gap-0 items-start">
            <p>{{ file.fileName.length > 30 ? file.fileName.slice(0, 30)+'...' : file.fileName }}</p>
            <p>Type: {{ file.fileType.length > 30 ? file.fileType.slice(0,30)+'...' : file.fileType }}</p>
          </div>
          </div>

          <div class="absolute -top-2 right-0">
          <button
          class="
          cursor-pointer hover:bg-gray-200 border border-gray-200 bg-white rounded-md p-1 pb-0
          transition-opacity duration-200
          md:opacity-0 md:group-hover:opacity-100
          focus-visible:opacity-100
          "
                @click="removeFile(i)"
          ><UIcon name="i-lucide-x" class="size-4" /></button>
        </div>
        </div>
      </div>

      <!-- Textarea -->
      <textarea
        id="project-idea"
        ref="textareaRef"
        v-model="inputValue"
        :maxlength="maxChars"
        :placeholder="animatedPlaceholder"
        class="w-full min-h-[90px] max-h-[55vh] rounded-2xl px-5 pt-5 text-base text-black/90 placeholder:text-black/40 focus:outline-none resize-none leading-relaxed"
        :aria-busy="loadingSubmit ? 'true' : 'false'"
        @input="autoResize"
        @keydown.enter.exact.prevent="generateProject"
        @keydown.meta.enter.prevent="generateProject"
        @keydown.ctrl.enter.prevent="generateProject"
        @paste="onPaste" 
      />

      <!-- Bottom bar: helper + counter + button -->
      <div class="flex flex-wrap items-center gap-3 px-4 py-2">
        <div>
          <div class="border border-gray-200 rounded-md flex items-center">
            <button class="hidden cursor-pointer hover:bg-gray-200 px-2 pt-1">
              <UIcon name="i-lucide-plus" class="size-4" />
            </button>
            <div class="h-6 w-[1px] bg-gray-200" />
            <label
              v-tooltip="'Upload images'"
              class="cursor-pointer hover:bg-gray-200 px-2 py-1 gap-1 flex items-center"
            >
              <UIcon name="i-lucide-paperclip" class="size-4" />
              <span>Attach images</span>
              <input
                id="image-input"
                type="file"
                hidden
                multiple
                :accept="['image/*','image/heic','image/heif'].join(',')"
                @change="onPick"
              >
          </label>
          </div>
        </div>
        <div class="ml-auto flex items-center gap-3">
          <!-- Counter -->
          <span class="text-xs tabular-nums text-black/60">
            {{ chars }}/{{ maxChars }}
          </span>

          <!-- Submit -->
          <button
            type="button"
            class="relative inline-flex items-center gap-2 h-10 px-4 rounded-lg text-black text-sm font-medium transition disabled:opacity-60 disabled:cursor-not-allowed text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400/50"
            :class="
              canSubmit
                ? 'cursor-pointer bg-indigo-600 hover:bg-indigo-700'
                : 'bg-indigo-500'
            "
            :disabled="!canSubmit || loadingSubmit"
            @click="generateProject"
          >
            <UIcon
              v-if="!loadingSubmit"
              name="i-lucide-wand-sparkles"
              class="size-5"
            />
            <UIcon
              v-else
              name="i-lucide-loader-circle"
              class="size-5 text-white animate-spin"
            />
            <span>{{ loadingSubmit ? "Generating…" : "Generate" }}</span>

            <!-- Progress hairline while loading -->
            <span
              v-if="loadingSubmit"
              class="pointer-events-none absolute -bottom-[2px] left-0 h-[2px] w-full overflow-hidden rounded-b-lg"
            >
              <span
                class="block h-full w-1/2 animate-[progress_1.2s_linear_infinite] bg-white/70"
              />
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Tiny tips row -->
    <p class="mt-2 text-xs text-black/50">
      Tip: Press
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">Shift + Enter</kbd> for new
      line, <kbd class="rounded bg-white/10 px-1.5 py-0.5">⌘/Ctrl</kbd> +
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> Or
      <kbd class="rounded bg-white/10 px-1.5 py-0.5">Enter</kbd> to generate. to
      generate.
    </p>

      <ImagePreviewDialog 
      :file="selectedFile" 
      :is-open="isOpenImagePreview" 
      @on-close="closeImagePreview"
      />
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import ImagePreviewDialog from "./dialog/ImagePreviewDialog.vue";
import type { File as FileType } from "~/types/Projects";
const { $modal } = useNuxtApp();


const config = useRuntimeConfig();
const MAX = 10 * 1024 * 1024 // 10MB (same as your picker rule)
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const inputValue = ref<string>("");
const filesValue = ref<{
  fileName: string,
  fileType: string,
  fileSize: number,
  base64: string,
}[]>([]);
const loadingSubmit = ref(false);
const selectedFile = ref<FileType | null>(null);
const isOpenImagePreview = ref(false);

const onOpenImagePreview = (file : FileType) => {
  selectedFile.value = file;
  isOpenImagePreview.value = true;
}

const closeImagePreview = () => {
    selectedFile.value = null;
  isOpenImagePreview.value = true;
}

/* ------- UX helpers ------- */
const maxChars = ref(2000); // soft cap for nice counter UX
const chars = ref(0);
watch(inputValue, (v) => (chars.value = v.length));

/* Animated placeholder (typed-in effect) */
const fullPlaceholder =
  "Describe your website idea… e.g. “A responsive landing with hero, features grid, and a contact form.”";
const animatedPlaceholder = ref("");
// put this once at the top-level (module scope)
let rafId = 0;

// REPLACE your function with this:
function runPlaceholderTyping() {
  cancelAnimationFrame(rafId);

  let i = 0; // current char index
  let dir: 1 | -1 = 1; // 1 = typing, -1 = deleting
  let last = 0; // last frame timestamp
  let pauseUntil = 0; // pause gate

  const TYPE_MS = 45; // speed while typing
  const ERASE_MS = 30; // speed while deleting
  const PAUSE_MS = 900; // pause at full & empty

  animatedPlaceholder.value = "";

  const step = (ts: number) => {
    if (!last) last = ts;

    // handle pause windows
    if (pauseUntil && ts < pauseUntil) {
      rafId = requestAnimationFrame(step);
      return;
    }

    const interval = dir === 1 ? TYPE_MS : ERASE_MS;
    if (ts - last >= interval) {
      last = ts;
      i += dir;

      if (i >= fullPlaceholder.length) {
        i = fullPlaceholder.length;
        animatedPlaceholder.value = fullPlaceholder;
        dir = -1; // start deleting next
        pauseUntil = ts + PAUSE_MS; // pause at full text
      } else if (i <= 0) {
        i = 0;
        animatedPlaceholder.value = "";
        dir = 1; // start typing next
        pauseUntil = ts + PAUSE_MS; // pause at empty
      } else {
        animatedPlaceholder.value = fullPlaceholder.slice(0, i);
      }
    }

    rafId = requestAnimationFrame(step);
  };

  if (import.meta.client) rafId = requestAnimationFrame(step);
}

/* Auto-resize textarea to content */
function autoResize() {
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = "0px";
  el.style.height = Math.min(el.scrollHeight, window.innerHeight * 0.55) + "px";
}

const fileToDataUrl = (file: File) =>
  new Promise<string>((resolve, reject) => {
    const r = new FileReader()
    r.onload = () => resolve(r.result as string)        // data:<mime>;base64,....
    r.onerror = () => reject(r.error)
    r.readAsDataURL(file)
  })

const onPick = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return

  const selected = Array.from(input.files)

  if (selected.some(f => f.size > MAX)) { 
      await $modal.alert({
      title: "Error",
      html: `<p>Maximum file size is 10 MB. Please select file again</p>`,
      variant: "danger",
    });
     return;
     }

  const mapped = await Promise.all(
    selected.map(async (f) => ({
      fileName: f.name,
      fileType: f.type || 'application/octet-stream',
      fileSize: f.size,
      base64: await fileToDataUrl(f),
    }))
  )
  filesValue.value = [...filesValue.value, ...mapped]
  input.value = ''
}

const removeFile = (index: number) => {
  filesValue.value.splice(index, 1)
}

const blobToDataUrl = (blob: Blob) =>
  new Promise<string>((resolve, reject) => {
    const r = new FileReader()
    r.onload = () => resolve(r.result as string)
    r.onerror = () => reject(r.error)
    r.readAsDataURL(blob)
})
const appendImageFile = async (file: File) => {
  if (file.size > MAX) {
    await $modal.alert({
      title: "Error",
      html: `<p>Maximum file size is 10 MB. Please select file again</p>`,
      variant: "danger",
    })
    return
  }
  filesValue.value.push({
    fileName: file.name || `pasted-${Date.now()}.png`,
    fileType: file.type || "image/png",
    fileSize: file.size,
    base64: await blobToDataUrl(file),
  })
}

const onPaste = async (e: ClipboardEvent) => {
  const cd = e.clipboardData
  if (!cd) return

  // 1) If clipboard has image files (screenshots, copied images)
  const items = Array.from(cd.items || [])
  const imageItems = items.filter(it => it.kind === "file" && it.type.startsWith("image/"))

  if (imageItems.length > 0) {
    e.preventDefault() // prevent raw image/text from being inserted into the textarea
    for (const it of imageItems) {
      const file = it.getAsFile()
      if (file) await appendImageFile(file)
    }
    return
  }

  // 2) Fallback: if clipboard has text that looks like an image URL or data URL
  const text = cd.getData("text/plain")?.trim()
  if (text) {
    // data URL (already base64)
    if (text.startsWith("data:image/")) {
      e.preventDefault()
      // rough size estimation is hard from data URL; let it pass
      filesValue.value.push({
        fileName: `pasted-${Date.now()}.png`,
        fileType: text.substring(5, text.indexOf(";")) || "image/png",
        fileSize: text.length, // not accurate, but you can skip validating size here
        base64: text,
      })
      return
    }

    // http(s) image URL → fetch → blob → base64
    const isLikelyImageUrl =
      /^https?:\/\/.+\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/i.test(text)
    if (isLikelyImageUrl) {
      e.preventDefault()
      try {
        const res = await fetch(text, { mode: "cors" })
        const blob = await res.blob()
        // Skip SVG XML heavy case or non-image
        if (!blob.type.startsWith("image/")) return
        if (blob.size > MAX) {
          await $modal.alert({
            title: "Error",
            html: `<p>Maximum file size is 10 MB. Please select file again</p>`,
            variant: "danger",
          })
          return
        }
        filesValue.value.push({
          fileName: text.split("/").pop() || `pasted-${Date.now()}.png`,
          fileType: blob.type,
          fileSize: blob.size,
          base64: await blobToDataUrl(blob),
        })
      } catch {
        /* ignore fetch failures */
      }
    }
  }
}


/* Submit rules */
const canSubmit = computed(
  () =>
    !!inputValue.value &&
    !loadingSubmit.value &&
    inputValue.value.length <= maxChars.value
);

/* ------- Submit ------- */
const generateProject = async () => {
  if (!canSubmit.value) return;
  loadingSubmit.value = true;

  try {
    const payload = {
      input: inputValue.value.trim(),
      files: filesValue.value,
    };
    const response = await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/create`,
      payload
    );
    const projectId = response.data.projectId;
    window.location.href = `/account/projects/${projectId}`;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.status === 401) {
        sessionStorage.setItem("create-project-text", inputValue.value);
        window.location.href = `/auth/login?next=${window.location.href}`;
      } else {
        await $modal.alert({
          title: "Error",
          html: `<p>${toErrorMessage(error?.response?.data?.message)}</p>`,
          variant: "danger",
        });
      }
    } else {
      await $modal.alert({
        title: "Error",
        html: `<p>${toErrorMessage(error)}</p>`,
        variant: "danger",
      });
    }
    loadingSubmit.value = false;
  }
};

const checkSessionStorage = () => {
  const createProjectText = sessionStorage.getItem("create-project-text");
  if (createProjectText) {
    inputValue.value = createProjectText;
    sessionStorage.removeItem("create-project-text");
  }
};

/* ------- Mount ------- */
onMounted(() => {
  checkSessionStorage();
  runPlaceholderTyping();
  nextTick(autoResize);
});
</script>

<style scoped>
@keyframes progress {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(200%);
  }
}
</style>
