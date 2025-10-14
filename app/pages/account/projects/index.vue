<template>
  <div class="relative min-h-screen text-slate-100 overflow-x-hidden">
    <div class="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute inset-0 bg-[#0b0f14]"/>
      <div
        class="absolute inset-0 opacity-90"
        :style="{
          backgroundImage: [
            // top cool glow
            `radial-gradient(1200px 700px at 50% -12%, rgba(144,180,255,0.55), rgba(144,180,255,0.08) 55%, rgba(144,180,255,0) 80%)`,
            // bottom warm glow
            `radial-gradient(1100px 800px at 50% 92%, rgba(255,172,70,0.55), rgba(255,120,110,0.25) 45%, rgba(255,120,110,0) 80%)`
          ].join(', ')
        }"
      />
      <div
        class="absolute inset-0 opacity-50 bg-black mix-blend-overlay "
        :style="{ backgroundImage: 'radial-gradient(rgba(255,255,255,0.08) 1px, transparent 1.2px)', backgroundSize: '6px 6px' }"
      />
      <div class="absolute inset-0 bg-[radial-gradient(1200px_700px_at_50%_50%,transparent_58%,rgba(0,0,0,0.55))]"/>
    </div>

    <div class="flex min-h-screen">
      <!-- Sidebar -->
      <ProjectSidebar class="hidden lg:block" :height-full="true" />

      <!-- Main -->
      <main class="flex-1 flex flex-col h-screen overflow-auto">
        <!-- Header (sticky for nice feel) -->
          <ProjectHeader :user="props.user" class="sticky top-0 z-50"/>

        <!-- Content -->
        <section class="flex-1 lg:p-6 md:p-10 grid place-items-center">
          <div class="w-full max-w-4xl">
            <!-- Card -->
            <div
              class="relative overflow-hidden rounded-2xl lg:border border-white/10 lg:bg-white/5 backdrop-blur-md shadow-2xl ring-1 ring-white/10"
            >
              <!-- Accent bar -->
              <div class="absolute inset-x-0 -top-px h-px bg-gradient-to-r from-transparent via-white/30 to-transparent"/>

              <div class="p-6 md:p-10 flex flex-col items-center text-center gap-6">
                <div class="inline-flex items-center gap-2 text-sm text-indigo-300/90">
                  <UIcon name="i-lucide-sparkles" class="size-5" />
                  <span>AI Project Starter</span>
                </div>

                <h1
                  class="text-4xl md:text-5xl font-extrabold leading-tight tracking-tight bg-clip-text text-transparent
                         bg-gradient-to-b from-white to-slate-300"
                >
                  Start a New Project
                </h1>

                <p class="max-w-2xl text-slate-300/90">
                  Describe your idea and let our AI suggest a structure, pages, and components—ready to edit.
                </p>

                <!-- Primary action -->
                <div class="w-full max-w-2xl">
                  <CreateProject />
                </div>

                <!-- Example prompts -->
                <div class="w-full max-w-2xl">
                  <div class="mt-2 text-sm text-slate-400">Try one of these:</div>
                  <div class="mt-3 flex flex-wrap justify-center gap-2">
                    <button
                      v-for="(ex, i) in examples"
                      :key="i"
                      class="group inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm
                             hover:bg-white/10 transition active:scale-[.98]"
                      @click="copyExample(ex, i)"
                    >
                      <UIcon
                        :name="copiedIndex === i ? 'i-lucide-check' : 'i-lucide-clipboard-copy'"
                        class="size-4"
                        :class="copiedIndex === i ? 'text-emerald-400' : 'text-slate-300'"
                      />
                      <span class="text-slate-200 group-hover:text-white">{{ ex }}</span>
                    </button>
                  </div>
                </div>

                <!-- Hints row -->
                <div class="mt-2 flex flex-wrap items-center justify-center gap-3 text-xs text-slate-400">
                  <div class="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-2.5 py-1.5">
                    <UIcon name="i-lucide-zap" class="size-4" />
                    <span>Generate pages & components</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-2.5 py-1.5">
                    <UIcon name="i-lucide-code" class="size-4" />
                    <span>Clean, editable HTML/CSS</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-2.5 py-1.5">
                    <UIcon name="i-lucide-shield-check" class="size-4" />
                    <span>No vendor lock-in</span>
                  </div>
                </div>
              </div>

              <!-- Soft glow at footer of card -->
              <div class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-white/10 to-transparent"/>
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
        class="fixed bottom-6 left-1/2 -translate-x-1/2 rounded-xl border border-white/10 bg-white/10 px-4 py-2 text-sm
               backdrop-blur-md shadow-lg"
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
