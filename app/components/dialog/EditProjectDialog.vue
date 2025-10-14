<template>
  <!-- Backdrop -->
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="props.isOpen"
      class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center"
      @click.self="close"
    >
      <div
        tabindex="0"
        class="relative w-full max-w-2xl rounded-2xl bg-[#0D1117] shadow-2xl ring-1 ring-white/10"
      >
        <!-- Header -->
        <div
          class="flex items-center justify-between rounded-t-2xl border-b border-white/10 px-6 py-4"
        >
          <div class="flex items-center gap-3">
            <span
              class="inline-flex h-2.5 w-2.5 rounded-full bg-emerald-400 shadow-[0_0_10px] shadow-emerald-400/70"
            />
            <span
              class="inline-flex h-2.5 w-2.5 rounded-full bg-amber-400 shadow-[0_0_10px] shadow-amber-400/70"
            />
            <span
              class="inline-flex h-2.5 w-2.5 rounded-full bg-rose-400 shadow-[0_0_10px] shadow-rose-400/70"
            />
          </div>

          <h2 id="dialog-title" class="text-lg font-semibold tracking-tight">
            Edit Project
          </h2>
           <button
          class="px-2 pt-1 h-9 w-9 rounded-full hover:bg-red-700 cursor-pointer"
          @click="close"
        >
          <UIcon name="i-lucide-x" class="size-5" />
        </button>
        </div>

        <!-- Body -->
        <form class="px-6 py-5" @submit.prevent="submitEdit">
          <!-- Title -->
          <div class="mb-4">
            <label class="mb-1.5 block text-sm font-medium text-slate-300"
              >Title</label
            >
            <input
              ref="titleRef"
              v-model="form.title"
              type="text"
              class="w-full rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-slate-100 placeholder-slate-400 focus:border-indigo-400/30 focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
              placeholder="Project title"
              required
            >
          </div>

          <!-- Description -->
          <div class="mb-4">
            <label class="mb-1.5 block text-sm font-medium text-slate-300"
              >Description</label
            >
            <textarea
              v-model="form.description"
              rows="4"
              class="w-full rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-slate-100 placeholder-slate-400 focus:border-indigo-400/30 focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
              placeholder="What is this project about?"
            />
          </div>

          <!-- Type -->
          <div class="mb-4">
            <label class="mb-1.5 block text-sm font-medium text-slate-300"
              >Type</label
            >
            <input
              v-model="form.type"
              type="text"
              class="w-full rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-slate-100 placeholder-slate-400 focus:border-indigo-400/30 focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
              placeholder="e.g. Web, Mobile, Data"
            >
          </div>

          <!-- Tags -->
          <div class="mb-1.5">
            <label class="mb-1.5 block text-sm font-medium text-slate-300"
              >Tags (comma separated)</label
            >
            <input
              v-model="form.tagsStr"
              type="text"
              class="w-full rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-slate-100 placeholder-slate-400 focus:border-indigo-400/30 focus:outline-none focus:ring-2 focus:ring-indigo-400/40"
              placeholder="design, vue, tailwind"
            >
          </div>

          <!-- Tags preview -->
          <div v-if="tagsPreview.length" class="mb-4">
            <div class="text-xs uppercase tracking-wider text-slate-400">
              Preview
            </div>
            <div class="mt-2 flex flex-wrap gap-2">
              <span
                v-for="tag in tagsPreview"
                :key="tag"
                class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-slate-200"
              >
                <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M4 13l7 7 9-9-7-7H4v9z"
                    stroke="currentColor"
                    stroke-width="1.6"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                {{ tag }}
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div
            class="mt-6 flex flex-col-reverse items-stretch gap-3 sm:flex-row sm:justify-end"
          >
            <button
              type="button"
              class="cursor-pointer inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-indigo-400/40 active:scale-[.99] transition"
              @click="close"
            >
              Cancel
            </button>

            <button
              type="submit"
              class="cursor-pointer inline-flex items-center justify-center rounded-xl bg-indigo-600 hover:bg-indigo-700 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/10 hover:shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-400/50 active:scale-[.99] transition"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { reactive, watch, ref, nextTick, computed } from "vue";
import type { Project } from "~/types/Projects";

const props = defineProps<{
  project: Project;
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: "onClose"): void;
  (e: "save", updatedProject: Project): void;
}>();

const form = reactive({
  title: "",
  description: "",
  type: "",
  tagsStr: "",
});

// Autofocus on open
const titleRef = ref<HTMLInputElement | null>(null);
watch(
  () => props.isOpen,
  async (open) => {
    if (open) await nextTick().then(() => titleRef.value?.focus());
  }
);

// Populate form when project changes
watch(
  () => props.project,
  (p) => {
    if (p) {
      form.title = p.title || "";
      form.description = p.description || "";
      form.type = p.type || "";
      form.tagsStr = (p.tags || []).join(", ");
    }
  },
  { immediate: true }
);

const tagsPreview = computed(() =>
  form.tagsStr
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean)
);

const close = () => emit("onClose");

const submitEdit = () => {
  const updatedProject: Project = {
    ...props.project,
    title: form.title.trim(),
    description: form.description.trim(),
    type: form.type.trim(),
    tags: tagsPreview.value,
  };
  emit("save", updatedProject);
};
</script>
