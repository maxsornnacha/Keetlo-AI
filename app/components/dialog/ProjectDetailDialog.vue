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
    class="fixed inset-0 bg-black/70 z-50 flex items-center justify-center"
    @click.self="close"
  >
      <div
        ref="panel"
        class="relative w-full lg:w-auto max-w-4xl rounded-2xl bg-[#0D1117] text-slate-100 shadow-2xl ring-1 ring-white/10"
      >
        <!-- Decorative header -->
        <div class="flex items-center justify-between rounded-t-2xl px-6 py-4 border-b border-white/10">
          <div class="flex items-center gap-3">
            <span class="inline-flex h-2.5 w-2.5 rounded-full bg-emerald-400 shadow-[0_0_12px] shadow-emerald-400/70" />
            <span class="inline-flex h-2.5 w-2.5 rounded-full bg-amber-400 shadow-[0_0_12px] shadow-amber-400/70" />
            <span class="inline-flex h-2.5 w-2.5 rounded-full bg-rose-400 shadow-[0_0_12px] shadow-rose-400/70" />
          </div>
          <button
          class="px-2 pt-1 h-9 w-9 rounded-full hover:bg-red-700 cursor-pointer"
          @click="close"
        >
          <UIcon name="i-lucide-x" class="size-5" />
        </button>
        </div>

        <!-- Body -->
        <div class="px-6 pb-6 pt-5 overflow-auto max-h-[90dvh] flex flex-col lg:flex-row gap-4 lg:gap-8">

          <div class="self-center lg:self-start flex-none w-[250px] h-[250px] overflow-hidden border rounded-md shadow-lg bg-white relative">
              <iframe
                :srcdoc="project.mainHtmlContent"
                class="absolute top-0 left-0 w-[1000px] h-[1200px] transform origin-top-left scale-[0.3]"
                style="pointer-events: none;"
                sandbox="allow-scripts allow-same-origin" 
                referrerpolicy="no-referrer"
                @load="lockIframe"
              />
            </div>
           <div>
                      <!-- Title -->
          <h2 id="project-title" class="text-2xl font-semibold tracking-tight">
            {{ project.title || "No title" }}
          </h2>

          <!-- Meta row -->
          <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-3">
            <div class="col-span-1">
              <div class="text-xs uppercase tracking-wider text-slate-400">Type</div>
              <div class="mt-1 inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-1.5 text-sm">
                <span class="inline-block h-2 w-2 rounded-full bg-indigo-400" />
                <span class="font-medium">{{ project.type }}</span>
              </div>
            </div>

            <div class="col-span-2">
              <div class="text-xs uppercase tracking-wider text-slate-400">Tags</div>
              <div class="mt-1 flex flex-wrap gap-2">
                <span
                  v-for="tag in project.tags"
                  :key="tag"
                  class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-slate-200"
                >
                  <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none">
                    <path d="M4 13l7 7 9-9-7-7H4v9z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="mt-5">
            <div class="text-xs uppercase tracking-wider text-slate-400">Description</div>
            <div class="mt-2 rounded-2xl border border-white/10 bg-gradient-to-b from-white/5 to-white/[0.02] p-4 text-slate-200 leading-relaxed">
              <p class="whitespace-pre-line">
                {{ project.description || "No description" }}
              </p>
            </div>
          </div>

          <!-- Actions -->
          <div class="mt-6 flex flex-col-reverse items-stretch gap-3 sm:flex-row sm:justify-end">
            <button
              class="cursor-pointer inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-indigo-400/40 active:scale-[.99] transition"
              @click="close"
            >
              Close
            </button>

            <button
              class="inline-flex items-center justify-center rounded-xl bg-indigo-600 hover:bg-indigo-700 cursor-pointer px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/10 hover:shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-400/50 active:scale-[.99] transition"
              @click="openEdit"
            >
              <svg class="mr-2 h-4 w-4" viewBox="0 0 24 24" fill="none">
                <path d="M4 21h4l11-11a2.828 2.828 0 10-4-4L4 17v4z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Edit
            </button>
          </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import type { Project } from '~/types/Projects'

const props = defineProps<{
  project: Project
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'onClose'): void
  (e: 'edit', project: Project): void
}>()

const close = () => emit('onClose')
const openEdit = () => emit('edit', props.project)
</script>
