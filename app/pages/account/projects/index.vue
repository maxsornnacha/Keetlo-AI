<template>
  <div class="relative text-slate-100 overflow-x-hidden bg-[#0D1117] selection:bg-indigo-400/20 selection:text-white">

    <div class="flex min-h-screen">
      <!-- Sidebar -->
      <ProjectSidebar class="hidden lg:block" :height-full="true" />

      <!-- Main -->
      <main class="flex-1 flex flex-col h-screen overflow-auto">
        <!-- Header -->
        <ProjectHeader :user="props.user" class="sticky top-0 z-50" />

        <!-- Content -->
        <section class="flex-1 lg:p-8 xl:p-10 overflow-hidden grid place-items-center">
          <div class="w-full max-w-5xl">
            <!-- Card -->
            <div
              class="relative overflow-hidden"
            >

              <!-- Body -->
              <div class="p-6 md:p-10">
                <!-- Title -->
                <h1 class="mx-auto max-w-3xl text-center text-4xl md:text-5xl font-extrabold leading-tight tracking-tight bg-clip-text text-transparent bg-gradient-to-b from-white to-slate-300/90">
                  Start a New Project
                </h1>

                <!-- Subtitle -->
                <p class="mx-auto mt-3 max-w-2xl text-center text-slate-300/85">
                  Describe your idea and let our AI suggest structure, pages, and components—clean, editable, and ready to tweak.
                </p>
                         <!-- Example prompts -->
                <div class="mx-auto hidden xl:block">
                  <div class="text-center text-sm text-slate-400">Try one of these:</div>
                  <div class="mt-3 flex flex-wrap justify-center gap-2">
                    <button
                      v-for="(ex, i) in examples"
                      :key="i"
                      class="cursor-pointer group inline-flex items-center gap-2 rounded-xl border border-slate-800 bg-slate-900/70 px-3 py-2 text-sm hover:bg-slate-900/90 transition active:scale-[.98]"
                      @click="copyExample(ex, i)"
                    >
                      <UIcon
                        :name="copiedIndex === i ? 'i-lucide-check' : 'i-lucide-clipboard-copy'"
                        class="size-4"
                        :class="copiedIndex === i ? 'text-emerald-400' : 'text-slate-300 group-hover:text-white'"
                      />
                      <span class="text-slate-200 group-hover:text-white">{{ ex }}</span>
                    </button>
                  </div>
                </div>
                <!-- Primary action -->
                <div class="mx-auto mt-6 w-full max-w-2xl">
                  <CreateProject />
                </div>

                <!-- Hints row -->
                <div class="mx-auto mt-7 flex flex-wrap items-center justify-center gap-3 text-xs text-slate-400">
                  <div class="inline-flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-900/70 px-2.5 py-1.5">
                    <UIcon name="i-lucide-zap" class="size-4 text-slate-200" />
                    <span>Generate pages & components</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-900/70 px-2.5 py-1.5">
                    <UIcon name="i-lucide-code" class="size-4 text-slate-200" />
                    <span>Clean, editable HTML/CSS</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-900/70 px-2.5 py-1.5">
                    <UIcon name="i-lucide-shield-check" class="size-4 text-slate-200" />
                    <span>No vendor lock-in</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>

    <!-- Toast -->
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-2"
    >
      <div
        v-if="copiedIndex !== null"
        class="fixed bottom-6 left-1/2 -translate-x-1/2 rounded-xl border border-slate-800 bg-slate-900/80 px-4 py-2 text-sm backdrop-blur-md shadow-lg"
      >
        Prompt copied — paste it into the box above
      </div>
    </transition>
  </div>
</template>



<script setup lang="ts">
import type { User } from "~/types/User"
const config = useRuntimeConfig();

useSeoMeta({
// Core
title: 'My Projects – Keetlo AI Website Generator',
description:
'Kick off a new AI-generated HTML project in minutes. Describe your idea and Keetlo suggests pages, structure, and components with clean, editable HTML/CSS and live preview.',
robots: 'index,follow,max-image-preview:large',
ogUrl: `${config.public.SITE_URL}/account/projects`,
ogSiteName: 'Keetlo',
ogType: 'website',
ogTitle: 'Start a New Project – Keetlo AI Website Generator',
ogDescription:
'Describe your idea and let Keetlo generate pages, components, and structure. Clean HTML/CSS, no vendor lock-in, and instant previews.',
ogImage: `${config.public.SITE_URL}/images/og/my-projects.png`,
twitterCard: 'summary_large_image',
twitterTitle: 'Start a New Project – Keetlo AI Website Generator',
twitterDescription:
'Launch a new AI-generated HTML project fast. Get suggested pages, components, and structure with instant preview.',
twitterImage: `${config.public.SITE_URL}/images/og/my-projects.png`,
})

const props = defineProps<{ user: User }>()

const examples = [
  "A SaaS dashboard with auth, pricing, and a changelog page",
  "A portfolio site with hero, case studies grid, and contact form",
  "A blog with categories, featured posts, and responsive cards",
  "A landing page for a mobile app with feature sections and FAQs",
  "An e-commerce product page with gallery, specs, and reviews"
]

const copiedIndex = ref<number | null>(null)
function copyExample(text: string, i: number) {
  navigator?.clipboard?.writeText?.(text).then(() => {
    copiedIndex.value = i
    setTimeout(() => (copiedIndex.value = null), 1200)
  })
}
</script>
