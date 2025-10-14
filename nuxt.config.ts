import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2025-07-15",
  devtools: { enabled: false },
  modules: [
    "@nuxt/ui", 
    "@nuxt/eslint", 
    "@nuxt/image", 
    '@pinia/nuxt', 
    'floating-vue/nuxt', 
  ],
  image: {
    domains: ['lh3.googleusercontent.com'],
  },
  runtimeConfig: {
    public: {
      SITE_URL: process.env.NUXT_PUBLIC_SITE_URL,
      NUXT_PUBLIC_API_BASE: process.env.NUXT_PUBLIC_API_BASE
    }
  },
  css: ['@/assets/css/main.css'],
  vite: {
    plugins: [tailwindcss()],
  },
  app: {
    head: {
      title: 'Keetlo – Create AI-Generated HTML Projects',
      meta: [
        { name: 'description', content:'Describe what you want and Keetlo generates beautiful, production-ready HTML pages with tailwindcss styling. Explore trending projects, remix ideas, and ship faster with AI.' },
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/logo.png' }
      ]
    }
  }
});