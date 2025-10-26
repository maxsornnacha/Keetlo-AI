import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2025-07-15",
  nitro: { preset: 'node-server' },
  devtools: { enabled: false },
  modules: [
    "@nuxt/ui",
    "@nuxt/eslint",
    "@nuxt/image",
    "@pinia/nuxt",
    "floating-vue/nuxt",
    "@nuxtjs/sitemap",
  ],
  image: {
    domains: ["lh3.googleusercontent.com"],
  },
  runtimeConfig: {
    public: {
      SITE_URL: process.env.NUXT_PUBLIC_SITE_URL,
      NUXT_PUBLIC_API_BASE: process.env.NUXT_PUBLIC_API_BASE,
    },
  },
  css: ["@/assets/css/main.css"],
  vite: {
    plugins: [tailwindcss()],
  },
  app: {
    head: {
      title: "Keetlo – Create AI-Generated HTML Projects",
      meta: [
        {
          name: "description",
          content:
            "Describe what you want and Keetlo generates beautiful, production-ready HTML pages with tailwindcss styling. Explore trending projects, remix ideas, and ship faster with AI.",
        },
      ],
     link: [
      { rel: "icon", href: "/favicon.ico" },
      { rel: "icon", type: "image/png", sizes: "48x48", href: "/favicon-48.png" },
      { rel: "icon", type: "image/png", sizes: "192x192", href: "/favicon-192.png" },
      { rel: "apple-touch-icon", sizes: "180x180", href: "/apple-touch-icon.png" },
      { rel: "manifest", href: "/site.webmanifest" }
    ]
    },
  },
   site: { 
    url: 'https://keetlo.com', 
    name: 'Keetlo' 
   }, 
  sitemap: {
    sources: ["/api/sitemap-projects"],
    exclude: ['/account/**', '/payment/**', '/oauth/**'],
  }
});
