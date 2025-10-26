<template>
  <div class="relative overflow-x-hidden selection:bg-indigo-400/20 selection:text-black">

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
                <h1 class="mx-auto max-w-3xl text-center text-4xl">
                  Start a New Project
                </h1>

                <!-- Subtitle -->
                <p class="mx-auto mt-3 max-w-2xl text-center text-gray-400">
                  Describe your idea and let our AI suggest structure, pages, and components—clean, editable, and ready to tweak.
                </p>
                <!-- Primary action -->
                <div class="mx-auto mt-6 w-full max-w-2xl">
                  <CreateProject />
                </div>

                <!-- Hints row -->
                <div class="mx-auto mt-7 flex flex-wrap items-center justify-center gap-3 text-xs text-black">
                  <div class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-2.5 py-1.5">
                    <UIcon name="i-lucide-zap" class="size-4 text-black" />
                    <span>Generate pages & components</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-2.5 py-1.5">
                    <UIcon name="i-lucide-code" class="size-4 text-black" />
                    <span>Clean, editable HTML/CSS</span>
                  </div>
                  <div class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-2.5 py-1.5">
                    <UIcon name="i-lucide-shield-check" class="size-4 text-black" />
                    <span>No vendor lock-in</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
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
</script>
