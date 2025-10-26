<template>
  <main>
    <div class="flex flex-col items-center justify-center text-center">
      <div
        class="relative border border-gray-50/20 min-h-[480px] w-full flex flex-col items-center justify-center bg-cover bg-center bg-no-repeat p-4"
        style="
          background-image: 
            url('/images/backgrounds/keetlo-hero-bg-light.webp');
        "
      >
        <div class="max-w-3xl space-y-6">
          <h1
            class="text-4xl sm:text-5xl lg:text-6xl"
          >
            Generate HTML Page Projects with AI
          </h1>
          <p class="text-lg text-gray-500 sm:text-xl">
            Keetlo AI simplifies web development by automatically generating
            projects with html-tailwindcss pages, complete with a live UX/UI
            preview. Experience the future of coding with AI-powered efficiency.
          </p>
        </div>
        <a href="/account/projects">
          <button
            class="cursor-pointer mt-8 rounded-md bg-indigo-600 px-8 py-3 text-lg text-white shadow-lg transition-transform hover:scale-105 active:scale-95"
          >
            Get Started Today
          </button>
        </a>
      </div>
    </div>
    <div class="py-24 sm:py-46">
      <div class="mx-auto max-w-7xl px-6 lg:px-8">
        <div class="text-center">
          <h2 class="text-3xl font-bold tracking-tighter sm:text-4xl">
            Key Features
          </h2>
          <p class="mt-4 text-lg leading-8 text-gray-400">
            Keetlo AI offers a suite of powerful features designed to streamline
            your development workflow and enhance your productivity.
          </p>
        </div>
        <div
          class="mt-16 grid grid-cols-1 gap-x-8 gap-y-10 sm:grid-cols-2 lg:grid-cols-4"
        >
          <div
            class="flex flex-col gap-4 rounded-xl bg-white border border-gray-200 p-6 shadow-lg backdrop-blur-sm"
          >
            <span class="text-blue-500"
              ><UIcon name="i-lucide-brain" class="size-10 bg-indigo-500"
            /></span>
            <div>
              <h3 class="text-lg font-semibold">AI-Powered Code Generation</h3>
              <p class="mt-2 text-sm text-gray-400">
                Our AI intelligently generates clean, efficient Vue.js code with
                TypeScript, tailored to your project requirements.
              </p>
            </div>
          </div>
          <div
            class="flex flex-col gap-4 rounded-xl bg-white border border-gray-200 p-6 shadow-lg backdrop-blur-sm"
          >
            <span class="text-blue-500"
              ><UIcon name="i-lucide-code" class="size-10 bg-indigo-500"
            /></span>
            <div>
              <h3 class="text-lg font-semibold">HTML + Tailwindcss</h3>
              <p class="mt-2 text-sm text-gray-400">
                Leverage the power of HTML + Tailwindcss for responsiveness,
                scalable web applications.
              </p>
            </div>
          </div>
          <div
            class="flex flex-col gap-4 rounded-xl bg-white border border-gray-200 p-6 shadow-lg backdrop-blur-sm"
          >
            <span class="text-blue-500"
              ><UIcon name="i-lucide-eye" class="size-10 bg-indigo-500"
            /></span>
            <div>
              <h3 class="text-lg font-semibold">Live UX/UI Preview</h3>
              <p class="mt-2 text-sm text-gray-400">
                Visualize your project's UI in real-time to iterate and refine
                your design effortlessly.
              </p>
            </div>
          </div>
          <div
            class="flex flex-col gap-4 rounded-xl bg-white border border-gray-200 p-6 shadow-lg backdrop-blur-sm"
          >
            <span class="text-blue-500"
              ><UIcon name="i-lucide-rocket" class="size-10 bg-indigo-500"
            /></span>
            <div>
              <h3 class="text-lg font-semibold">Rapid Project Setup</h3>
              <p class="mt-2 text-sm text-gray-400">
                Quickly initialize new projects with pre-configured settings,
                saving you valuable time.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div>
      <div class="mx-auto max-w-7xl px-6 lg:px-8">
        <div class="text-center">
          <h2 class="text-3xl font-bold tracking-tighter sm:text-4xl">
            How It Works
          </h2>
          <p class="mt-4 text-lg leading-8 text-gray-400">
            Keetlo AI simplifies the project creation process into three easy
            steps.
          </p>
        </div>
        <div class="mt-16 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
          <div class="flex flex-col gap-4">
            <div
              class="w-full aspect-video rounded-xl bg-cover bg-center bg-no-repeat shadow-lg"
              style="
                background-image: url('/images/features/define-project.png');
              "
            />
            <div>
              <h3 class="text-lg font-semibold">1. Define Your Project</h3>
              <p class="mt-2 text-sm text-gray-400">
                Input your specifications, features, components, and styling
                preferences using our intuitive interface.
              </p>
            </div>
          </div>
          <div class="flex flex-col gap-4">
            <div
              class="w-full aspect-video rounded-xl bg-cover bg-center bg-no-repeat shadow-lg"
              style="
                background-image: url('/images/features/generate-code.png');
              "
            />
            <div>
              <h3 class="text-lg font-semibold">2. Generate Code</h3>
              <p class="mt-2 text-sm text-gray-400">
                Our AI engine processes your input and generates a complete
                Vue.js project with TypeScript.
              </p>
            </div>
          </div>
          <div class="flex flex-col gap-4">
            <div
              class="w-full aspect-video rounded-xl bg-cover bg-center bg-no-repeat shadow-lg"
              style="
                background-image: url('/images/features/preview-refine.png');
              "
            />
            <div>
              <h3 class="text-lg font-semibold">3. Preview and Refine</h3>
              <p class="mt-2 text-sm text-gray-400">
                Interact with a live preview of your application's UX/UI and
                make adjustments as needed.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="text-center py-24 sm:py-32">
      <h2 class="text-3xl font-bold tracking-tighter sm:text-4xl">
        Ready to Build Something Amazing?
      </h2>
      <p class="mt-4 text-lg leading-8 text-gray-400 max-w-2xl mx-auto">
        Start your journey with Keetlo AI today and experience the future of web
        development.
      </p>
      <div class="mt-10 flex items-center justify-center gap-x-6">
        <a href="/account/projects">
          <button
            class="cursor-pointer rounded-md bg-indigo-600 px-8 py-3 text-lg text-white shadow-lg transition-transform hover:scale-105 active:scale-95"
          >
            Get Started for Free
          </button>
        </a>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
const config = useRuntimeConfig();
useSeoMeta({
  // Core
  title: 'Keetlo Features – Build HTML + Tailwind Pages with AI',
  description:
    'Turn simple prompts into production-ready HTML + Tailwind pages with live preview, remixable templates, and one-click export.',
  robots: 'index,follow,max-image-preview:large',
  ogUrl: `${config.public.SITE_URL}/features`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Keetlo Features – Build HTML + Tailwind Pages with AI',
  ogDescription:
    'Generate beautiful, responsive pages, preview in real time, remix templates, and export clean HTML. Faster web creation with Keetlo AI.',
  ogImage: `${config.public.SITE_URL}/images/og/feature.png`,
  twitterCard: 'summary_large_image',
  twitterTitle: 'Keetlo Features – Build HTML + Tailwind Pages with AI',
  twitterDescription:
    'Generate HTML + Tailwind pages with live preview, remixable templates, and instant export.',
  twitterImage: `${config.public.SITE_URL}/images/og/feature.png`,
})


</script>
